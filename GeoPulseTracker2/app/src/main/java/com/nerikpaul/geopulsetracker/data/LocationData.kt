package com.nerikpaul.geopulsetracker.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LocationRequest(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val address: String? = null,
    val timestamp: String
)

@JsonClass(generateAdapter = true)
data class LocationResponse(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float?,
    val address: String?,
    val timestamp: String,
    val userId: String
)

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)
