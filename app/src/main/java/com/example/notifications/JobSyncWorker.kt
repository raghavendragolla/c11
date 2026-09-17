package com.example.notifications

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.api.ApiClientProvider
import java.util.concurrent.TimeUnit

class JobSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.i(TAG, "JobSyncWorker started periodic background scan for new jobs")
        val context = applicationContext

        if (!PushNotificationHelper.isPushNotificationsEnabled(context)) {
            Log.i(TAG, "Push notifications disabled by user preference, skipping scan")
            return Result.success()
        }

        try {
            val api = ApiClientProvider.getService(context)
            val response = api.getJobs(limit = 40)
            if (!response.isSuccessful || response.body() == null) {
                Log.w(TAG, "API failed with code: ${response.code()}")
                return Result.retry()
            }

            val jobs = response.body()!!
            val prefs = context.getSharedPreferences("career_radar_seen_jobs", Context.MODE_PRIVATE)
            val seenIds = prefs.getStringSet("seen_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
            val minScore = PushNotificationHelper.getMinNotificationScore(context)

            val newMatches = jobs.filter { job ->
                !seenIds.contains(job.id) && job.radarMatchScore >= minScore
            }

            // Always update seen IDs so we don't spam duplicate alerts
            seenIds.addAll(jobs.map { it.id })
            prefs.edit().putStringSet("seen_ids", seenIds).apply()

            if (newMatches.isNotEmpty()) {
                val topMatch = newMatches.maxByOrNull { it.radarMatchScore } ?: newMatches.first()
                Log.i(TAG, "Found ${newMatches.size} new matches! Sending notification for: ${topMatch.title}")
                PushNotificationHelper.sendJobMatchNotification(context, topMatch)
            } else {
                Log.i(TAG, "Background sync complete. No new high-match jobs above threshold ($minScore%)")
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "JobSyncWorker failed with exception", e)
            return Result.retry()
        }
    }

    companion object {
        private const val TAG = "JobSyncWorker"
        private const val UNIQUE_WORK_NAME = "career_radar_periodic_job_sync"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // Periodic work runs every 1 hour (WorkManager minimum interval is 15 minutes)
            val periodicRequest = PeriodicWorkRequestBuilder<JobSyncWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
            Log.i(TAG, "Periodic JobSyncWorker scheduled with 1 hour interval")
        }
    }
}
