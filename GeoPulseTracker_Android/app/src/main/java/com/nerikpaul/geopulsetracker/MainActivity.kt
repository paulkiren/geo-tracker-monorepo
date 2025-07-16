package com.nerikpaul.geopulsetracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    // For requesting multiple permissions at once
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val backgroundLocationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: false
        } else {
            true // Not applicable for older Android versions
        }

        if (fineLocationGranted && backgroundLocationGranted) {
            Log.d(TAG, "All required location permissions granted.")
            startLocationService(25000L) // Start service with 25-second interval
        } else {
            Log.w(TAG, "Not all location permissions granted. Fine: $fineLocationGranted, Background: $backgroundLocationGranted")
            showLocationPermissionRationale()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.start_service_button).setOnClickListener {
            checkAndRequestPermissions()
        }

        findViewById<Button>(R.id.stop_service_button).setOnClickListener {
            stopLocationService()
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        val allPermissionsGranted = permissionsToRequest.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allPermissionsGranted) {
            Log.d(TAG, "Permissions already granted, starting service.")
            startLocationService(25000L) // Start service with 25-second interval
        } else {
            Log.d(TAG, "Requesting location permissions.")
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun showLocationPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Location Permission Needed")
            .setMessage("This app needs access to your location in the background to track your position even when the app is closed. Please grant 'Allow all the time' for continuous tracking.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Location tracking disabled without necessary permissions.", Toast.LENGTH_LONG).show()
            }
            .show()
    }

    private fun startLocationService(interval: Long) {
        val serviceIntent = Intent(this, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_FOREGROUND_SERVICE
            putExtra(LocationTrackingService.EXTRA_INTERVAL, interval)
        }
        // For Android O (API 26) and above, use startForegroundService()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, serviceIntent)
        } else {
            startService(serviceIntent)
        }
        Toast.makeText(this, "Location tracking service started.", Toast.LENGTH_SHORT).show()
    }

    private fun stopLocationService() {
        val serviceIntent = Intent(this, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP_FOREGROUND_SERVICE
        }
        stopService(serviceIntent)
        Toast.makeText(this, "Location tracking service stopped.", Toast.LENGTH_SHORT).show()
    }
}