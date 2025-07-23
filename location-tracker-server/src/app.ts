// Import necessary modules
const express = require('express');
const bodyParser = require('body-parser');

// Create an Express application
const app = express();
const port = 3000; // The port your server will listen on

// --- Temporary Storage ---
// This array will hold the location data received.
// IMPORTANT: Data stored here is LOST when the server restarts.
// For persistent storage, you would use a database (e.g., MongoDB, PostgreSQL, SQLite).
const receivedLocations = [];

// --- Middleware ---
// Use body-parser middleware to parse JSON request bodies.
// This is crucial for receiving JSON data from your Android app.
app.use(bodyParser.json());

// --- Routes ---

// 1. Root route: Just a simple message to check if the server is running.
app.get('/', (req, res) => {
    res.send('Location Tracker Server is running!');
});

// 2. API endpoint to receive location data from the device.
// This should match the URL you set in your Android app's MainActivity.kt.
// Example: http://your-server-ip:3000/update-location
app.post('/update-location', (req, res) => {
    const { latitude, longitude, timestamp } = req.body;

    // Basic validation
    if (latitude == null || longitude == null || timestamp == null) {
        console.warn('Received incomplete location data:', req.body);
        return res.status(400).json({ error: 'Missing latitude, longitude, or timestamp' });
    }

    const newLocation = {
        latitude: latitude,
        longitude: longitude,
        timestamp: timestamp,
        receivedAt: new Date().toISOString() // When the server received it
    };

    receivedLocations.push(newLocation); // Add to our temporary storage

    console.log('Received location:', newLocation); // Log to server console

    // Send a success response back to the Android app
    res.status(200).json({ message: 'Location received successfully!', data: newLocation });
});

// 3. Optional: Endpoint to view all temporarily stored locations.
// For debugging purposes. You might remove this in a production environment.
app.get('/locations', (req, res) => {
    res.status(200).json(receivedLocations);
});

// --- Start the Server ---
app.listen(port, () => {
    console.log(`Server listening at http://localhost:${port}`);
    console.log(`Location update endpoint: http://localhost:${port}/update-location`);
    console.log(`View all locations: http://localhost:${port}/locations`);
});