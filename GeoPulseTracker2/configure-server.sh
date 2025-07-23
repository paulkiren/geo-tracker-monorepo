#!/bin/bash

# GeoPulse Tracker - Server Configuration Helper
# This script helps you configure the correct server URL for your Android app

echo "🚀 GeoPulse Tracker - Server Configuration Helper"
echo "================================================="
echo ""

# Get local IP address
LOCAL_IP=$(ifconfig | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1' | head -1)

echo "📍 Your computer's IP address: $LOCAL_IP"
echo ""
echo "Configuration options:"
echo ""
echo "1. For Android Emulator (development):"
echo "   BASE_URL = \"http://10.0.2.2:3000/api/\""
echo ""
echo "2. For Physical Device (same network):"
echo "   BASE_URL = \"http://$LOCAL_IP:3000/api/\""
echo ""
echo "3. For Production:"
echo "   BASE_URL = \"https://your-domain.com/api/\""
echo ""
echo "📝 Update these values in:"
echo "   app/src/main/java/com/nerikpaul/geopulsetracker/config/AppConfig.kt"
echo ""
echo "🔧 Don't forget to:"
echo "   1. Start your API server: npm run dev"
echo "   2. Update your JWT token in AppConfig.kt"
echo "   3. Grant location permissions in the app"
echo ""

# Check if server is running
echo "🔍 Checking if server is running..."
if curl -s "http://localhost:3000/api/health" > /dev/null 2>&1; then
    echo "✅ Server is running on localhost:3000"
else
    echo "❌ Server is not running on localhost:3000"
    echo "   Start it with: cd api && npm run dev"
fi

echo ""
echo "Happy tracking! 📱📍"
