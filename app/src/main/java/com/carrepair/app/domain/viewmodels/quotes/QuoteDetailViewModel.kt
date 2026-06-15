package com.carrepair.app.domain.viewmodels.quotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.carrepair.app.data.apis.LeadApi
import com.carrepair.app.data.dto.jobtracking.JobProgressDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuoteDetailViewModel(
    private val leadApi: LeadApi,
    private val leadId: Long
) : ViewModel() {

    private val _progress = MutableStateFlow<List<JobProgressDto>>(emptyList())
    val progress: StateFlow<List<JobProgressDto>> = _progress

    private val _markDoneSuccess = MutableStateFlow(false)
    val markDoneSuccess: StateFlow<Boolean> = _markDoneSuccess

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _channelId = MutableStateFlow<String?>(null)
    val channelId: StateFlow<String?> = _channelId

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _showSuccessAnimation = MutableStateFlow(false)
    val showSuccessAnimation: StateFlow<Boolean> = _showSuccessAnimation

    init {
        fetchProgress()
    }

    fun fetchProgress() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = leadApi.getJobProgress(leadId)
                if (res.isSuccessful) _progress.value = res.body() ?: emptyList()
                else _error.value = "Failed to load progress"
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }



    fun fetchChatChannel() {
        viewModelScope.launch {
            try {
                val res = leadApi.getChatChannel(leadId)
                if (res.isSuccessful) _channelId.value = res.body()?.channelId
            } catch (e: Exception) { }
        }
    }
    fun markWorkDone() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = leadApi.markWorkDone(leadId)
                if (res.isSuccessful) {
                    _markDoneSuccess.value = true
                    _showSuccessAnimation.value = true
                    fetchProgress()
                } else _error.value = "Failed to mark work done"
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun dismissSuccessAnimation() {
        _showSuccessAnimation.value = false
    }

    fun resetChannelId() {
        _channelId.value = null
    }
}

class QuoteDetailViewModelFactory(
    private val leadApi: LeadApi,
    private val leadId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return QuoteDetailViewModel(leadApi, leadId) as T
    }
}