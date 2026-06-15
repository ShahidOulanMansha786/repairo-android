package com.carrepair.app.domain.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.carrepair.app.data.apis.LeadApi
import com.carrepair.app.data.dto.jobtracking.AcceptedLeadDetailDto
import com.carrepair.app.data.dto.jobtracking.JobProgressDto
import com.carrepair.app.data.dto.lead.LeadResponseDto
import com.carrepair.app.domain.security.TokenManager
import kotlinx.coroutines.launch
class LeadDetailViewModel(
    private val leadApi: LeadApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    var lead by mutableStateOf<LeadResponseDto?>(null)
    var isLoading by mutableStateOf(true)
    var jobSteps by mutableStateOf<List<JobProgressDto>>(emptyList())
    var error by mutableStateOf<String?>(null)

    fun loadLead(leadId: Long) {
        viewModelScope.launch {
            isLoading = true
            try {
                val token = "Bearer " + tokenManager.getAccessToken()
                val response = leadApi.getLeadById(token, leadId)
                if (response.isSuccessful) {
                    lead = response.body()
                }
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun loadJobProgress(leadId: Long) {
        viewModelScope.launch {
            try {
                val token = "Bearer " + tokenManager.getAccessToken()
                val response = leadApi.getJobProgress(leadId)
                if (response.isSuccessful) {
                    jobSteps = response.body() ?: emptyList()
                }
            } catch (e: Exception) { }
        }
    }

    fun cancelLead(leadId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val token = "Bearer " + tokenManager.getAccessToken()
                val response = leadApi.cancelLead(token, leadId)
                if (response.isSuccessful) {
                    lead = response.body()
                    onSuccess()
                } else {
                    onError("Failed to cancel lead")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error")
            }
        }
    }

    fun refreshAll(leadId: Long) {
        loadLead(leadId)
        loadJobProgress(leadId)
    }
}

class LeadDetailViewModelFactory(
    private val leadApi: LeadApi,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return LeadDetailViewModel(leadApi, tokenManager) as T
    }
}