package com.example.data.repository

import android.content.Context
import com.example.data.api.ApiClientProvider
import com.example.data.model.LoginRequest
import com.example.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AuthRepository(private val context: Context) {

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        val existingToken = ApiClientProvider.getAuthToken(context)
        if (!existingToken.isNullOrBlank()) {
            _isLoggedIn.value = true
            _currentUser.value = UserProfile()
        }
    }

    suspend fun login(username: String, password: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val api = ApiClientProvider.getService(context)
            val response = api.login(LoginRequest(username = username, password = password))
            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                ApiClientProvider.setAuthToken(context, tokenResponse.accessToken)
                val user = tokenResponse.user ?: UserProfile(username = username)
                _currentUser.value = user
                _isLoggedIn.value = true
                Result.success(user)
            } else {
                // If mock fallback enabled
                if (ApiClientProvider.isMockFallbackEnabled(context)) {
                    val fallbackUser = UserProfile(username = username)
                    ApiClientProvider.setAuthToken(context, "mock_token_success")
                    _currentUser.value = fallbackUser
                    _isLoggedIn.value = true
                    Result.success(fallbackUser)
                } else {
                    Result.failure(Exception("Login failed: ${response.code()} ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            if (ApiClientProvider.isMockFallbackEnabled(context)) {
                val fallbackUser = UserProfile(username = username)
                ApiClientProvider.setAuthToken(context, "mock_token_success")
                _currentUser.value = fallbackUser
                _isLoggedIn.value = true
                Result.success(fallbackUser)
            } else {
                Result.failure(e)
            }
        }
    }

    fun logout() {
        ApiClientProvider.setAuthToken(context, null)
        _currentUser.value = null
        _isLoggedIn.value = false
    }

    fun getServerUrl(): String {
        return ApiClientProvider.getBaseUrl(context)
    }

    fun setServerUrl(url: String) {
        ApiClientProvider.setBaseUrl(context, url)
    }

    fun isMockEnabled(): Boolean {
        return ApiClientProvider.isMockFallbackEnabled(context)
    }

    fun setMockEnabled(enabled: Boolean) {
        ApiClientProvider.setMockFallbackEnabled(context, enabled)
    }
}
