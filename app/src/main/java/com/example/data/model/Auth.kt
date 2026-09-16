package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = username) val username: String,
    @Json(name = password) val password: String
)

@JsonClass(generateAdapter = true)
data class TokenResponse(
    @Json(name = access_token) val accessToken: String,
    @Json(name = token_type) val tokenType: String = bearer,
    @Json(name = user) val user: UserProfile? = null
)

@JsonClass(generateAdapter = true)
data class UserProfile(
    @Json(name = id) val id: String = usr_radar_raghavendra,
    @Json(name = username) val username: String = raghavendra,
    @Json(name = email) val email: String = raghavendrayadavgolla@gmail.com,
    @Json(name = full_name) val fullName: String = Raghavendra Golla,
    @Json(name = target_role) val targetRole: String = Data Analyst / BI & ML Analyst,
    @Json(name = radar_skills) val radarSkills: List<String> = listOf(Python, SQL, Power BI, Excel, Pandas, NumPy, Statistics, Machine Learning, Scikit-learn),
    @Json(name = target_min_salary) val targetMinSalary: Int = 80000,
    @Json(name = preferred_work_mode) val preferredWorkMode: String = Remote / Hybrid
)
