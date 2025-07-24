package com.nerikpaul.geopulsetracker.repository

import android.util.Log
import com.nerikpaul.geopulsetracker.data.LocationRequest as ApiLocationRequest
import com.nerikpaul.geopulsetracker.network.LocationApiService
import com.nerikpaul.geopulsetracker.config.AppConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

/**
 * Repository for handling location data operations
 * Implements proper error handling, retry logic, and caching
 */
@Singleton
class LocationRepository @Inject constructor(
    private val locationApiService: LocationApiService
) {
    
    companion object {
        private const val TAG = "LocationRepository"
        private const val MAX_RETRY_ATTEMPTS = 3
        private val RETRY_DELAYS = listOf(2.seconds, 5.seconds, 10.seconds)
    }

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * Send location to server with automatic retry logic
     */
    suspend fun sendLocationWithRetry(
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        timestamp: Long
    ): Flow<Result<String>> = flow {
        emit(Result.Loading)
        
        val locationRequest = ApiLocationRequest(
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            timestamp = dateFormatter.format(Date(timestamp))
        )

        var lastException: Exception? = null
        
        repeat(MAX_RETRY_ATTEMPTS) { attempt ->
            try {
                val response = locationApiService.sendLocation(
                    "Bearer ${AppConfig.AUTH_TOKEN}",
                    locationRequest
                )

                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "Location sent successfully"
                    emit(Result.Success(message))
                    return@flow
                } else {
                    throw HttpException(response)
                }
                
            } catch (e: UnknownHostException) {
                lastException = e
                Log.w(TAG, "Network error on attempt ${attempt + 1}: Unable to resolve host")
                
                if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                    delay(RETRY_DELAYS[attempt])
                }
                
            } catch (e: IOException) {
                lastException = e
                Log.w(TAG, "I/O error on attempt ${attempt + 1}: ${e.message}")
                
                if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                    delay(RETRY_DELAYS[attempt])
                }
                
            } catch (e: HttpException) {
                // Don't retry on HTTP errors (4xx, 5xx)
                lastException = e
                Log.e(TAG, "HTTP error: ${e.code()} - ${e.message()}")
                break
                
            } catch (e: Exception) {
                lastException = e
                Log.e(TAG, "Unexpected error on attempt ${attempt + 1}", e)
                
                if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                    delay(RETRY_DELAYS[attempt])
                }
            }
        }

        // All retries failed
        emit(Result.Error(lastException ?: Exception("Unknown error occurred")))
    }

    /**
     * Send location without retry (for one-shot requests)
     */
    suspend fun sendLocation(
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        timestamp: Long
    ): Flow<Result<String>> = flow {
        emit(Result.Loading)
        
        try {
            val locationRequest = ApiLocationRequest(
                latitude = latitude,
                longitude = longitude,
                accuracy = accuracy,
                timestamp = dateFormatter.format(Date(timestamp))
            )

            val response = locationApiService.sendLocation(
                "Bearer ${AppConfig.AUTH_TOKEN}",
                locationRequest
            )

            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Location sent successfully"
                emit(Result.Success(message))
            } else {
                emit(Result.Error(HttpException(response)))
            }
            
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    /**
     * Result sealed class for better error handling
     */
    sealed class Result<out T> {
        object Loading : Result<Nothing>()
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val exception: Exception) : Result<Nothing>()
    }
}
