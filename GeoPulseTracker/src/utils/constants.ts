// API Configuration
export const API_CONFIG = {
  BASE_URL: __DEV__ ? 'http://localhost:3000/api' : 'https://your-production-api.com/api',
  TIMEOUT: 10000, // 10 seconds
};

// Common API endpoints
export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    PROFILE: '/auth/profile',
  },
  LOCATIONS: {
    BASE: '/locations',
    STATS: '/locations/stats/summary',
  },
};

// HTTP status codes
export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  INTERNAL_SERVER_ERROR: 500,
};

// Storage keys for AsyncStorage
export const STORAGE_KEYS = {
  AUTH_TOKEN: 'authToken',
  USER_DATA: 'user',
  APP_SETTINGS: 'appSettings',
};
