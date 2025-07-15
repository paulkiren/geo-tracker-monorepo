import { Request, Response, NextFunction } from 'express';
import { verifyToken } from '../utils/jwt';
import { AuthRequest } from '../types';

export const authenticateToken = (req: AuthRequest, res: Response, next: NextFunction): void => {
  const authHeader = req.headers['authorization'];
  const token = authHeader?.split(' ')[1]; // Bearer TOKEN

  if (!token) {
    res.status(401).json({ error: 'Access token required' });
    return;
  }

  try {
    const decoded = verifyToken(token);
    req.user = decoded;
    next();
  } catch (error) {
    console.error('Token validation failed:', error);
    res.status(403).json({ error: 'Invalid or expired token' });
  }
};

export const optionalAuth = (req: AuthRequest, res: Response, next: NextFunction): void => {
  const authHeader = req.headers['authorization'];
  const token = authHeader?.split(' ')[1];

  if (token) {
    try {
      const decoded = verifyToken(token);
      req.user = decoded;
    } catch (error) {
      // Token is invalid but we continue without authentication
      console.error('Token validation failed:', error);
    }
  }
  next();
};
