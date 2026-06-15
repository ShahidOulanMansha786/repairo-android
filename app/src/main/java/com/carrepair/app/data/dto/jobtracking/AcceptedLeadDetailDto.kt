package com.carrepair.app.data.dto.jobtracking

data class AcceptedLeadDetailDto(
    val leadId: Long,
    val title: String,
    val description: String,
    val carMake: String,
    val carModel: String,
    val carYear: Int,
    val address: String,
    val imageUrls: List<String>,
    val customerContact: CustomerContactDto
)