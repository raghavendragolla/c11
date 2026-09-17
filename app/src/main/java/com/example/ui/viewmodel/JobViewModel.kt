package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.SavedJobEntity
import com.example.data.model.Job
import com.example.data.model.JobFilter
import com.example.data.model.NotificationItem
import com.example.data.repository.JobRepository
import com.example.notifications.PushNotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JobViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = JobRepository(application)

    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _filter = MutableStateFlow(JobFilter())
    val filter: StateFlow<JobFilter> = _filter.asStateFlow()

    private val _selectedJob = MutableStateFlow<Job?>(null)
    val selectedJob: StateFlow<Job?> = _selectedJob.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _pushNotificationsEnabled = MutableStateFlow(
        PushNotificationHelper.isPushNotificationsEnabled(application)
    )
    val pushNotificationsEnabled: StateFlow<Boolean> = _pushNotificationsEnabled.asStateFlow()

    private val _minNotificationScore = MutableStateFlow(
        PushNotificationHelper.getMinNotificationScore(application)
    )
    val minNotificationScore: StateFlow<Int> = _minNotificationScore.asStateFlow()

    val savedJobs: StateFlow<List<SavedJobEntity>> = repository.savedJobs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        PushNotificationHelper.createNotificationChannels(application)
        loadJobs()
        loadNotifications()
    }

    fun loadJobs() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.fetchJobs(_filter.value)
            result.onSuccess { fetchedJobs ->
                _jobs.value = fetchedJobs
                checkForNewJobNotifications(fetchedJobs)
            }
            _isLoading.value = false
        }
    }

    private fun checkForNewJobNotifications(freshJobs: List<Job>) {
        if (freshJobs.isEmpty()) return
        val prefs = getApplication<Application>().getSharedPreferences("career_radar_seen_jobs", android.content.Context.MODE_PRIVATE)
        val seenIds = prefs.getStringSet("seen_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        val isFirstLaunch = seenIds.isEmpty()

        val threshold = _minNotificationScore.value
        val newHighMatches = freshJobs.filter { job ->
            (!seenIds.contains(job.id) || isFirstLaunch) && job.radarMatchScore >= threshold
        }

        seenIds.addAll(freshJobs.map { it.id })
        prefs.edit().putStringSet("seen_ids", seenIds).apply()

        if (newHighMatches.isNotEmpty()) {
            val topMatches = newHighMatches.sortedByDescending { it.radarMatchScore }.take(3)
            for (match in topMatches) {
                PushNotificationHelper.sendJobMatchNotification(getApplication(), match)
                val newItem = NotificationItem(
                    id = "notif_${match.id}_${System.currentTimeMillis()}",
                    title = "🎯 Radar Hit: ${match.radarMatchScore}% Match",
                    message = "${match.title} at ${match.company} (${match.workMode})",
                    jobId = match.id,
                    radarScore = match.radarMatchScore,
                    timestamp = System.currentTimeMillis(),
                    type = "RADAR_MATCH"
                )
                if (_notifications.value.none { it.jobId == match.id }) {
                    _notifications.value = listOf(newItem) + _notifications.value
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _filter.value = _filter.value.copy(query = query)
        loadJobs()
    }

    fun updateFilter(newFilter: JobFilter) {
        _filter.value = newFilter
        loadJobs()
    }

    fun clearFilters() {
        _filter.value = JobFilter(query = _filter.value.query)
        loadJobs()
    }

    fun selectJobById(jobId: String) {
        viewModelScope.launch {
            val result = repository.getJobById(jobId)
            result.onSuccess {
                _selectedJob.value = it
            }
        }
    }

    fun clearSelectedJob() {
        _selectedJob.value = null
    }

    fun toggleSaveJob(job: Job) {
        viewModelScope.launch {
            val isCurrentlySaved = savedJobs.value.any { it.id == job.id }
            if (isCurrentlySaved) {
                repository.unsaveJob(job.id)
            } else {
                repository.saveJob(job)
            }
        }
    }

    fun updateJobNotes(jobId: String, notes: String) {
        viewModelScope.launch {
            repository.updateJobNotes(jobId, notes)
        }
    }

    fun deleteSavedJob(jobId: String) {
        viewModelScope.launch {
            repository.unsaveJob(jobId)
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            val result = repository.fetchNotifications()
            result.onSuccess {
                _notifications.value = it
            }
        }
    }

    fun triggerJobNotification(job: Job) {
        PushNotificationHelper.sendJobMatchNotification(getApplication(), job)
        // Add to in-app notification list as well
        val newItem = NotificationItem(
            id = "notif_${System.currentTimeMillis()}",
            title = "Radar Hit: ${job.radarMatchScore}% Match",
            message = "${job.title} at ${job.company}",
            jobId = job.id,
            radarScore = job.radarMatchScore,
            timestamp = System.currentTimeMillis(),
            type = "RADAR_MATCH"
        )
        _notifications.value = listOf(newItem) + _notifications.value
    }

    fun triggerCustomPushTest() {
        val topMatches = _jobs.value
            .filter { it.radarMatchScore >= _minNotificationScore.value }
            .ifEmpty { _jobs.value }
            .sortedByDescending { it.radarMatchScore }
            .take(3)

        if (topMatches.isNotEmpty()) {
            topMatches.forEach { job ->
                triggerJobNotification(job)
            }
        } else {
            PushNotificationHelper.sendCustomNotification(
                getApplication(),
                "Career Radar Active",
                "Radar background scanner is actively monitoring FastAPI job feeds."
            )
        }
    }

    fun setPushNotificationsEnabled(enabled: Boolean) {
        PushNotificationHelper.setPushNotificationsEnabled(getApplication(), enabled)
        _pushNotificationsEnabled.value = enabled
    }

    fun setMinNotificationScore(score: Int) {
        PushNotificationHelper.setMinNotificationScore(getApplication(), score)
        _minNotificationScore.value = score
    }
}
