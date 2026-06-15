package com.carrepair.app.domain.viewmodels.quotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.carrepair.app.data.apis.LeadApi
import com.carrepair.app.data.dto.jobtracking.AcceptedLeadDetailDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
class AcceptedLeadDetailViewModel(
    private val leadApi: LeadApi,
    private val leadId: Long
) : ViewModel() {

    private val _detail = MutableStateFlow<AcceptedLeadDetailDto?>(null)
    val detail: StateFlow<AcceptedLeadDetailDto?> = _detail

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchDetail()
    }

    fun fetchDetail() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = leadApi.getAcceptedLeadDetail(leadId)
                if (res.isSuccessful) _detail.value = res.body()
                else _error.value = "Failed to load lead detail"
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class AcceptedLeadDetailViewModelFactory(
    private val leadApi: LeadApi,
    private val leadId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AcceptedLeadDetailViewModel(leadApi, leadId) as T
    }
}