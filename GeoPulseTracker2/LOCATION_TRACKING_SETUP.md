# GeoPulse Tracker - Location Tracking Feature

This Android app sends GPS coordinates to your server at customizable intervals. The app includes a modern UI with interval selection buttons and real-time tracking status.

## Features

- 📍 **Real-time Location Tracking**: Continuously tracks GPS coordinates
- ⏱️ **Customizable Intervals**: Choose from 1, 5, 10, 15, 30, or 60-minute intervals
- 🌐 **Server Integration**: Automatically sends location data to your API server
- 🔒 **Permission Management**: Handles location permissions gracefully
- 📊 **Status Dashboard**: Shows tracking status, interval, and location count
- 🎨 **Modern UI**: Clean Material Design 3 interface

## Setup Instructions

### 1. Server Configuration

Update the server URL in `AppConfig.kt`:

```kotlin
// For Android Emulator (development)
const val BASE_URL = "http://10.0.2.2:3000/api/"

// For Physical Device (same network)
const val BASE_URL = "http://YOUR_COMPUTER_IP:3000/api/"

// For Production
const val BASE_URL = "https://your-domain.com/api/"
```

### 2. Authentication Setup

Update the auth token in `AppConfig.kt`:

```kotlin
const val AUTH_TOKEN = "Bearer your_actual_jwt_token_here"
```

**Note**: For production, implement proper authentication with login/registration.

### 3. Start Your API Server

Make sure your GeoPulse API server is running:

```bash
cd api
npm run dev
```

The server should be accessible at `http://localhost:3000`

### 4. Build and Run

1. Open the project in Android Studio
2. Sync the project to download dependencies
3. Run the app on an emulator or physical device

## API Integration

The app sends POST requests to `/api/locations` with this payload:

```json
{
  "latitude": 37.7749,
  "longitude": -122.4194,
  "accuracy": 15.0,
  "address": null
}
```

Headers include:
```
Authorization: Bearer your_jwt_token
Content-Type: application/json
```

## How to Use

1. **Grant Permissions**: Allow location access when prompted
2. **Select Interval**: Choose your preferred tracking interval (1-60 minutes)
3. **Start Tracking**: Tap "START TRACKING" to begin sending location data
4. **Monitor Status**: View real-time status including:
   - Tracking state (Active/Inactive)
   - Current interval setting
   - Number of locations sent
   - Last known coordinates
5. **Stop Tracking**: Tap "STOP TRACKING" to end the session
6. **Reset Counter**: Clear the location count (only when stopped)

## Technical Details

### Dependencies Added

- **Retrofit 2**: HTTP client for API communication
- **Gson Converter**: JSON serialization/deserialization
- **OkHttp Logging**: Network request/response logging
- **Google Play Services Location**: High-accuracy location services

### Permissions Required

- `ACCESS_FINE_LOCATION`: Precise location access
- `ACCESS_COARSE_LOCATION`: Approximate location access
- `INTERNET`: Network communication
- `FOREGROUND_SERVICE`: Background location tracking
- `FOREGROUND_SERVICE_LOCATION`: Location-specific foreground service

### Architecture

```
├── data/
│   └── LocationData.kt          # Data models for API
├── network/
│   ├── ApiClient.kt            # Retrofit configuration
│   └── LocationApiService.kt    # API interface
├── service/
│   └── LocationService.kt      # Location tracking logic
├── utils/
│   └── PermissionHelper.kt     # Permission utilities
├── config/
│   └── AppConfig.kt           # Configuration constants
└── MainActivity.kt            # Main UI and app logic
```

## Troubleshooting

### Common Issues

1. **"Network Error"**
   - Check if the API server is running
   - Verify the correct IP address in `AppConfig.kt`
   - Ensure device and server are on the same network (for local testing)

2. **"Location Permission Denied"**
   - Grant location permissions in app settings
   - Check that location services are enabled on the device

3. **"Authentication Failed"**
   - Update the JWT token in `AppConfig.kt`
   - Ensure the token is valid and properly formatted

4. **"No Location Updates"**
   - Move to an outdoor location for better GPS signal
   - Check that location services are enabled
   - Verify the app has location permissions

### Testing Tips

- Use Android Studio's emulator with mock locations for testing
- Check the Logcat for detailed error messages
- Test with different intervals to verify functionality
- Use tools like Postman to test your API endpoints independently

## Next Steps

Consider implementing these enhancements:

1. **Background Service**: Continue tracking when app is closed
2. **User Authentication**: Login/registration system
3. **Offline Storage**: Cache locations when network is unavailable
4. **Location History**: View previously tracked locations
5. **Geofencing**: Alerts when entering/leaving specific areas
6. **Battery Optimization**: Smart tracking based on movement detection

## Security Notes

- Always use HTTPS in production
- Implement proper JWT token refresh mechanisms
- Consider encrypting sensitive location data
- Follow privacy regulations (GDPR, CCPA, etc.)
- Provide clear privacy policies to users
