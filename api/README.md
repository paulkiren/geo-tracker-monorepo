# GeoPulse Location Tracker API

A robust Node.js API built with TypeScript and Express.js for tracking and managing location data with JWT-based authentication.

## Features

- 🔐 **JWT Authentication**: Secure token-based authentication
- 📍 **Location Management**: CRUD operations for location data
- 🛡️ **Security**: Rate limiting, input validation, and security headers
- 🔄 **Middleware**: Custom authentication and error handling middleware
- 📊 **Analytics**: Location statistics and data insights
- 🧪 **Testing**: Comprehensive test suite with Jest
- 📝 **TypeScript**: Full TypeScript support with strict typing
- 🚀 **Production Ready**: Optimized for production deployment

## Quick Start

### Prerequisites

- Node.js (v16 or higher)
- npm or yarn

### Installation

1. Clone the repository and navigate to the API directory:
```bash
cd api
```

2. Install dependencies:
```bash
npm install
```

3. Create environment file:
```bash
cp .env.example .env
```

4. Update the environment variables in `.env`:
```env
NODE_ENV=development
PORT=3000
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production
JWT_EXPIRES_IN=24h
BCRYPT_ROUNDS=12
```

5. Start the development server:
```bash
npm run dev
```

The API will be available at `http://localhost:3000`

## API Endpoints

### Authentication

#### Register User
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

#### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

#### Get Profile
```http
GET /api/v1/auth/profile
Authorization: Bearer YOUR_JWT_TOKEN
```

#### Refresh Token
```http
POST /api/v1/auth/refresh
Authorization: Bearer YOUR_JWT_TOKEN
```

### Locations

#### Get All Locations
```http
GET /api/v1/locations?limit=50&offset=0&startDate=2023-01-01&endDate=2023-12-31
Authorization: Bearer YOUR_JWT_TOKEN
```

#### Create Location
```http
POST /api/v1/locations
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "latitude": 40.7128,
  "longitude": -74.0060,
  "accuracy": 5,
  "address": "New York, NY"
}
```

#### Get Specific Location
```http
GET /api/v1/locations/:id
Authorization: Bearer YOUR_JWT_TOKEN
```

#### Update Location
```http
PUT /api/v1/locations/:id
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "latitude": 40.7589,
  "longitude": -73.9851,
  "accuracy": 3,
  "address": "Times Square, New York, NY"
}
```

#### Delete Location
```http
DELETE /api/v1/locations/:id
Authorization: Bearer YOUR_JWT_TOKEN
```

#### Get Location Statistics
```http
GET /api/v1/locations/stats/summary
Authorization: Bearer YOUR_JWT_TOKEN
```

### Health Check

```http
GET /health
```

## Data Models

### User
```typescript
interface User {
  id: string;
  username: string;
  email: string;
  password: string;
  createdAt: Date;
}
```

### Location
```typescript
interface Location {
  id: string;
  userId: string;
  latitude: number;
  longitude: number;
  accuracy?: number;
  timestamp: Date;
  address?: string;
}
```

### JWT Payload
```typescript
interface JWTPayload {
  userId: string;
  username: string;
  email: string;
}
```

## Authentication

The API uses JWT (JSON Web Tokens) for authentication. Include the token in the Authorization header:

```
Authorization: Bearer YOUR_JWT_TOKEN
```

### Token Expiration

Tokens expire after 24 hours by default. Use the refresh endpoint to get a new token.

## Rate Limiting

The API implements rate limiting to prevent abuse:

- **General API**: 100 requests per 15 minutes
- **Auth endpoints**: 5 requests per 15 minutes
- **Location creation**: 30 requests per minute

## Validation

### User Registration
- Username: 3-20 characters, alphanumeric and underscores only
- Email: Valid email format
- Password: Minimum 8 characters with uppercase, lowercase, number, and special character

### Location Data
- Latitude: -90 to 90
- Longitude: -180 to 180
- Accuracy: Positive number (optional)
- Address: Maximum 255 characters (optional)

## Error Handling

The API returns consistent error responses:

```json
{
  "success": false,
  "error": "Error message",
  "details": ["Validation error details"]
}
```

## Development Commands

```bash
# Start development server
npm run dev

# Build for production
npm run build

# Start production server
npm start

# Run tests
npm test

# Run linting
npm run lint

# Fix linting issues
npm run lint:fix
```

## Testing

Run the test suite:

```bash
npm test
```

The API includes comprehensive tests for:
- Authentication endpoints
- Location CRUD operations
- Middleware functionality
- Error handling

## Security Features

- **Helmet**: Security headers
- **CORS**: Configurable cross-origin resource sharing
- **Rate Limiting**: Prevents abuse
- **Input Validation**: Server-side validation
- **Password Hashing**: Bcrypt with configurable rounds
- **JWT Security**: Secure token generation and validation

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `NODE_ENV` | Environment mode | `development` |
| `PORT` | Server port | `3000` |
| `JWT_SECRET` | JWT signing secret | Required |
| `JWT_EXPIRES_IN` | Token expiration | `24h` |
| `BCRYPT_ROUNDS` | Password hashing rounds | `12` |
| `CORS_ORIGIN` | Allowed origins | `http://localhost:3000` |
| `RATE_LIMIT_WINDOW_MS` | Rate limit window | `900000` |
| `RATE_LIMIT_MAX_REQUESTS` | Max requests per window | `100` |

## Production Deployment

1. Set environment variables
2. Build the application: `npm run build`
3. Start the server: `npm start`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

MIT License - see LICENSE file for details.

## Support

For questions or issues, please open an issue on the GitHub repository.
