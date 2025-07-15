import AsyncStorage from '@react-native-async-storage/async-storage';
import { LoginCredentials, RegisterCredentials, AuthResponse, User } from '../types/auth';
import { API_CONFIG, API_ENDPOINTS, STORAGE_KEYS } from '../utils/constants';

class AuthService {
  private static instance: AuthService;
  private token: string | null = null;

  private constructor() {}

  public static getInstance(): AuthService {
    if (!AuthService.instance) {
      AuthService.instance = new AuthService();
    }
    return AuthService.instance;
  }

  // Initialize the service by loading stored token
  async initialize(): Promise<void> {
    try {
      this.token = await AsyncStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);
    } catch (error) {
      console.error('Error loading auth token:', error);
    }
  }

  // Login user
  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    try {
      const response = await fetch(`${API_CONFIG.BASE_URL}${API_ENDPOINTS.AUTH.LOGIN}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(credentials),
      });

      const data: AuthResponse = await response.json();

      if (data.success && data.data?.token) {
        this.token = data.data.token;
        await AsyncStorage.setItem(STORAGE_KEYS.AUTH_TOKEN, this.token);
        await AsyncStorage.setItem(STORAGE_KEYS.USER_DATA, JSON.stringify(data.data.user));
      }

      return data;
    } catch (error) {
      console.error('Login error:', error);
      return {
        success: false,
        error: 'Network error. Please check your connection.',
      };
    }
  }

  // Register user
  async register(credentials: RegisterCredentials): Promise<AuthResponse> {
    try {
      const response = await fetch(`${API_CONFIG.BASE_URL}${API_ENDPOINTS.AUTH.REGISTER}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(credentials),
      });

      const data: AuthResponse = await response.json();

      if (data.success && data.data?.token) {
        this.token = data.data.token;
        await AsyncStorage.setItem(STORAGE_KEYS.AUTH_TOKEN, this.token);
        await AsyncStorage.setItem(STORAGE_KEYS.USER_DATA, JSON.stringify(data.data.user));
      }

      return data;
    } catch (error) {
      console.error('Registration error:', error);
      return {
        success: false,
        error: 'Network error. Please check your connection.',
      };
    }
  }

  // Logout user
  async logout(): Promise<void> {
    try {
      this.token = null;
      await AsyncStorage.removeItem(STORAGE_KEYS.AUTH_TOKEN);
      await AsyncStorage.removeItem(STORAGE_KEYS.USER_DATA);
    } catch (error) {
      console.error('Logout error:', error);
    }
  }

  // Get current user from storage
  async getCurrentUser(): Promise<User | null> {
    try {
      const userString = await AsyncStorage.getItem(STORAGE_KEYS.USER_DATA);
      return userString ? JSON.parse(userString) : null;
    } catch (error) {
      console.error('Error getting current user:', error);
      return null;
    }
  }

  // Check if user is authenticated
  isAuthenticated(): boolean {
    return this.token !== null;
  }

  // Get auth token
  getToken(): string | null {
    return this.token;
  }

  // Get auth headers for API requests
  getAuthHeaders(): Record<string, string> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };

    if (this.token) {
      headers.Authorization = `Bearer ${this.token}`;
    }

    return headers;
  }

  // Make authenticated API request
  async authenticatedRequest(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<Response> {
    const url = endpoint.startsWith('http') ? endpoint : `${API_CONFIG.BASE_URL}${endpoint}`;
    
    return fetch(url, {
      ...options,
      headers: {
        ...this.getAuthHeaders(),
        ...options.headers,
      },
    });
  }
}

export default AuthService.getInstance();
