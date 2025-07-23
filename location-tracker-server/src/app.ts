import express, { Request, Response } from 'express';
import bodyParser from 'body-parser';

// Type definitions
interface LocationData {
    latitude: number;
    longitude: number;
    timestamp: string;
}

interface StoredLocation extends LocationData {
    receivedAt: string;
}

interface LocationUpdateRequest extends Request {
    body: LocationData;
}

interface ApiResponse {
    message: string;
    data?: StoredLocation;
    error?: string;
}

// Create an Express application
const app: express.Application = express();
const port: number = 3000;

// --- Temporary Storage ---
// This array will hold the location data received.
// IMPORTANT: Data stored here is LOST when the server restarts.
// For persistent storage, you would use a database (e.g., MongoDB, PostgreSQL, SQLite).
const receivedLocations: StoredLocation[] = [];

// --- Middleware ---
// Use body-parser middleware to parse JSON request bodies.
// This is crucial for receiving JSON data from your Android app.
app.use(bodyParser.json());

// --- Routes ---

// 1. Root route: Just a simple message to check if the server is running.
app.get('/', (req: Request, res: Response): void => {
    res.send('Location Tracker Server is running!');
});

// 2. API endpoint to receive location data from the device.
// This should match the URL you set in your Android app's MainActivity.kt.
// Example: http://your-server-ip:3000/update-location
app.post('/update-location', (req: LocationUpdateRequest, res: Response<ApiResponse>): void => {
    const { latitude, longitude, timestamp } = req.body;

    // Basic validation
    if (latitude == null || longitude == null || timestamp == null) {
        console.warn('Received incomplete location data:', req.body);
        res.status(400).json({ 
            error: 'Missing latitude, longitude, or timestamp',
            message: 'Invalid request data'
        });
        return;
    }

    // Additional type validation
    if (typeof latitude !== 'number' || typeof longitude !== 'number' || typeof timestamp !== 'string') {
        console.warn('Received invalid data types:', req.body);
        res.status(400).json({ 
            error: 'Invalid data types. Expected latitude and longitude as numbers, timestamp as string',
            message: 'Invalid request data'
        });
        return;
    }

    const newLocation: StoredLocation = {
        latitude,
        longitude,
        timestamp,
        receivedAt: new Date().toISOString() // When the server received it
    };

    receivedLocations.push(newLocation); // Add to our temporary storage

    console.log('Received location:', newLocation); // Log to server console

    // Send a success response back to the Android app
    res.status(200).json({ 
        message: 'Location received successfully!', 
        data: newLocation 
    });
});

// 3. Optional: Endpoint to view all temporarily stored locations.
// For debugging purposes. You might remove this in a production environment.
app.get('/locations', (req: Request, res: Response<StoredLocation[]>): void => {
    res.status(200).json(receivedLocations);
});

// --- Start the Server ---
app.listen(port, (): void => {
    console.log(`Server listening at http://localhost:${port}`);
    console.log(`Location update endpoint: http://localhost:${port}/update-location`);
    console.log(`View all locations: http://localhost:${port}/locations`);
});

export default app;