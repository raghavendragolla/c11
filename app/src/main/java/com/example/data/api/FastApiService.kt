package com.example.data.api

import com.example.data.model.Job
import com.example.data.model.LoginRequest
import com.example.data.model.NotificationItem
import com.example.data.model.TokenResponse
import com.example.data.model.UserProfile
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FastApiService {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<TokenResponse>

    @GET("users/me")
    suspend fun getCurrentUser(): Response<UserProfile>

    @GET("jobs")
    suspend fun getJobs(
        @Query("search") search: String? = null,
        @Query("work_mode") workMode: String? = null,
        @Query("job_type") jobType: String? = null,
        @Query("experience_level") experienceLevel: String? = null,
        @Query("min_score") minScore: Int? = null,
        @Query("min_salary") minSalary: Int? = null,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 50
    ): Response<List<Job>>

    @GET("jobs/{id}")
    suspend fun getJobById(
        @Path("id") id: String
    ): Response<Job>

    @POST("jobs/{id}/save")
    suspend fun saveJobRemote(
        @Path("id") id: String
    ): Response<Map<String, Boolean>>

    @DELETE("jobs/{id}/save")
    suspend fun unsaveJobRemote(
        @Path("id") id: String
    ): Response<Map<String, Boolean>>

    @GET("notifications")
    suspend fun getNotifications(): Response<List<NotificationItem>>

    @POST("notifications/register-token")
    suspend fun registerPushToken(
        @Body tokenPayload: Map<String, String>
    ): Response<Map<String, String>>

    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body request: Map<String, String>
    ): Response<Map<String, Any>>
}
