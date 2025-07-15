import { Platform } from 'react-native';

const getBaseUrl = () => {
  if (!__DEV__) {
    return 'http://localhost:3000/api'; // Production URL
  }
  
  // Your laptop's IP address
  const LAPTOP_IP = '192.168.1.9';
  
  if (Platform.OS === 'android') {
    // For Android emulator: use 10.0.2.2
    // For physical Android device: use laptop IP
    // You can manually switch this based on your setup
    const USE_EMULATOR = false; // Set to true if using Android emulator
    
    return USE_EMULATOR 
      ? 'http://10.0.2.2:3000/api'  // Android emulator
      : `http://${LAPTOP_IP}:3000/api`; // Physical device
  } else if (Platform.OS === 'ios') {
    return 'http://localhost:3000/api'; // iOS simulator
  }
  
  return 'http://localhost:3000/api'; // Default fallback
};

// API Configuration
export const API_CONFIG = {
  BASE_URL: getBaseUrl(),
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
