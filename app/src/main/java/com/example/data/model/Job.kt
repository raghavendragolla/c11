package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Job(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "company") val company: String,
    @Json(name = "location") val location: String,
    @Json(name = "work_mode") val workMode: String = "Remote", // Remote, Hybrid, On-site
    @Json(name = "job_type") val jobType: String = "Full-time", // Full-time, Contract, Part-time
    @Json(name = "experience_level") val experienceLevel: String = "Mid-Senior", // Entry, Mid, Senior, Lead
    @Json(name = "salary_min") val salaryMin: Int? = null,
    @Json(name = "salary_max") val salaryMax: Int? = null,
    @Json(name = "salary_currency") val salaryCurrency: String = "USD",
    @Json(name = "radar_match_score") val radarMatchScore: Int = 85, // 0 to 100
    @Json(name = "match_reasons") val matchReasons: List<String> = emptyList(),
    @Json(name = "skills") val skills: List<String> = emptyList(),
    @Json(name = "description") val description: String = "",
    @Json(name = "posted_time") val postedTime: String = "2d ago",
    @Json(name = "application_url") val applicationUrl: String = "https://example.com/apply",
    @Json(name = "company_logo") val companyLogo: String? = null,
    @Json(name = "is_hot") val isHot: Boolean = false,
    @Json(name = "skills_match_pct") val skillsMatchPct: Int = 90,
    @Json(name = "experience_fit_pct") val experienceFitPct: Int = 85,
    @Json(name = "salary_fit_pct") val salaryFitPct: Int = 95,
    @Json(name = "location_fit_pct") val locationFitPct: Int = 100
) {
    val salaryFormatted: String
        get() {
            return when {
                salaryMin != null && salaryMax != null -> "$${salaryMin / 1000}k - $${salaryMax / 1000}k/yr"
                salaryMin != null -> "From $${salaryMin / 1000}k/yr"
                salaryMax != null -> "Up to $${salaryMax / 1000}k/yr"
                else -> "Competitive Salary"
            }
        }
}

data class JobFilter(
    val query: String = "",
    val workMode: String? = null, // "Remote", "Hybrid", "On-site"
    val jobType: String? = null, // "Full-time", "Contract", "Part-time"
    val experienceLevel: String? = null, // "Entry", "Mid", "Senior", "Lead"
    val minRadarScore: Int = 0, // 0 to 95
    val minSalary: Int = 0, // in thousands (e.g. 100 = $100k)
    val skillsQuery: String = ""
) {
    val activeFilterCount: Int
        get() {
            var count = 0
            if (!workMode.isNullOrEmpty()) count++
            if (!jobType.isNullOrEmpty()) count++
            if (!experienceLevel.isNullOrEmpty()) count++
            if (minRadarScore > 0) count++
            if (minSalary > 0) count++
            if (skillsQuery.isNotEmpty()) count++
            return count
        }
}
