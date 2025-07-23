package com.nerikpaul.geopulsetracker.network

import com.nerikpaul.geopulsetracker.data.LocationRequest
import com.nerikpaul.geopulsetracker.data.LocationResponse
import com.nerikpaul.geopulsetracker.data.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface LocationApiService {
    @POST("locations")
    suspend fun sendLocation(
        @Header("Authorization") token: String,
        @Body location: LocationRequest
    ): Response<ApiResponse<LocationResponse>>
}
