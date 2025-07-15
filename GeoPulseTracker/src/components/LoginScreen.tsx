import React, { useState } from 'react';
import {
  View,
  Text,
  ScrollView,
  StyleSheet,
  Alert,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import CustomInput from '../components/CustomInput';
import CustomButton from '../components/CustomButton';
import AuthService from '../services/AuthService';
import { LoginCredentials, RegisterCredentials } from '../types/auth';

interface LoginScreenProps {
  onLoginSuccess: () => void;
}

const LoginScreen: React.FC<LoginScreenProps> = ({ onLoginSuccess }) => {
  const [isLogin, setIsLogin] = useState(true);
  const [loading, setLoading] = useState(false);
  
  // Login form state
  const [loginForm, setLoginForm] = useState<LoginCredentials>({
    email: '',
    password: '',
  });
  
  // Register form state
  const [registerForm, setRegisterForm] = useState<RegisterCredentials>({
    username: '',
    email: '',
    password: '',
  });
  
  // Form errors
  const [errors, setErrors] = useState<Record<string, string>>({});

  const validateLoginForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!loginForm.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(loginForm.email)) {
      newErrors.email = 'Invalid email format';
    }

    if (!loginForm.password.trim()) {
      newErrors.password = 'Password is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const validateRegisterForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!registerForm.username.trim()) {
      newErrors.username = 'Username is required';
    } else if (registerForm.username.length < 3) {
      newErrors.username = 'Username must be at least 3 characters';
    }

    if (!registerForm.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(registerForm.email)) {
      newErrors.email = 'Invalid email format';
    }

    if (!registerForm.password.trim()) {
      newErrors.password = 'Password is required';
    } else if (registerForm.password.length < 8) {
      newErrors.password = 'Password must be at least 8 characters';
    } else if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/.test(registerForm.password)) {
      newErrors.password = 'Password must contain uppercase, lowercase, number, and special character';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleLogin = async () => {
    if (!validateLoginForm()) return;

    setLoading(true);
    try {
      const response = await AuthService.login(loginForm);
      
      if (response.success) {
        Alert.alert('Success', 'Login successful!');
        onLoginSuccess();
      } else {
        if (response.details) {
          const newErrors: Record<string, string> = {};
          response.details.forEach(detail => {
            newErrors[detail.field] = detail.message;
          });
          setErrors(newErrors);
        } else {
          Alert.alert('Error', response.error || 'Login failed');
        }
      }
    } catch (error) {
      Alert.alert('Error', 'Network error. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async () => {
    if (!validateRegisterForm()) return;

    setLoading(true);
    try {
      const response = await AuthService.register(registerForm);
      
      if (response.success) {
        Alert.alert('Success', 'Registration successful!');
        onLoginSuccess();
      } else {
        if (response.details) {
          const newErrors: Record<string, string> = {};
          response.details.forEach(detail => {
            newErrors[detail.field] = detail.message;
          });
          setErrors(newErrors);
        } else {
          Alert.alert('Error', response.error || 'Registration failed');
        }
      }
    } catch (error) {
      Alert.alert('Error', 'Network error. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const switchMode = () => {
    setIsLogin(!isLogin);
    setErrors({});
    if (isLogin) {
      setRegisterForm({ username: '', email: '', password: '' });
    } else {
      setLoginForm({ email: '', password: '' });
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <ScrollView
        contentContainerStyle={styles.scrollContent}
        keyboardShouldPersistTaps="handled"
      >
        <View style={styles.header}>
          <Text style={styles.title}>GeoPulse Tracker</Text>
          <Text style={styles.subtitle}>
            {isLogin ? 'Welcome back!' : 'Create your account'}
          </Text>
        </View>

        <View style={styles.form}>
          {!isLogin && (
            <CustomInput
              label="Username"
              placeholder="Enter your username"
              value={registerForm.username}
              onChangeText={(text) => setRegisterForm({ ...registerForm, username: text })}
              error={errors.username}
              autoCapitalize="none"
            />
          )}

          <CustomInput
            label="Email"
            placeholder="Enter your email"
            value={isLogin ? loginForm.email : registerForm.email}
            onChangeText={(text) => {
              if (isLogin) {
                setLoginForm({ ...loginForm, email: text });
              } else {
                setRegisterForm({ ...registerForm, email: text });
              }
            }}
            error={errors.email}
            keyboardType="email-address"
            autoCapitalize="none"
          />

          <CustomInput
            label="Password"
            placeholder="Enter your password"
            value={isLogin ? loginForm.password : registerForm.password}
            onChangeText={(text) => {
              if (isLogin) {
                setLoginForm({ ...loginForm, password: text });
              } else {
                setRegisterForm({ ...registerForm, password: text });
              }
            }}
            error={errors.password}
            isPassword={true}
            autoCapitalize="none"
          />

          <CustomButton
            title={isLogin ? 'Sign In' : 'Sign Up'}
            onPress={isLogin ? handleLogin : handleRegister}
            loading={loading}
            style={styles.submitButton}
          />

          <CustomButton
            title={isLogin ? 'Create an account' : 'Already have an account?'}
            onPress={switchMode}
            variant="outline"
            style={styles.switchButton}
          />
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8f9fa',
  },
  scrollContent: {
    flexGrow: 1,
    justifyContent: 'center',
    padding: 24,
  },
  header: {
    alignItems: 'center',
    marginBottom: 48,
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#2196F3',
    marginBottom: 8,
    textAlign: 'center',
  },
  subtitle: {
    fontSize: 16,
    color: '#666',
    textAlign: 'center',
  },
  form: {
    backgroundColor: '#fff',
    borderRadius: 16,
    padding: 24,
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 4,
  },
  submitButton: {
    marginTop: 8,
    marginBottom: 16,
  },
  switchButton: {
    marginTop: 8,
  },
});

export default LoginScreen;
