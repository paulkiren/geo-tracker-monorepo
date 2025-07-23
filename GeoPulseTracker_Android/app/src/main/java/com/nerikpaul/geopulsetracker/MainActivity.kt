package com.nerikpaul.geopulsetracker

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    // UI elements
    private lateinit var statusTextView: TextView
    private lateinit var toggleButton: Button

    // Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isTracking = false
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    // For periodic location updates
    private val handler = Handler(Looper.getMainLooper())
    private val locationUpdateInterval: Long = 5000 // Update every 5 seconds (adjust as needed)

    // Runnable to continuously get and send location
    private val locationUpdateRunnable = object : Runnable {
        override fun run() {
            if (isTracking) {
                getLocationAndSend()
                handler.postDelayed(this, locationUpdateInterval) // Schedule next update
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        statusTextView = findViewById(R.id.statusTextView)
        toggleButton = findViewById(R.id.toggleButton)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Set up the button click listener
        toggleButton.setOnClickListener {
            if (isTracking) {
                // If currently tracking, turn it off
                stopLocationTracking()
            } else {
                // If not tracking, request permissions and start
                checkLocationPermissions()
            }
        }

        // Set initial UI state
        updateUI()
    }

    // --- Permission Handling ---
    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, start tracking
            startLocationTracking()
        } else {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    // Callback for permission request results
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted by the user
                startLocationTracking()
            } else {
                // Permission denied by the user
                Toast.makeText(this, "Location permission denied. Cannot track location.", Toast.LENGTH_SHORT).show()
                isTracking = false // Ensure tracking is off if permission is denied
                updateUI()
            }
        }
    }

    // --- Location Tracking Control ---
    private fun startLocationTracking() {
        isTracking = true
        updateUI()
        Toast.makeText(this, "Location tracking started!", Toast.LENGTH_SHORT).show()
        // Start the periodic location update
        handler.post(locationUpdateRunnable)
    }

    private fun stopLocationTracking() {
        isTracking = false
        updateUI()
        Toast.makeText(this, "Location tracking stopped.", Toast.LENGTH_SHORT).show()
        // Stop the periodic location update
        handler.removeCallbacks(locationUpdateRunnable)
    }

    // --- UI Update ---
    private fun updateUI() {
        if (isTracking) {
            statusTextView.text = "Location Tracking: ON"
            toggleButton.text = "Turn Off Tracking"
        } else {
            statusTextView.text = "Location Tracking: OFF"
            toggleButton.text = "Turn On Tracking"
        }
    }

    // --- Get Location and Send to API ---
    private fun getLocationAndSend() {
        // Check for permissions again before attempting to get location (good practice)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        val timestamp = System.currentTimeMillis() // Or use location.time for GPS timestamp

                        Log.d("LocationTracker", "Latitude: $latitude, Longitude: $longitude")
                        Toast.makeText(this, "Lat: $latitude, Lon: $longitude", Toast.LENGTH_SHORT).show()

                        // Call your API to update location
                        sendLocationToBackend(latitude, longitude, timestamp)

                    } else {
                        Log.w("LocationTracker", "Last known location is null.")
                        Toast.makeText(this, "Could not get location.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("LocationTracker", "Error getting location: ${e.message}")
                    Toast.makeText(this, "Error getting location: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // This case should ideally not happen if checkLocationPermissions() was called correctly
            Log.e("LocationTracker", "Location permission missing when trying to get location.")
            Toast.makeText(this, "Location permission is required!", Toast.LENGTH_LONG).show()
            stopLocationTracking() // Stop if permissions are somehow lost
        }
    }

    // This is a placeholder function for your API call.
    // Replace with your actual backend URL and data format.
    private fun sendLocationToBackend(latitude: Double, longitude: Double, timestamp: Long) {
        val backendUrl = "YOUR_BACKEND_API_ENDPOINT_HERE" // <--- IMPORTANT: Replace with your actual API URL

        // Use a background thread for network operations
        Executors.newSingleThreadExecutor().execute {
            try {
                val url = URL(backendUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST" // Or "PUT", depending on your API
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.doOutput = true // Allow output

                // Create the JSON payload for your backend
                val jsonInputString = "{\"latitude\": $latitude, \"longitude\": $longitude, \"timestamp\": $timestamp}"

                conn.outputStream.use { os ->
                    val input = jsonInputString.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    conn.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                        val response = reader.readText()
                        Log.d("API_CALL", "API Success: $response")
                        runOnUiThread {
                            // Update UI on the main thread if needed
                            Toast.makeText(this, "Location updated successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    val errorStream = conn.errorStream?.bufferedReader(Charsets.UTF_8)?.readText()
                    Log.e("API_CALL", "API Error: Response Code $responseCode, Error: $errorStream")
                    runOnUiThread {
                        Toast.makeText(this, "Failed to update location. Code: $responseCode", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("API_CALL", "API call failed: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this, "API call failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Clean up when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        // Ensure that the runnable is removed to prevent memory leaks
        handler.removeCallbacks(locationUpdateRunnable)
    }
}