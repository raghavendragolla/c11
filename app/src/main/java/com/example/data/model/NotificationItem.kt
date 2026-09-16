package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotificationItem(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "message") val message: String,
    @Json(name = "job_id") val jobId: String? = null,
    @Json(name = "radar_score") val radarScore: Int? = null,
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
    @Json(name = "is_read") val isRead: Boolean = false,
    @Json(name = "type") val type: String = "RADAR_MATCH" // RADAR_MATCH, APPLICATION_UPDATE, DIGEST
)
