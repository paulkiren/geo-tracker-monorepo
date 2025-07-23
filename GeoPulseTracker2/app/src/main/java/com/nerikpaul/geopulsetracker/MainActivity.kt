package com.nerikpaul.geopulsetracker

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nerikpaul.geopulsetracker.service.LocationService
import com.nerikpaul.geopulsetracker.ui.theme.GeoPulseTrackerTheme
import com.nerikpaul.geopulsetracker.utils.PermissionHelper

class MainActivity : ComponentActivity() {
    private lateinit var locationService: LocationService
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            Toast.makeText(this, "Location permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Location permissions are required for this app", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        locationService = LocationService(this)
        
        // Check permissions on startup
        if (!PermissionHelper.hasLocationPermission(this)) {
            requestPermissionLauncher.launch(PermissionHelper.getLocationPermissions())
        }
        
        setContent {
            GeoPulseTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LocationTrackingScreen(
                        modifier = Modifier.padding(innerPadding),
                        locationService = locationService,
                        onPermissionRequest = {
                            requestPermissionLauncher.launch(PermissionHelper.getLocationPermissions())
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LocationTrackingScreen(
    modifier: Modifier = Modifier,
    locationService: LocationService,
    onPermissionRequest: () -> Unit
) {
    val context = LocalContext.current
    var isTracking by remember { mutableStateOf(false) }
    var selectedInterval by remember { mutableStateOf(1) } // Default 1 minute
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var locationCount by remember { mutableStateOf(0) }
    
    // Available intervals in minutes
    val intervalOptions = listOf(1, 5, 10, 15, 30, 60)
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        // Title
        Text(
            text = "GeoPulse Tracker",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        // Current status card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tracking Status",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (isTracking) "ACTIVE" else "INACTIVE",
                    fontSize = 16.sp,
                    color = if (isTracking) Color.Green else Color.Red,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Interval: $selectedInterval minute${if (selectedInterval > 1) "s" else ""}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Locations sent: $locationCount",
                    fontSize = 14.sp
                )
                currentLocation?.let { location ->
                    Text(
                        text = "Last location: ${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Interval selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select Tracking Interval",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                Column(
                    modifier = Modifier.selectableGroup()
                ) {
                    intervalOptions.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { interval ->
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .selectable(
                                            selected = selectedInterval == interval,
                                            onClick = { 
                                                if (!isTracking) {
                                                    selectedInterval = interval 
                                                }
                                            },
                                            role = Role.RadioButton
                                        )
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedInterval == interval,
                                        onClick = null,
                                        enabled = !isTracking
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${interval}m",
                                        fontSize = 14.sp,
                                        color = if (isTracking) MaterialTheme.colorScheme.onSurfaceVariant 
                                               else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Control buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (!PermissionHelper.hasLocationPermission(context)) {
                        onPermissionRequest()
                        return@Button
                    }
                    
                    if (isTracking) {
                        locationService.stopLocationTracking()
                        isTracking = false
                    } else {
                        locationService.startLocationTracking(selectedInterval) { location ->
                            currentLocation = location
                            locationCount++
                        }
                        isTracking = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTracking) Color.Red else Color.Green,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = if (isTracking) "STOP TRACKING" else "START TRACKING",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Button(
                onClick = {
                    locationCount = 0
                    currentLocation = null
                },
                modifier = Modifier.fillMaxWidth(0.6f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White
                ),
                enabled = !isTracking
            ) {
                Text(text = "Reset Counter")
            }
        }
        
        // Info text
        Text(
            text = "Note: Location data will be sent to your server at the selected interval when tracking is active.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun TrackingButton(modifier: Modifier = Modifier) {
    var isTracking by remember { mutableStateOf(false) }

    Button(
        onClick = { isTracking = !isTracking },
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(0.8f), // Adjust the fraction to control the width
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isTracking) Color.Green else Color.Gray,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Text(text = if (isTracking) "Location Tracking ON" else "Turn ON Location Tracking")
    }
}

@Preview(showBackground = true)
@Composable
fun LocationTrackingScreenPreview() {
    GeoPulseTrackerTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            LocationTrackingScreen(
                modifier = Modifier.padding(innerPadding),
                locationService = LocationService(LocalContext.current),
                onPermissionRequest = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TrackingButtonPreview() {
    GeoPulseTrackerTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) { TrackingButton() }
        }
    }
}