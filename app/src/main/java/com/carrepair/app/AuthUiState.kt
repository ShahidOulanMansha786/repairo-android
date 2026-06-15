package com.carrepair.app


sealed class AuthUiState {

    // Nothing happening — initial state after screen loads or after reset
    object Idle : AuthUiState()

    // API call is in progress — show spinner
    object Loading : AuthUiState()

    // API call succeeded — carry the message from backend to show in UI
    data class Success(val message: String) : AuthUiState()

    // API call failed — carry the error message to show in UI
    data class Error(val message: String) : AuthUiState()
}
