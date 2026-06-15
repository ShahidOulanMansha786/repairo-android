package com.carrepair.app.domain.viewmodels


import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.carrepair.app.data.apis.AuthApi
import com.carrepair.app.data.apis.LeadApi
import com.carrepair.app.data.repository.ChatChannel
import com.carrepair.app.data.repository.ChatMessage
import com.carrepair.app.data.repository.FirestoreChatRepository
import com.carrepair.app.domain.security.TokenManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SendUiState {
    object Idle : SendUiState()
    object Sending : SendUiState()
    data class Error(val message: String) : SendUiState()
}

class ChatViewModel(
    private val repository: FirestoreChatRepository,
    private val tokenManager: TokenManager,
    private val authApi: AuthApi,
    private val leadApi: LeadApi
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _channelInfo = MutableStateFlow<ChatChannel?>(null)
    val channelInfo: StateFlow<ChatChannel?> = _channelInfo

    private val _sendUiState = MutableStateFlow<SendUiState>(SendUiState.Idle)
    val sendUiState: StateFlow<SendUiState> = _sendUiState

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId
    private val _currentUserName = MutableStateFlow<String?>(null)

    private val _channelId = MutableStateFlow<String?>(null)
    val channelId: StateFlow<String?> = _channelId

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var messagesJob: Job? = null

    fun init(channelId: String) {
        viewModelScope.launch {
            try {
                val token = "Bearer ${tokenManager.getAccessToken()}"
                val response = authApi.getMe(token)
                if (response.isSuccessful) {
                    val user = response.body()
                    _currentUserId.value = user?.id?.toString()
                    _currentUserName.value = user?.fullName
                } else {
                    return@launch
                }
            } catch (e: Exception) {
                return@launch
            }

            messagesJob = viewModelScope.launch {
                repository.getMessagesFlow(channelId).collect { msgs ->
                    _messages.value = msgs
                    val userId = _currentUserId.value ?: return@collect
                    try {
                        repository.markMessagesAsRead(channelId, userId)
                    } catch (e: Exception) {
                        // silently ignore read receipt failures
                    }
                }
            }
        }
    }

    fun sendText(channelId: String, text: String) {
        if (text.isBlank()) return
        val userId = _currentUserId.value ?: return
        val userName = _currentUserName.value ?: return
        viewModelScope.launch {
            _sendUiState.value = SendUiState.Sending
            try {
                repository.sendTextMessage(channelId, userId, userName, text)
                _sendUiState.value = SendUiState.Idle
            } catch (e: Exception) {
                _sendUiState.value = SendUiState.Error(e.message ?: "Failed to send message")
            }
        }
    }

    fun sendImage(channelId: String, imageUri: Uri, context: Context) {
        val userId = _currentUserId.value ?: return
        val userName = _currentUserName.value ?: return
        viewModelScope.launch {
            _sendUiState.value = SendUiState.Sending
            try {
                repository.sendImageMessage(channelId, userId, userName, imageUri, context)
                _sendUiState.value = SendUiState.Idle
            } catch (e: Exception) {
                _sendUiState.value = SendUiState.Error(e.message ?: "Failed to send image")
            }
        }
    }

    fun loadChannel(leadId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val token = "Bearer ${tokenManager.getAccessToken()}"
                val response = leadApi.getChatChannel( leadId)
                if (response.isSuccessful) {
                    _channelId.value = response.body()?.channelId
                } else {
                    _error.value = "Failed to load chat channel"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Something went wrong"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun reset() {
        _channelId.value = null
        _error.value = null
        _isLoading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        messagesJob?.cancel()
    }
}

class ChatViewModelFactory(
    private val repository: FirestoreChatRepository,
    private val tokenManager: TokenManager,
    private val authApi: AuthApi,
    private val leadApi: LeadApi
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository, tokenManager, authApi, leadApi) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}