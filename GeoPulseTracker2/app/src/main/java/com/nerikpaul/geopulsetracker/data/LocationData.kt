package com.nerikpaul.geopulsetracker.data

data class LocationRequest(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val address: String? = null,
    val timestamp: String
)

data class LocationResponse(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float?,
    val address: String?,
    val timestamp: String,
    val userId: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)
