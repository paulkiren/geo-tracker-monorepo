# GeoPulse Tracker - Location Tracking Implementation

Your Android app is now ready to send GPS coordinates to your server at custom intervals! 🎉

## 🚀 **What's Been Implemented:**

### **Core Features:**
- ✅ **Real-time GPS tracking** with Google Play Services
- ✅ **Custom interval selection** (1, 5, 10, 15, 30, 60 minutes)
- ✅ **Server communication** via Retrofit HTTP client
- ✅ **Modern Material Design 3 UI** with interval selector buttons
- ✅ **Permission handling** for location access
- ✅ **Status dashboard** showing tracking state and location count

### **UI Components:**
- **Interval Selection**: Radio buttons for 1m, 5m, 10m, 15m, 30m, 60m
- **Status Card**: Shows tracking state, interval, and locations sent
- **Control Buttons**: START/STOP tracking and reset counter
- **Location Display**: Shows last GPS coordinates

### **Technical Stack:**
- **LocationService**: Handles GPS tracking and server communication
- **Retrofit + OkHttp**: HTTP client for API calls
- **Google Play Services**: High-accuracy location tracking
- **Kotlin Coroutines**: Asynchronous server requests
- **Material Design 3**: Modern Android UI components

## 🔧 **Setup Instructions:**

### **1. Configure Server URL:**

Update your server URL in `AppConfig.kt`:

```kotlin
// For Android Emulator (development)
const val BASE_URL = "http://10.0.2.2:3000/api/"

// For Physical Device (same network as your computer)
const val BASE_URL = "http://YOUR_COMPUTER_IP:3000/api/"

// For Production
const val BASE_URL = "https://your-domain.com/api/"
```

**To find your computer's IP address:**
```bash
# Run the helper script
./configure-server.sh

# Or manually find IP:
# macOS/Linux: ifconfig | grep "inet " | grep -v 127.0.0.1
# Windows: ipconfig | findstr "IPv4"
```

### **2. Set Authentication Token:**

Update your JWT token in `AppConfig.kt`:
```kotlin
const val AUTH_TOKEN = "Bearer your_actual_jwt_token_here"
```

### **3. Start Your API Server:**

```bash
cd ../api
npm run dev
```

Your server should be running on `http://localhost:3000`

### **4. Build and Install:**

```bash
# Build the APK
./gradlew assembleDebug

# Install on device/emulator
adb install app/build/outputs/apk/debug/app-debug.apk

# Or open in Android Studio and run
```

## 📱 **How to Use the App:**

1. **Launch the app** - You'll see the GeoPulse Tracker interface
2. **Grant permissions** - Allow location access when prompted
3. **Select interval** - Choose from 1, 5, 10, 15, 30, or 60 minutes
4. **Start tracking** - Tap "START TRACKING" (button turns red)
5. **Monitor status** - Watch the dashboard for:
   - Tracking state (ACTIVE/INACTIVE)
   - Selected interval
   - Number of locations sent to server
   - Last GPS coordinates
6. **Stop tracking** - Tap "STOP TRACKING" when done
7. **Reset counter** - Clear the sent locations count

## 🔍 **API Communication:**

The app sends POST requests to `/api/locations` with this payload:

```json
{
  "latitude": 37.7749,
  "longitude": -122.4194,
  "accuracy": 15.0,
  "address": null
}
```

**Request Headers:**
```
Authorization: Bearer your_jwt_token
Content-Type: application/json
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "id": "location_id",
    "latitude": 37.7749,
    "longitude": -122.4194,
    "accuracy": 15.0,
    "timestamp": "2025-07-23T16:30:00Z",
    "userId": "user_id"
  },
  "message": "Location saved successfully"
}
```

## 🛠 **File Structure:**

```
app/src/main/java/com/nerikpaul/geopulsetracker/
├── MainActivity.kt              # Main UI with interval selector
├── config/
│   └── AppConfig.kt            # Server URL and auth token
├── data/
│   └── LocationData.kt         # API data models
├── network/
│   ├── ApiClient.kt           # Retrofit HTTP client
│   └── LocationApiService.kt   # API interface
├── service/
│   └── LocationService.kt     # GPS tracking and server communication
└── utils/
    └── PermissionHelper.kt    # Location permission utilities
```

## 🔧 **Troubleshooting:**

### **Common Issues:**

1. **"Network Error" / "Failed to send location"**
   - ✅ Check if API server is running (`npm run dev`)
   - ✅ Verify correct IP address in `AppConfig.kt`
   - ✅ Ensure device and computer are on same network
   - ✅ Check firewall settings

2. **"Location Permission Denied"**
   - ✅ Grant location permissions in app settings
   - ✅ Enable location services on device
   - ✅ Try restarting the app

3. **"Authentication Failed"**
   - ✅ Update JWT token in `AppConfig.kt`
   - ✅ Ensure token format: `"Bearer your_token_here"`
   - ✅ Check token expiration

4. **"No GPS Updates"**
   - ✅ Move to outdoor location for better GPS signal
   - ✅ Check location permissions
   - ✅ Verify location services are enabled

### **Testing Tips:**

```bash
# Test your API endpoint manually
curl -X POST http://10.0.2.2:3000/api/locations \
  -H "Authorization: Bearer your_token" \
  -H "Content-Type: application/json" \
  -d '{"latitude": 37.7749, "longitude": -122.4194, "accuracy": 15.0}'

# Check Android logs
adb logcat | grep LocationService

# Monitor network traffic
adb logcat | grep OkHttp
```

## 🎯 **Next Steps:**

Consider implementing these enhancements:

1. **Background Service**: Continue tracking when app is closed
2. **Offline Storage**: Cache locations when network is unavailable
3. **User Authentication**: Login/registration system
4. **Location History**: View previously tracked locations
5. **Geofencing**: Alerts when entering/leaving areas
6. **Battery Optimization**: Smart tracking based on movement

## 🔐 **Security Considerations:**

- Always use HTTPS in production
- Implement proper JWT token refresh
- Consider encrypting location data
- Follow privacy regulations (GDPR, CCPA)
- Provide clear privacy policies

---

**Your app is now ready to track and send GPS coordinates to your server! 🌍📡**

Happy tracking! 🚀
