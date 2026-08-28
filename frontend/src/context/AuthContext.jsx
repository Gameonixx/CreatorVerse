import { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [loading, setLoading] = useState(true);

  // In a real app, you would probably hit a /me endpoint to validate the token 
  // and fetch the user profile on mount. For now, since CreatorVerse Phase 4 
  // backend doesn't necessarily have a standard /me returning full profile yet,
  // we will try to decode it or store the user object locally.
  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    if (token && savedUser) {
      setUser(JSON.parse(savedUser));
    }
    setLoading(false);
  }, [token]);

  const login = async (credentials) => {
    try {
      const response = await api.post('/auth/login', credentials);
      if (response.accessToken) {
        setToken(response.accessToken);
        localStorage.setItem('token', response.accessToken);
        // Temporarily, we extract the username from credentials to set simple user obj
        // In the future, this would come from the JWT claims or a /me endpoint.
        const userObj = { username: credentials.usernameOrEmail, role: 'USER' }; 
        setUser(userObj);
        localStorage.setItem('user', JSON.stringify(userObj));
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
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };

  return (
    <AuthContext.Provider value={{ user, token, login, register, logout, loading }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
