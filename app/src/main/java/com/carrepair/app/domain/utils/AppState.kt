package com.carrepair.app.domain.utils

import kotlinx.coroutines.flow.MutableStateFlow

object AppState {
    val isBlocked = MutableStateFlow(false)
    val shouldNavigateToRoleSelection = MutableStateFlow(false)
}