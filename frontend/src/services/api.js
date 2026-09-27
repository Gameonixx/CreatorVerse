const getAuthToken = () => localStorage.getItem('token');

let refreshPromise = null;

const refreshTokenFlow = async () => {
  const refreshToken = localStorage.getItem('refreshToken');
  if (!refreshToken) {
    throw new Error('No refresh token available');
  }

  const baseUrl = import.meta.env.VITE_API_URL || '/api';
  const response = await fetch(`${baseUrl}/auth/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken })
  });

  if (!response.ok) {
    throw new Error('Refresh failed');
  }

  const data = await response.json();
  localStorage.setItem('token', data.accessToken);
  localStorage.setItem('refreshToken', data.refreshToken);
  return data.accessToken;
};

const request = async (endpoint, options = {}, isRetry = false) => {
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

  const baseUrl = import.meta.env.VITE_API_URL || '/api';
  const url = `${baseUrl}${endpoint}`;

  let response = await fetch(url, {
    ...options,
    headers,
  });

  if ((response.status === 401 || response.status === 403) && !isRetry) {
    const hasRefreshToken = !!localStorage.getItem('refreshToken');
    if (hasRefreshToken) {
      let isAuthFailure = response.status === 401;

      if (response.status === 403) {
        if (token) {
          try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            // Check if expired or within 10 seconds of expiring
            if (Date.now() >= (payload.exp * 1000) - 10000) {
              isAuthFailure = true;
            }
          } catch (e) {
            isAuthFailure = true;
          }
        } else {
          isAuthFailure = true;
        }
      }

      if (isAuthFailure) {
        if (!refreshPromise) {
          refreshPromise = refreshTokenFlow().finally(() => {
            refreshPromise = null;
          });
        }

        try {
          const newToken = await refreshPromise;
          headers['Authorization'] = `Bearer ${newToken}`;
          response = await fetch(url, { ...options, headers });
        } catch (refreshErr) {
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
          localStorage.removeItem('user');
          window.dispatchEvent(new CustomEvent('auth:invalidated'));

          const error = new Error('Session expired');
          error.status = 401;
          throw error;
        }
      }
    }
  }

  if (!response.ok) {
    const errorData = await response.json().catch(() => null);
    const error = new Error(errorData?.message || errorData?.error || 'API Request failed');
    error.status = response.status;
    error.data = errorData;

    // Suppress console error for expected 404s (e.g. optional BrandProfile missing)
    const isExpected404 = response.status === 404 && endpoint.includes('/brands/profile');

    if (!isExpected404) {
      // Log the actual error for development diagnostics
      console.error(`[API Error] ${options.method || 'GET'} ${endpoint} failed with status ${response.status}:`, {
        errorData,
        url: response.url
      });
    }

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

export const collaborationApi = {
  getMyCollaborations: (page = 0, size = 10, sortBy = 'createdAt', sortDir = 'desc') => 
    api.get(`/collaborations/mine?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`),
  getCollaboration: (id) => api.get(`/collaborations/${id}`),
  updateStatus: (id, status) => api.put(`/collaborations/${id}/status`, { status }),
  
  // Deliverables
  getDeliverables: (id) => api.get(`/collaborations/${id}/deliverables`),
  createDeliverable: (id, data) => api.post(`/collaborations/${id}/deliverables`, data),
  updateDeliverable: (id, deliverableId, data) => api.put(`/collaborations/${id}/deliverables/${deliverableId}`, data),
  deleteDeliverable: (id, deliverableId) => api.delete(`/collaborations/${id}/deliverables/${deliverableId}`),
  submitDeliverable: (id, deliverableId, submissionUrl) => api.put(`/collaborations/${id}/deliverables/${deliverableId}/submission`, { submissionUrl }),
  reviewDeliverable: (id, deliverableId, status, feedback) => api.put(`/collaborations/${id}/deliverables/${deliverableId}/review`, { status, feedback }),

  // Messaging
  getConversation: (id) => api.get(`/collaborations/${id}/conversation`),
  getMessages: (id, page = 0, size = 50) => api.get(`/collaborations/${id}/messages?page=${page}&size=${size}`),
  sendMessage: (id, content) => api.post(`/collaborations/${id}/messages`, { content }),
  markMessagesAsRead: (id) => api.put(`/collaborations/${id}/messages/read`, {}),
};

export const notificationApi = {
  getNotifications: (page = 0, size = 20) => api.get(`/notifications?page=${page}&size=${size}`),
  getUnreadCount: () => api.get('/notifications/unread-count'),
  markAsRead: (id) => api.post(`/notifications/${id}/read`, {}),
  markAllAsRead: () => api.post('/notifications/read-all', {}),
};
