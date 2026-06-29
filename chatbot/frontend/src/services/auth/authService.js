import axiosInstance from '../api/axiosConfig';

/**
 * Authentication Service
 * 
 * Handles all authentication-related API calls
 */
const authService = {
  
  /**
   * Register new user
   */
  register: async (userData) => {
    const response = await axiosInstance.post('/auth/register', userData);
    
    // Save token and user info
    if (response.data.success && response.data.data.token) {
      localStorage.setItem('token', response.data.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.data));
    }
    
    return response.data;
  },

  /**
   * Login user
   */
  login: async (credentials) => {
    const response = await axiosInstance.post('/auth/login', credentials);
    
    // Save token and user info
    if (response.data.success && response.data.data.token) {
      localStorage.setItem('token', response.data.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.data));
    }
    
    return response.data;
  },

  /**
   * Logout user
   */
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  /**
   * Get current user
   */
  getCurrentUser: () => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  /**
   * Check if user is authenticated
   */
  isAuthenticated: () => {
    return !!localStorage.getItem('token');
  },
};

export default authService;
