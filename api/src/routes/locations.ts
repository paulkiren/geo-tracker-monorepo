import { Router, Response } from 'express';
import { authenticateToken } from '../middleware/auth';
import { AuthRequest, Location } from '../types';

const router = Router();

// Mock location storage (replace with database in production)
const locations: Location[] = [];

// Get user's locations
router.get('/', authenticateToken, (req: AuthRequest, res: Response) => {
  const userLocations = locations.filter(loc => loc.userId === req.user?.userId);
  res.json({ locations: userLocations });
});

// Add new location
router.post('/', authenticateToken, (req: AuthRequest, res: Response) => {
  try {
    const { latitude, longitude, accuracy, address } = req.body;

    if (!latitude || !longitude) {
      return res.status(400).json({ error: 'Latitude and longitude are required' });
    }

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
      message: 'Location saved successfully',
      location
    });
  } catch (error) {
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Get specific location
router.get('/:id', authenticateToken, (req: AuthRequest, res: Response) => {
  const location = locations.find(loc => 
    loc.id === req.params.id && loc.userId === req.user?.userId
  );

  if (!location) {
    return res.status(404).json({ error: 'Location not found' });
  }

  res.json({ location });
});

// Update location
router.put('/:id', authenticateToken, (req: AuthRequest, res: Response) => {
  const locationIndex = locations.findIndex(loc => 
    loc.id === req.params.id && loc.userId === req.user?.userId
  );

  if (locationIndex === -1) {
    return res.status(404).json({ error: 'Location not found' });
  }

  const { latitude, longitude, accuracy, address } = req.body;
  
  locations[locationIndex] = {
    ...locations[locationIndex],
    latitude: latitude || locations[locationIndex].latitude,
    longitude: longitude || locations[locationIndex].longitude,
    accuracy: accuracy || locations[locationIndex].accuracy,
    address: address || locations[locationIndex].address
  };

  res.json({
    message: 'Location updated successfully',
    location: locations[locationIndex]
  });
});

// Delete location
router.delete('/:id', authenticateToken, (req: AuthRequest, res: Response) => {
  const locationIndex = locations.findIndex(loc => 
    loc.id === req.params.id && loc.userId === req.user?.userId
  );

  if (locationIndex === -1) {
    return res.status(404).json({ error: 'Location not found' });
  }

  locations.splice(locationIndex, 1);
  res.json({ message: 'Location deleted successfully' });
});

export default router;
