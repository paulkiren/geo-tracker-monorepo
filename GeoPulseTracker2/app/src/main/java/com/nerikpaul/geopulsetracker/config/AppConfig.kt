package com.nerikpaul.geopulsetracker.config

object AppConfig {
    // Change this to your actual server URL
    // For Android emulator: "http://10.0.2.2:3000/api/"
    // For physical device: "http://YOUR_SERVER_IP:3000/api/"
    // For production: "https://your-domain.com/api/"
    
    // Primary server URL
    const val BASE_URL = "https://location-tracker-server-production.up.railway.app/"
    
    // Fallback URLs in case primary fails
    val FALLBACK_URLS = listOf(
        "http://10.0.2.2:3000/", // Android emulator
        "http://192.168.1.100:3000/" // Replace with your local IP
    )
    
    // Update this with your actual JWT token or implement proper authentication
    const val AUTH_TOKEN = "your_jwt_token_here" // Removed "Bearer " prefix as it's added in the service
}
