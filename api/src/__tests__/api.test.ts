import request from 'supertest';
import app from '../server';

describe('Auth Endpoints', () => {
  const testUser = {
    username: 'testuser',
    email: 'test@example.com',
    password: 'Test123@!'
  };

  it('should register a new user', async () => {
    const response = await request(app)
      .post('/api/v1/auth/register')
      .send(testUser);

    expect(response.status).toBe(201);
    expect(response.body.success).toBe(true);
    expect(response.body.data.token).toBeDefined();
    expect(response.body.data.user.email).toBe(testUser.email);
  });

  it('should login with valid credentials', async () => {
    // First register a user
    await request(app)
      .post('/api/v1/auth/register')
      .send(testUser);

    const response = await request(app)
      .post('/api/v1/auth/login')
      .send({
        email: testUser.email,
        password: testUser.password
      });

    expect(response.status).toBe(200);
    expect(response.body.success).toBe(true);
    expect(response.body.data.token).toBeDefined();
  });

  it('should reject login with invalid credentials', async () => {
    const response = await request(app)
      .post('/api/v1/auth/login')
      .send({
        email: 'wrong@example.com',
        password: 'wrongpassword'
      });

    expect(response.status).toBe(401);
    expect(response.body.success).toBe(false);
  });
});

describe('Location Endpoints', () => {
  let token: string;

  beforeEach(async () => {
    // Register and login to get a token
    const user = {
      username: 'locationuser',
      email: 'location@example.com',
      password: 'Test123@!'
    };

    const loginResponse = await request(app)
      .post('/api/v1/auth/register')
      .send(user);

    token = loginResponse.body.data.token;
  });

  it('should create a new location', async () => {
    const location = {
      latitude: 40.7128,
      longitude: -74.0060,
      accuracy: 5,
      address: 'New York, NY'
    };

    const response = await request(app)
      .post('/api/v1/locations')
      .set('Authorization', `Bearer ${token}`)
      .send(location);

    expect(response.status).toBe(201);
    expect(response.body.success).toBe(true);
    expect(response.body.data.location.latitude).toBe(location.latitude);
  });

  it('should get user locations', async () => {
    // Create a location first
    const location = {
      latitude: 40.7128,
      longitude: -74.0060,
      accuracy: 5,
      address: 'New York, NY'
    };

    await request(app)
      .post('/api/v1/locations')
      .set('Authorization', `Bearer ${token}`)
      .send(location);

    const response = await request(app)
      .get('/api/v1/locations')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.success).toBe(true);
    expect(response.body.data.locations).toBeDefined();
    expect(response.body.data.locations.length).toBeGreaterThan(0);
  });

  it('should require authentication for protected routes', async () => {
    const response = await request(app)
      .get('/api/v1/locations');

    expect(response.status).toBe(401);
    expect(response.body.success).toBe(false);
  });
});
