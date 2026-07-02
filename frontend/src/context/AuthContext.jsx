import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/axios';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [loading, setLoading] = useState(true);
  const [pendingCredentials, setPendingCredentials] = useState(null); // stores {email, password} during OTP step

  const decodeToken = (t) => {
    try {
      const payload = JSON.parse(atob(t.split('.')[1]));
      return {
        token: t,
        id: payload.userId,       // userId claim (Long)
        email: payload.sub,       // sub = email string
        role: payload.role || 'ROLE_USER'
      };
    } catch (e) {
      return { token: t, role: 'ROLE_USER' };
    }
  };

  useEffect(() => {
    if (token) {
      setUser(decodeToken(token));
    }
    setLoading(false);
  }, [token]);

  const login = async (credentials) => {
    const response = await api.post('/auth/login', credentials);
    const responseData = response.data;

    if (responseData && responseData.requiresOtp) {
      setPendingCredentials(credentials); // save {email, password} for OTP step
      return { requiresOtp: true };
    }

    // Direct login (no OTP) — shouldn't happen with current backend but handle it
    const newToken = responseData.token;
    localStorage.setItem('token', newToken);
    setToken(newToken);
    setUser(decodeToken(newToken));
    return { success: true };
  };

  const verifyOtp = async ({ email, otp }) => {
    const savedCreds = pendingCredentials || {};
    const response = await api.post('/auth/verify-otp', {
      email,
      otp,
      password: savedCreds.password || ''  // required by backend for re-verification
    });
    const responseData = response.data;
    const newToken = responseData.token;
    localStorage.setItem('token', newToken);
    setToken(newToken);
    setUser(decodeToken(newToken));
    setPendingCredentials(null); // clear stored creds
    return { success: true };
  };

  const register = async (userData) => {
    await api.post('/auth/register', userData);
    return true;
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  };

  const updateUserRole = (newRole) => {
    if (user) setUser({ ...user, role: newRole });
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, verifyOtp, register, logout, updateUserRole }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);