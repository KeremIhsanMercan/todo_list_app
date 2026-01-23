import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

// Create axios instance with interceptor for auth token
const api = axios.create({
  baseURL: API_URL,
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
  updateUser: (userData) => api.put('/auth/update', userData),
  deleteAccount: (data) => api.delete('/auth/delete', { data }),
};

// TodoList API
export const todoListAPI = {
  getAll: () => api.get('/lists'),
  getById: (id) => api.get(`/lists/${id}`),
  create: (data) => api.post('/lists', data),
  update: (id, data) => api.put(`/lists/${id}`, data),
  delete: (id) => api.delete(`/lists/${id}`),
};

// TodoItem API
export const todoItemAPI = {
  getAll: (listId, params = {}) => 
    api.get(`/lists/${listId}/items`, { params }),
  getById: (listId, itemId) => 
    api.get(`/lists/${listId}/items/${itemId}`),
  create: (listId, data) => 
    api.post(`/lists/${listId}/items`, data),
  update: (listId, itemId, data) => 
    api.put(`/lists/${listId}/items/${itemId}`, data),
  markComplete: (listId, itemId) => 
    api.patch(`/lists/${listId}/items/${itemId}/complete`),
  delete: (listId, itemId) => 
    api.delete(`/lists/${listId}/items/${itemId}`),
  addDependency: (listId, itemId, dependencyId) => 
    api.post(`/lists/${listId}/items/${itemId}/dependencies/${dependencyId}`),
  removeDependency: (listId, itemId, dependencyId) => 
    api.delete(`/lists/${listId}/items/${itemId}/dependencies/${dependencyId}`),
};

export default api;
