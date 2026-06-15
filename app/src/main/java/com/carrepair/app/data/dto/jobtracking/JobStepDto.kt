package com.carrepair.app.data.dto.jobtracking

data class JobProgressDto(
    val step: String,
    val completed: Boolean,
    val completedAt: String?,
    val metadata: String?
)