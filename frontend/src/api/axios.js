import axios from 'axios';
import { jwtDecode } from 'jwt-decode';

const api = axios.create({
  // Hardcoded relative URL so Vite proxy perfectly intercepts it
  baseURL: '/api', 
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to attach JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      try {
        const decoded = jwtDecode(token);
        if (decoded.userId) config.headers['X-User-Id'] = decoded.userId.toString();
        if (decoded.sub) config.headers['X-User-Email'] = decoded.sub;
      } catch (e) {
        console.error("Failed to decode token", e);
      }
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => {
    if (response.data && response.data.hasOwnProperty('data') && response.data.hasOwnProperty('message')) {
      response.data = response.data.data;
    }
    return response;
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      const authEndpoints = ['/auth/login', '/auth/register', '/auth/verify-otp'];
      if (!authEndpoints.some(endpoint => error.config.url.includes(endpoint))) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
