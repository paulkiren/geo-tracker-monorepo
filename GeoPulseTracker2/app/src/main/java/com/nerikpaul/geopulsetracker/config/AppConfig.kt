package com.nerikpaul.geopulsetracker.config

object AppConfig {
    // Change this to your actual server URL
    // For Android emulator: "http://10.0.2.2:3000/api/"
    // For physical device: "http://YOUR_SERVER_IP:3000/api/"
    // For production: "https://your-domain.com/api/"
    const val BASE_URL = "https://location-tracker-server-production.up.railway.app/"
    
    // Update this with your actual JWT token or implement proper authentication
    const val AUTH_TOKEN = "Bearer your_jwt_token_here"
}
