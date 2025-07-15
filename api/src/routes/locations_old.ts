import { Router, Response } from 'express';
import { body, validationResult, param } from 'express-validator';
import { authenticateToken } from '../middleware/auth';
import { AuthRequest, Location, LocationInput } from '../types';

const router = Router();

// Mock location storage (replace with database in production)
const locations: Location[] = [];

// Validation middleware
const validateLocation = [
  body('latitude')
    .isFloat({ min: -90, max: 90 })
    .withMessage('Latitude must be between -90 and 90'),
  body('longitude')
    .isFloat({ min: -180, max: 180 })
    .withMessage('Longitude must be between -180 and 180'),
  body('accuracy')
    .optional()
    .isFloat({ min: 0 })
    .withMessage('Accuracy must be a positive number'),
  body('address')
    .optional()
    .isString()
    .trim()
    .isLength({ max: 255 })
    .withMessage('Address must be less than 255 characters')
];

const validateLocationId = [
  param('id')
    .notEmpty()
    .withMessage('Location ID is required')
    .isString()
    .withMessage('Location ID must be a string')
];

// Get user's locations with optional filtering
router.get('/', authenticateToken, (req: AuthRequest, res: Response) => {
  try {
    const { limit = '50', offset = '0', startDate, endDate } = req.query;
    
    let userLocations = locations.filter(loc => loc.userId === req.user?.userId);
    
    // Filter by date range if provided
    if (startDate) {
      const start = new Date(startDate as string);
      userLocations = userLocations.filter(loc => loc.timestamp >= start);
    }
    
    if (endDate) {
      const end = new Date(endDate as string);
      userLocations = userLocations.filter(loc => loc.timestamp <= end);
    }
    
    // Sort by timestamp (newest first)
    userLocations.sort((a, b) => b.timestamp.getTime() - a.timestamp.getTime());
    
    // Apply pagination
    const limitNum = parseInt(limit as string, 10);
    const offsetNum = parseInt(offset as string, 10);
    const paginatedLocations = userLocations.slice(offsetNum, offsetNum + limitNum);
    
    res.json({
      success: true,
      data: {
        locations: paginatedLocations,
        total: userLocations.length,
        limit: limitNum,
        offset: offsetNum
      }
    });
  } catch (error) {
    console.error('Get locations error:', error);
    res.status(500).json({
      success: false,
      error: 'Internal server error'
    });
  }
});

// Add new location
router.post('/', authenticateToken, validateLocation, (req: AuthRequest, res: Response): void => {
  try {
    // Check validation results
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      res.status(400).json({
        success: false,
        error: 'Validation failed',
        details: errors.array()
      });
      return;
    }

    const { latitude, longitude, accuracy, address }: LocationInput = req.body;

    const location: Location = {
      id: Date.now().toString(),
      userId: req.user!.userId,
      latitude,
      longitude,
      accuracy,
      address,
      timestamp: new Date()
    };

    locations.push(location);

    res.status(201).json({
      success: true,
      message: 'Location saved successfully',
      data: {
        location
      }
    });
  } catch (error) {
    console.error('Create location error:', error);
    res.status(500).json({
      success: false,
      error: 'Internal server error'
    });
  }
});

// Get specific location
router.get('/:id', authenticateToken, validateLocationId, (req: AuthRequest, res: Response) => {
  try {
    // Check validation results
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({
        success: false,
        error: 'Validation failed',
        details: errors.array()
      });
    }

    const location = locations.find(loc => 
      loc.id === req.params.id && loc.userId === req.user?.userId
    );

    if (!location) {
      return res.status(404).json({
        success: false,
        error: 'Location not found'
      });
    }

    res.json({
      success: true,
      data: {
        location
      }
    });
  } catch (error) {
    console.error('Get location error:', error);
    res.status(500).json({
      success: false,
      error: 'Internal server error'
    });
  }
});

// Update location
router.put('/:id', authenticateToken, validateLocationId, validateLocation, (req: AuthRequest, res: Response) => {
  try {
    // Check validation results
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({
        success: false,
        error: 'Validation failed',
        details: errors.array()
      });
    }

    const locationIndex = locations.findIndex(loc => 
      loc.id === req.params.id && loc.userId === req.user?.userId
    );

    if (locationIndex === -1) {
      return res.status(404).json({
        success: false,
        error: 'Location not found'
      });
    }

    const { latitude, longitude, accuracy, address }: LocationInput = req.body;
    
    locations[locationIndex] = {
      ...locations[locationIndex],
      latitude,
      longitude,
      accuracy,
      address
    };

    res.json({
      success: true,
      message: 'Location updated successfully',
      data: {
        location: locations[locationIndex]
      }
    });
  } catch (error) {
    console.error('Update location error:', error);
    res.status(500).json({
      success: false,
      error: 'Internal server error'
    });
  }
});

// Delete location
router.delete('/:id', authenticateToken, validateLocationId, (req: AuthRequest, res: Response) => {
  try {
    // Check validation results
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({
        success: false,
        error: 'Validation failed',
        details: errors.array()
      });
    }

    const locationIndex = locations.findIndex(loc => 
      loc.id === req.params.id && loc.userId === req.user?.userId
    );

    if (locationIndex === -1) {
      return res.status(404).json({
        success: false,
        error: 'Location not found'
      });
    }

    const deletedLocation = locations.splice(locationIndex, 1)[0];
    
    res.json({
      success: true,
      message: 'Location deleted successfully',
      data: {
        location: deletedLocation
      }
    });
  } catch (error) {
    console.error('Delete location error:', error);
    res.status(500).json({
      success: false,
      error: 'Internal server error'
    });
  }
});

// Get location statistics
router.get('/stats/summary', authenticateToken, (req: AuthRequest, res: Response): void => {
  try {
    const userLocations = locations.filter(loc => loc.userId === req.user?.userId);
    
    const stats = {
      totalLocations: userLocations.length,
      firstLocation: userLocations.length > 0 ? 
        userLocations.reduce((earliest, loc) => 
          loc.timestamp < earliest.timestamp ? loc : earliest, userLocations[0]
        ).timestamp : null,
      lastLocation: userLocations.length > 0 ? 
        userLocations.reduce((latest, loc) => 
          loc.timestamp > latest.timestamp ? loc : latest, userLocations[0]
        ).timestamp : null,
      averageAccuracy: userLocations.length > 0 ? 
        userLocations
          .filter(loc => loc.accuracy !== undefined)
          .reduce((sum, loc) => sum + (loc.accuracy || 0), 0) / 
        userLocations.filter(loc => loc.accuracy !== undefined).length : null
    };

    res.json({
      success: true,
      data: {
        stats
      }
    });
  } catch (error) {
    console.error('Get location stats error:', error);
    res.status(500).json({
      success: false,
      error: 'Internal server error'
    });
  }
});

export default router;
