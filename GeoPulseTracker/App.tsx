/**
 * GeoPulse Tracker React Native App
 * Location tracking and management application
 *
 * @format
 */

import React from 'react';
import { StatusBar, StyleSheet, useColorScheme, View, Text } from 'react-native';
import { AuthProvider, useAuth } from './src/contexts/AuthContext';
import LoginScreen from './src/components/LoginScreen';
import HomeScreen from './src/components/HomeScreen';

const AppContent: React.FC = () => {
  const { isAuthenticated, isLoading } = useAuth();
  const isDarkMode = useColorScheme() === 'dark';

  if (isLoading) {
    return (
      <View style={styles.loadingContainer}>
        <Text style={styles.loadingText}>Loading...</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      {isAuthenticated ? (
        <HomeScreen />
      ) : (
        <LoginScreen onLoginSuccess={() => {}} />
      )}
    </View>
  );
};

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f8f9fa',
  },
  loadingText: {
    fontSize: 18,
    color: '#666',
  },
});

export default App;
