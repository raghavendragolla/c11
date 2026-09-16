package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.Job

object PushNotificationHelper {

    const val CHANNEL_MATCHES_ID = "career_radar_matches_channel"
    const val CHANNEL_MATCHES_NAME = "Career Radar Job Matches"
    const val CHANNEL_DIGEST_ID = "career_radar_digest_channel"
    const val CHANNEL_DIGEST_NAME = "Career Radar Updates & Digest"

    private const val PREFS_NAME = "career_radar_notification_prefs"
    private const val KEY_NOTIFS_ENABLED = "notifs_enabled"
    private const val KEY_MIN_NOTIF_SCORE = "min_notif_score"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val matchesChannel = NotificationChannel(
                CHANNEL_MATCHES_ID,
                CHANNEL_MATCHES_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alerts when a job matches your radar threshold"
                enableVibration(true)
                enableLights(true)
            }

            val digestChannel = NotificationChannel(
                CHANNEL_DIGEST_ID,
                CHANNEL_DIGEST_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily digests and application updates"
            }

            notificationManager.createNotificationChannel(matchesChannel)
            notificationManager.createNotificationChannel(digestChannel)
        }
    }

    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun isPushNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIFS_ENABLED, true)
    }

    fun setPushNotificationsEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_NOTIFS_ENABLED, enabled)
            .apply()
    }

    fun getMinNotificationScore(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_MIN_NOTIF_SCORE, 85)
    }

    fun setMinNotificationScore(context: Context, score: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_MIN_NOTIF_SCORE, score)
            .apply()
    }

    fun sendJobMatchNotification(context: Context, job: Job) {
        if (!isPushNotificationsEnabled(context)) return

        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("job_id", job.id)
            putExtra("from_notification", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            job.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🎯 Radar Hit: ${job.radarMatchScore}% Match"
        val content = "${job.title} at ${job.company} (${job.workMode} • ${job.salaryFormatted})"

        val builder = NotificationCompat.Builder(context, CHANNEL_MATCHES_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$content\nKey match reasons: ${job.matchReasons.firstOrNull() ?: "Skills & Salary match"}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)

        try {
            NotificationManagerCompat.from(context).notify(job.id.hashCode(), builder.build())
        } catch (e: SecurityException) {
            // Android 13+ permission not yet granted
        }
    }

    fun sendCustomNotification(context: Context, title: String, message: String, jobId: String? = null) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (jobId != null) {
                putExtra("job_id", jobId)
                putExtra("from_notification", true)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_MATCHES_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            // Android 13+ permission not yet granted
        }
    }
}
