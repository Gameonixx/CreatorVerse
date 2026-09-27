import { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [hasCreatorMode, setHasCreatorMode] = useState(false);
  const [hasBrandMode, setHasBrandMode] = useState(false);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [loading, setLoading] = useState(true);

  const performLocalLogout = () => {
    setToken(null);
    setUser(null);
    setHasCreatorMode(false);
    setHasBrandMode(false);
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  };

  useEffect(() => {
    const handleAuthInvalidated = () => {
      performLocalLogout();
    };
    window.addEventListener('auth:invalidated', handleAuthInvalidated);
    return () => window.removeEventListener('auth:invalidated', handleAuthInvalidated);
  }, []);

  // Hydrate user from /me endpoint
  useEffect(() => {
    const fetchMe = async () => {
      try {
        const response = await api.get('/users/me');
        setUser(response);
        localStorage.setItem('user', JSON.stringify(response));
        await checkProfileModes(response.id);
      } catch (err) {
        console.error("Failed to fetch user profile", err);
        // Do not resurrect cached user on failure.
        // The api.js interceptor might have already cleared storage and fired auth:invalidated,
        // but we ensure local state is cleared here too if it failed unrecoverably.
        performLocalLogout();
      } finally {
        setLoading(false);
      }
    };

    if (token) {
      fetchMe();
    } else {
      setLoading(false);
    }
  }, [token]);

  const login = async (credentials) => {
    try {
      const response = await api.post('/auth/login', credentials);
      if (response.accessToken) {
        setToken(response.accessToken);
        localStorage.setItem('token', response.accessToken);
        if (response.refreshToken) {
          localStorage.setItem('refreshToken', response.refreshToken);
        }
        // Temporarily, we extract the username from credentials to set simple user obj
        // In the future, this would come from the JWT claims or a /me endpoint.
        const userObj = {
          id: response.userId,
          username: credentials.usernameOrEmail,
          role: 'USER'
        };
        setUser(userObj);
        localStorage.setItem('user', JSON.stringify(userObj));
        await checkProfileModes(response.userId);
      }
      return response;
    } catch (error) {
      throw error;
    }
  };

  const register = async (userData) => {
    try {
      const response = await api.post('/auth/register', userData);
      return response;
    } catch (error) {
      throw error;
    }
  };

  const logout = async () => {
    const refreshToken = localStorage.getItem('refreshToken');
    if (refreshToken) {
      try {
        await api.post('/auth/logout', { refreshToken });
      } catch (err) {
        console.error('Backend logout failed', err);
      }
    }
    performLocalLogout();
  };

  const checkProfileModes = async (userId) => {
    if (!userId) return;
    try {
      await api.get(`/creators/profile/${userId}`);
      setHasCreatorMode(true);
    } catch {
      setHasCreatorMode(false);
    }

    try {
      await api.get(`/brands/profile/${userId}`);
      setHasBrandMode(true);
    } catch {
      setHasBrandMode(false);
    }
  };

  const refreshProfileModes = async () => {
    if (user && user.id) {
      await checkProfileModes(user.id);
    }
  };

  const refreshUser = async () => {
    if (token) {
      try {
        const response = await api.get('/users/me');
        setUser(response);
        localStorage.setItem('user', JSON.stringify(response));
        await checkProfileModes(response.id);
      } catch (err) {
        console.error("Failed to refresh user profile", err);
      }
    }
  };

  return (
    <AuthContext.Provider value={{ user, token, hasCreatorMode, hasBrandMode, refreshProfileModes, refreshUser, login, register, logout, loading }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
