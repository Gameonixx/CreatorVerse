import { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [hasCreatorMode, setHasCreatorMode] = useState(false);
  const [hasBrandMode, setHasBrandMode] = useState(false);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [loading, setLoading] = useState(true);

  // Hydrate user from /me endpoint, falling back to local storage
  useEffect(() => {
    const fetchMe = async () => {
      try {
        const response = await api.get('/users/me');
        setUser(response);
        localStorage.setItem('user', JSON.stringify(response));
        await checkProfileModes(response.id);
      } catch (err) {
        console.error("Failed to fetch user profile", err);
        const savedUser = localStorage.getItem('user');
        if (savedUser) {
          const parsedUser = JSON.parse(savedUser);
          setUser(parsedUser);
          await checkProfileModes(parsedUser.id);
        }
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

  const logout = () => {
    setToken(null);
    setUser(null);
    setHasCreatorMode(false);
    setHasBrandMode(false);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
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

  return (
    <AuthContext.Provider value={{ user, token, hasCreatorMode, hasBrandMode, refreshProfileModes, login, register, logout, loading }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
