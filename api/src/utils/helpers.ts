import { Response } from 'express';
import { ApiResponse } from '../types';

// Utility function to send standardized API responses
export const sendResponse = <T = any>(
  res: Response,
  statusCode: number,
  success: boolean,
  message?: string,
  data?: T,
  error?: string
) => {
  const response: ApiResponse<T> = {
    success,
    ...(message && { message }),
    ...(data && { data }),
    ...(error && { error })
  };
  
  res.status(statusCode).json(response);
};

// Utility function to send success response
export const sendSuccess = <T = any>(
  res: Response,
  data?: T,
  message?: string,
  statusCode: number = 200
) => {
  sendResponse(res, statusCode, true, message, data);
};

// Utility function to send error response
export const sendError = (
  res: Response,
  error: string,
  statusCode: number = 500,
  message?: string
) => {
  sendResponse(res, statusCode, false, message, undefined, error);
};

// Utility function to validate coordinate values
export const isValidCoordinate = (lat: number, lng: number): boolean => {
  return lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
};

// Utility function to calculate distance between two points using Haversine formula
export const calculateDistance = (
  lat1: number,
  lon1: number,
  lat2: number,
  lon2: number
): number => {
  const R = 6371; // Earth's radius in kilometers
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = 
    Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * 
    Math.sin(dLon/2) * Math.sin(dLon/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  return R * c;
};

// Utility function to generate a random ID
export const generateId = (): string => {
  return Date.now().toString(36) + Math.random().toString(36).substring(2);
};

// Utility function to validate email format
export const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

// Utility function to sanitize user input
export const sanitizeInput = (input: string): string => {
  return input.trim().replace(/[<>]/g, '');
};
