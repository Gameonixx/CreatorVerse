const getAuthToken = () => localStorage.getItem('token');

const request = async (endpoint, options = {}) => {
  const token = getAuthToken();
  const headers = {
    ...options.headers,
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  // If the body is FormData, do not set Content-Type.
  // The browser will set it automatically with the boundary.
  if (!(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }

  const response = await fetch(`/api${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => null);
    const error = new Error(errorData?.message || errorData?.error || 'API Request failed');
    error.status = response.status;
    error.data = errorData;
    
    // Log the actual error for development diagnostics
    console.error(`[API Error] ${options.method || 'GET'} ${endpoint} failed with status ${response.status}:`, {
      errorData,
      url: response.url
    });
    
    throw error;
  }

  // For 204 No Content, there is no JSON to parse
  if (response.status === 204) {
    return null;
  }

  return response.json();
};

export const api = {
  get: (endpoint, options) => request(endpoint, { method: 'GET', ...options }),
  
  post: (endpoint, data, options = {}) => {
    const isFormData = data instanceof FormData;
    return request(endpoint, {
      method: 'POST',
      body: isFormData ? data : JSON.stringify(data),
      ...options,
    });
  },

  put: (endpoint, data, options = {}) => {
    const isFormData = data instanceof FormData;
    return request(endpoint, {
      method: 'PUT',
      body: isFormData ? data : JSON.stringify(data),
      ...options,
    });
  },

  delete: (endpoint, options) => request(endpoint, { method: 'DELETE', ...options }),
};

export const socialApi = {
  followUser: (userId) => api.post(`/social/follow/${userId}`),
  unfollowUser: (userId) => api.delete(`/social/unfollow/${userId}`),
  getFollowers: (userId) => api.get(`/social/followers/${userId}`),
  getFollowing: (userId) => api.get(`/social/following/${userId}`),
};
