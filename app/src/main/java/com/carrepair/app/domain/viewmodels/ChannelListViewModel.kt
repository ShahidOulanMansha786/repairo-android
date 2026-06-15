package com.carrepair.app.domain.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.carrepair.app.data.apis.AuthApi
import com.carrepair.app.data.repository.ChatChannel
import com.carrepair.app.data.repository.FirestoreChatRepository
import com.carrepair.app.domain.security.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChannelListViewModel(
    private val repository: FirestoreChatRepository,
    private val tokenManager: TokenManager,
    private val authApi: AuthApi
) : ViewModel() {

    private val _channels = MutableStateFlow<List<ChatChannel>>(emptyList())
    val channels: StateFlow<List<ChatChannel>> = _channels

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadChannels() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = "Bearer ${tokenManager.getAccessToken()}"
                val response = authApi.getMe(token)
                if (!response.isSuccessful) {
                    _isLoading.value = false
                    return@launch
                }
                val userId = response.body()?.id?.toString() ?: run {
                    _isLoading.value = false
                    return@launch
                }
                _currentUserId.value = userId
                repository.getUserChannelsFlow(userId).collect { channels ->
                    _channels.value = channels
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
}

class ChannelListViewModelFactory(
    private val repository: FirestoreChatRepository,
    private val tokenManager: TokenManager,
    private val authApi: AuthApi
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChannelListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChannelListViewModel(repository, tokenManager, authApi) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}