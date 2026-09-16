package com.example.data.repository

import android.content.Context
import com.example.data.api.ApiClientProvider
import com.example.data.db.AppDatabase
import com.example.data.db.SavedJobDao
import com.example.data.db.SavedJobEntity
import com.example.data.model.Job
import com.example.data.model.JobFilter
import com.example.data.model.NotificationItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class JobRepository(private val context: Context) {

    private val savedJobDao: SavedJobDao by lazy {
        AppDatabase.getDatabase(context).savedJobDao()
    }

    val savedJobs: Flow<List<SavedJobEntity>> = savedJobDao.getAllSavedJobs()

    fun isJobSaved(id: String): Flow<Boolean> = savedJobDao.isJobSaved(id)

    suspend fun saveJob(job: Job, notes: String = ""): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val entity = SavedJobEntity.fromJob(job, notes)
            savedJobDao.insert(entity)
            // Try syncing with FastAPI backend
            try {
                ApiClientProvider.getService(context).saveJobRemote(job.id)
            } catch (_: Exception) {
                // Ignore remote network error; local Room copy is saved
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unsaveJob(jobId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            savedJobDao.deleteById(jobId)
            try {
                ApiClientProvider.getService(context).unsaveJobRemote(jobId)
            } catch (_: Exception) {
                // Ignore remote network error
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateJobNotes(jobId: String, notes: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            savedJobDao.updateNotes(jobId, notes)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchJobs(filter: JobFilter = JobFilter()): Result<List<Job>> = withContext(Dispatchers.IO) {
        try {
            val api = ApiClientProvider.getService(context)
            val response = api.getJobs(
                search = filter.query.ifBlank { null },
                workMode = filter.workMode,
                jobType = filter.jobType,
                experienceLevel = filter.experienceLevel,
                minScore = if (filter.minRadarScore > 0) filter.minRadarScore else null,
                minSalary = if (filter.minSalary > 0) filter.minSalary else null
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                // Fallback to sample jobs if server returns error or empty
                val fallback = ApiClientProvider.sampleJobs.filter { job ->
                    val matchesQuery = filter.query.isBlank() ||
                            job.title.contains(filter.query, ignoreCase = true) ||
                            job.company.contains(filter.query, ignoreCase = true) ||
                            job.skills.any { it.contains(filter.query, ignoreCase = true) }
                    val matchesWorkMode = filter.workMode == null || job.workMode.equals(filter.workMode, ignoreCase = true)
                    val matchesJobType = filter.jobType == null || job.jobType.equals(filter.jobType, ignoreCase = true)
                    val matchesScore = filter.minRadarScore == 0 || job.radarMatchScore >= filter.minRadarScore
                    val matchesSalary = filter.minSalary == 0 || (job.salaryMin ?: 0) >= filter.minSalary * 1000

                    matchesQuery && matchesWorkMode && matchesJobType && matchesScore && matchesSalary
                }
                Result.success(fallback)
            }
        } catch (e: Exception) {
            // Local fallback
            val fallback = ApiClientProvider.sampleJobs
            Result.success(fallback)
        }
    }

    suspend fun getJobById(id: String): Result<Job> = withContext(Dispatchers.IO) {
        try {
            // Check if in saved jobs first
            val saved = savedJobDao.getSavedJobById(id)
            if (saved != null) {
                return@withContext Result.success(saved.toJob())
            }

            val api = ApiClientProvider.getService(context)
            val response = api.getJobById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val sample = ApiClientProvider.sampleJobs.find { it.id == id }
                    ?: ApiClientProvider.sampleJobs.first()
                Result.success(sample)
            }
        } catch (e: Exception) {
            val sample = ApiClientProvider.sampleJobs.find { it.id == id }
                ?: ApiClientProvider.sampleJobs.first()
            Result.success(sample)
        }
    }

    suspend fun fetchNotifications(): Result<List<NotificationItem>> = withContext(Dispatchers.IO) {
        try {
            val api = ApiClientProvider.getService(context)
            val response = api.getNotifications()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(ApiClientProvider.sampleNotifications)
            }
        } catch (e: Exception) {
            Result.success(ApiClientProvider.sampleNotifications)
        }
    }
}
