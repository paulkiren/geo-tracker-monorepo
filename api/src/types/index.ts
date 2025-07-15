import { Request } from 'express';

export interface User {
  id: string;
  username: string;
  email: string;
  password: string;
  createdAt: Date;
}

export interface Location {
  id: string;
  userId: string;
  latitude: number;
  longitude: number;
  accuracy?: number;
  timestamp: Date;
  address?: string;
}

export interface JWTPayload {
  userId: string;
  username: string;
  email: string;
}

export interface AuthRequest extends Request {
  user?: JWTPayload;
}

export interface ApiResponse<T = any> {
  success: boolean;
  message?: string;
  data?: T;
  error?: string;
}

export interface LocationInput {
  latitude: number;
  longitude: number;
  accuracy?: number;
  address?: string;
}

export interface UserRegistration {
  username: string;
  email: string;
  password: string;
}

export interface UserLogin {
  email: string;
  password: string;
}
