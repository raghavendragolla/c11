package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)

    val currentUser: StateFlow<UserProfile?> = repository.currentUser
    val isLoggedIn: StateFlow<Boolean> = repository.isLoggedIn

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _serverUrl = MutableStateFlow(repository.getServerUrl())
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _isMockFallback = MutableStateFlow(repository.isMockEnabled())
    val isMockFallback: StateFlow<Boolean> = _isMockFallback.asStateFlow()

    fun login(username: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.login(username, pass)
            result.onFailure {
                _errorMessage.value = it.message ?: "Authentication failed"
            }
            _isLoading.value = false
        }
    }

    fun resetPassword(username: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.resetPassword(username, newPass)
            result.onSuccess { msg ->
                _isLoading.value = false
                onResult(true, msg)
            }.onFailure { err ->
                _isLoading.value = false
                val msg = err.message ?: "Failed to reset password"
                _errorMessage.value = msg
                onResult(false, msg)
            }
        }
    }

    fun quickDemoLogin() {
        login("radar_pilot", "demo1234")
    }

    fun logout() {
        repository.logout()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun updateServerUrl(url: String) {
        repository.setServerUrl(url)
        _serverUrl.value = repository.getServerUrl()
    }

    fun setMockFallback(enabled: Boolean) {
        repository.setMockEnabled(enabled)
        _isMockFallback.value = enabled
    }
}
