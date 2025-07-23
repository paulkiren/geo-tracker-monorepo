const axios = require('axios');

const BASE_URL = 'http://localhost:3000';

// Generate random coordinates (around San Francisco area)
function generateRandomLocation() {
    const lat = 37.7749 + (Math.random() - 0.5) * 0.1; // ±0.05 degrees
    const lng = -122.4194 + (Math.random() - 0.5) * 0.1;
    
    return {
        latitude: parseFloat(lat.toFixed(6)),
        longitude: parseFloat(lng.toFixed(6)),
        timestamp: new Date().toISOString()
    };
}

async function testAPIs() {
    try {
        // Test root endpoint
        console.log('Testing root endpoint...');
        const rootResponse = await axios.get(`${BASE_URL}/`);
        console.log('✅ Root:', rootResponse.data);

        // Send 5 random locations
        console.log('\nSending random locations...');
        for (let i = 1; i <= 5; i++) {
            const location = generateRandomLocation();
            const response = await axios.post(`${BASE_URL}/update-location`, location);
            console.log(`✅ Location ${i}:`, response.data);
            
            // Wait 1 second between requests
            await new Promise(resolve => setTimeout(resolve, 1000));
        }

        // Get all locations
        console.log('\nFetching all locations...');
        const locationsResponse = await axios.get(`${BASE_URL}/locations`);
        console.log('✅ All locations:', locationsResponse.data);

    } catch (error) {
        console.error('❌ Error:', error.response?.data || error.message);
    }
}

testAPIs();