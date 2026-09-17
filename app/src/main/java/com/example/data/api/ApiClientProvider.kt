package com.example.data.api

import android.content.Context
import com.example.data.model.Job
import com.example.data.model.NotificationItem
import com.example.data.model.UserProfile
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClientProvider {

    private const val PREFS_NAME = "career_radar_api_prefs"
    private const val KEY_BASE_URL = "fastapi_base_url"
    private const val KEY_AUTH_TOKEN = "fastapi_auth_token"
    private const val KEY_USE_MOCK_FALLBACK = "use_mock_fallback"

    const val PROD_CLOUD_URL = "https://career-radar-2fx3.onrender.com/"
    const val CUSTOM_DOMAIN_URL = "https://career.raghavendragolla.com/"
    const val LAN_API_URL = "http://192.168.0.101:8000/"
    const val EMULATOR_API_URL = "http://10.0.2.2:8000/"
    const val DEFAULT_API_URL = PROD_CLOUD_URL

    private var cachedBaseUrl: String? = null
    private var cachedToken: String? = null
    private var cachedService: FastApiService? = null

    val sampleJobs = listOf(
        Job(
            id = "job_radar_01",
            title = "Data Analyst (Analytics & Reporting)",
            company = "Flipkart",
            location = "Bengaluru, India (Remote)",
            workMode = "Remote / Hybrid",
            jobType = "Full-time",
            experienceLevel = "Senior",
            salaryMin = 165000,
            salaryMax = 210000,
            radarMatchScore = 98,
            matchReasons = listOf(
                "Direct match for Python, SQL & Power BI",
                "Aligned with MSc Data Science & Analytics background",
                "Target role: Data Analyst"
            ),
            skills = listOf("Python", "SQL", "Power BI", "Excel", "Pandas", "AWS"),
            description = "We are seeking a high-performing Senior Android Engineer to pioneer next-generation mobile experiences. You will collaborate directly with our AI teams to build responsive, reactive interfaces using Jetpack Compose, Kotlin Coroutines, and robust local persistence. Strong background in modern architecture and performance tuning is desired.",
            postedTime = "2 hours ago",
            applicationUrl = "https://example.com/jobs/apex-systems-01",
            isHot = true,
            skillsMatchPct = 99,
            experienceFitPct = 95,
            salaryFitPct = 100,
            locationFitPct = 100
        ),
        Job(
            id = "job_radar_02",
            title = "BI Analyst (Dashboards & Insights)",
            company = "Swiggy",
            location = "Hyderabad, India",
            workMode = "Remote / Hybrid",
            jobType = "Full-time",
            experienceLevel = "Mid-Senior",
            salaryMin = 150000,
            salaryMax = 190000,
            radarMatchScore = 95,
            matchReasons = listOf(
                "Direct match for SQL queries & interactive Power BI dashboards",
                "Strong fit for KPI reporting and business intelligence",
                "Fully remote US/Canada"
            ),
            skills = listOf("SQL", "Power BI", "Excel", "Statistics", "Python"),
            description = "Swiggy is building agentic toolchains for enterprise search and reasoning. You will architect high-throughput FastAPI async microservices and craft client applications. If you love building end-to-end solutions that connect Python backends with fluid client interfaces, this role is for you.",
            postedTime = "5 hours ago",
            applicationUrl = "https://example.com/jobs/hyperscale-02",
            isHot = true,
            skillsMatchPct = 96,
            experienceFitPct = 92,
            salaryFitPct = 98,
            locationFitPct = 100
        ),
        Job(
            id = "job_radar_03",
            title = "Staff Mobile Platform Engineer",
            company = "Strata Cloud Networks",
            location = "Seattle, WA",
            workMode = "Hybrid",
            jobType = "Full-time",
            experienceLevel = "Lead",
            salaryMin = 185000,
            salaryMax = 235000,
            radarMatchScore = 91,
            matchReasons = listOf(
                "High match for architectural leadership and mobile SDK design",
                "Substantial equity and competitive tier-1 compensation"
            ),
            skills = listOf("Kotlin", "Modularization", "Network Protocols", "Security", "FastAPI"),
            description = "Lead the architecture of our core developer platforms. You will establish coding conventions, build reusable UI component libraries, optimize network payloads between mobile clients and internal FastAPI telemetry services, and mentor engineering teams across global pods.",
            postedTime = "1 day ago",
            applicationUrl = "https://example.com/jobs/strata-03",
            isHot = false,
            skillsMatchPct = 92,
            experienceFitPct = 90,
            salaryFitPct = 100,
            locationFitPct = 80
        ),
        Job(
            id = "job_radar_04",
            title = "Android & Python Backend Engineer",
            company = "Pulse Health Technologies",
            location = "Austin, TX",
            workMode = "Remote / Hybrid",
            jobType = "Full-time",
            experienceLevel = "Mid",
            salaryMin = 135000,
            salaryMax = 165000,
            radarMatchScore = 89,
            matchReasons = listOf(
                "FastAPI + Kotlin matching your core stack",
                "Health telemetry sensors and real-time alerts"
            ),
            skills = listOf("Kotlin", "FastAPI", "Bluetooth LE", "AsyncIO", "Material 3"),
            description = "Pulse is transforming personal health monitoring. We need an engineer proficient in Android Jetpack Compose to visualize real-time biomarker feeds received from wearable devices and integrate with our scalable FastAPI ingestion cluster.",
            postedTime = "2 days ago",
            applicationUrl = "https://example.com/jobs/pulse-04",
            isHot = false,
            skillsMatchPct = 90,
            experienceFitPct = 88,
            salaryFitPct = 85,
            locationFitPct = 100
        ),
        Job(
            id = "job_radar_05",
            title = "Lead Software Engineer - Client Architecture",
            company = "Vortex Data Systems",
            location = "Boston, MA",
            workMode = "On-site",
            jobType = "Full-time",
            experienceLevel = "Lead",
            salaryMin = 190000,
            salaryMax = 240000,
            radarMatchScore = 82,
            matchReasons = listOf(
                "Strong technical match, but on-site requirement in Boston"
            ),
            skills = listOf("Kotlin", "Compose", "Architecture", "CI/CD", "Performance"),
            description = "Drive technical excellence across our client teams. Own system design for ultra low-latency data rendering engines and mission-critical enterprise tools.",
            postedTime = "3 days ago",
            applicationUrl = "https://example.com/jobs/vortex-05",
            isHot = false,
            skillsMatchPct = 95,
            experienceFitPct = 96,
            salaryFitPct = 100,
            locationFitPct = 40
        ),
        Job(
            id = "job_radar_06",
            title = "Contract Mobile & API Specialist (FastAPI)",
            company = "Catalyst Studio",
            location = "Remote / Hybrid",
            workMode = "Remote / Hybrid",
            jobType = "Contract",
            experienceLevel = "Senior",
            salaryMin = 80000,
            salaryMax = 180000,
            radarMatchScore = 88,
            matchReasons = listOf(
                "Great match for contract/flexible duration with FastAPI backend",
                "Fast onboarding and 6-month extendable scope"
            ),
            skills = listOf("FastAPI", "Kotlin Compose", "REST API", "GitLab CI"),
            description = "6-month contract with possibility of extension to build a client-facing radar monitoring tool connecting directly to FastAPI data feeds.",
            postedTime = "1 day ago",
            applicationUrl = "https://example.com/jobs/catalyst-06",
            isHot = false,
            skillsMatchPct = 88,
            experienceFitPct = 88,
            salaryFitPct = 90,
            locationFitPct = 100
        )
    )

    val sampleNotifications = listOf(
        NotificationItem(
            id = "notif_01",
            title = "Radar Hit: 98% Match Detected!",
            message = "Flipkart posted 'Senior Android Engineer' matching your target salary & Kotlin skills.",
            jobId = "job_radar_01",
            radarScore = 98,
            timestamp = System.currentTimeMillis() - 3600000L,
            type = "RADAR_MATCH"
        ),
        NotificationItem(
            id = "notif_02",
            title = "New FastAPI Job Alert",
            message = "Swiggy is looking for BI Analyst (Dashboards & Insights).",
            jobId = "job_radar_02",
            radarScore = 95,
            timestamp = System.currentTimeMillis() - 7200000L,
            type = "RADAR_MATCH"
        ),
        NotificationItem(
            id = "notif_03",
            title = "Career Radar Daily Digest",
            message = "6 new matching jobs scanned in the last 24 hours matching your profile.",
            jobId = null,
            radarScore = 91,
            timestamp = System.currentTimeMillis() - 86400000L,
            type = "DIGEST"
        )
    )

    fun getBaseUrl(context: Context): String {
        if (cachedBaseUrl == null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val defaultUrl = PROD_CLOUD_URL
            cachedBaseUrl = prefs.getString(KEY_BASE_URL, defaultUrl) ?: defaultUrl
        }
        return cachedBaseUrl!!
    }

    fun setBaseUrl(context: Context, url: String) {
        val normalized = if (!url.endsWith("/")) "$url/" else url
        cachedBaseUrl = normalized
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_BASE_URL, normalized)
            .apply()
        cachedService = null
    }

    fun getAuthToken(context: Context): String? {
        if (cachedToken == null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            cachedToken = prefs.getString(KEY_AUTH_TOKEN, null)
        }
        return cachedToken
    }

    fun setAuthToken(context: Context, token: String?) {
        cachedToken = token
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_AUTH_TOKEN, token)
            .apply()
        cachedService = null
    }

    fun isMockFallbackEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_USE_MOCK_FALLBACK, false)
    }

    fun setMockFallbackEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_USE_MOCK_FALLBACK, enabled)
            .apply()
        cachedService = null
    }

    fun getService(context: Context): FastApiService {
        cachedService?.let { return it }

        val baseUrl = getBaseUrl(context)
        val token = getAuthToken(context)
        val mockFallback = isMockFallbackEnabled(context)

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()
            if (!token.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $token")
            }
            builder.header("Accept", "application/json")
            chain.proceed(builder.build())
        }

        val fallbackInterceptor = Interceptor { chain ->
            val request = chain.request()
            try {
                val response = chain.proceed(request)
                if (response.isSuccessful || !mockFallback) {
                    return@Interceptor response
                }
                // If 404/500 and mockFallback enabled, fallback to mock response
                handleMockResponse(request, moshi)
            } catch (e: Exception) {
                if (mockFallback) {
                    handleMockResponse(request, moshi)
                } else {
                    throw e
                }
            }
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(fallbackInterceptor)
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val service = retrofit.create(FastApiService::class.java)
        cachedService = service
        return service
    }

    private fun handleMockResponse(request: okhttp3.Request, moshi: Moshi): Response {
        val path = request.url.encodedPath
        val method = request.method
        val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

        val bodyString = when {
            path.endsWith("auth/login") && method == "POST" -> {
                """
                {
                  "access_token": "radar_jwt_token_demo_sample_98823",
                  "token_type": "bearer",
                  "user": {
                    "id": "usr_radar_raghavendra",
                    "username": "radar_pilot",
                    "email": "raghavendrayadavgolla@gmail.com",
                    "full_name": "Raghavendra Golla",
                    "target_role": "Data Analyst / BI & ML Analyst",
                    "radar_skills": ["Python", "SQL", "Power BI", "Excel", "Pandas", "Scikit-learn", "NumPy", "Statistics", "Machine Learning"],
                    "target_min_salary": 80000,
                    "preferred_work_mode": "Remote / Hybrid"
                  }
                }
                """.trimIndent()
            }
            path.endsWith("users/me") && method == "GET" -> {
                """
                {
                  "id": "usr_radar_raghavendra",
                  "username": "radar_pilot",
                  "email": "raghavendrayadavgolla@gmail.com",
                  "full_name": "Raghavendra Golla",
                  "target_role": "Data Analyst / BI & ML Analyst",
                  "radar_skills": ["Python", "SQL", "Power BI", "Excel", "Pandas", "Scikit-learn", "NumPy", "Statistics", "Machine Learning"],
                  "target_min_salary": 80000,
                  "preferred_work_mode": "Remote / Hybrid"
                }
                """.trimIndent()
            }
            path.contains("jobs") && method == "GET" && !path.matches(Regex(".*/jobs/[^/]+")) -> {
                val adapter = moshi.adapter(List::class.java)
                // Filter by query params if present
                val searchQuery = request.url.queryParameter("search")?.lowercase()
                val workModeParam = request.url.queryParameter("work_mode")?.lowercase()
                val jobTypeParam = request.url.queryParameter("job_type")?.lowercase()
                val minScoreParam = request.url.queryParameter("min_score")?.toIntOrNull()
                val minSalaryParam = request.url.queryParameter("min_salary")?.toIntOrNull()

                var filtered = sampleJobs
                if (!searchQuery.isNullOrBlank()) {
                    filtered = filtered.filter {
                        it.title.lowercase().contains(searchQuery) ||
                        it.company.lowercase().contains(searchQuery) ||
                        it.skills.any { s -> s.lowercase().contains(searchQuery) } ||
                        it.location.lowercase().contains(searchQuery)
                    }
                }
                if (!workModeParam.isNullOrBlank()) {
                    filtered = filtered.filter { it.workMode.lowercase() == workModeParam }
                }
                if (!jobTypeParam.isNullOrBlank()) {
                    filtered = filtered.filter { it.jobType.lowercase() == jobTypeParam }
                }
                if (minScoreParam != null && minScoreParam > 0) {
                    filtered = filtered.filter { it.radarMatchScore >= minScoreParam }
                }
                if (minSalaryParam != null && minSalaryParam > 0) {
                    filtered = filtered.filter { (it.salaryMin ?: 0) >= minSalaryParam * 1000 }
                }

                moshi.adapter(Any::class.java).toJson(filtered)
            }
            path.matches(Regex(".*/jobs/[^/]+")) && method == "GET" -> {
                val id = path.substringAfterLast("/")
                val job = sampleJobs.find { it.id == id } ?: sampleJobs.first()
                moshi.adapter(Job::class.java).toJson(job)
            }
            path.contains("/save") -> {
                """{"success": true}"""
            }
            path.contains("notifications") && method == "GET" -> {
                moshi.adapter(Any::class.java).toJson(sampleNotifications)
            }
            else -> {
                """{"status": "ok"}"""
            }
        }

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK (FastAPI Engine)")
            .body(bodyString.toResponseBody(jsonMediaType))
            .build()
    }
}
