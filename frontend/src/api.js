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
  register: (userData) => api.post('/auth/register', userData),
  login: (credentials) => api.post('/auth/login', credentials),
};

// TodoList API
export const todoListAPI = {
  getAll: () => api.get('/todolists'),
  getById: (id) => api.get(`/todolists/${id}`),
  create: (data) => api.post('/todolists', data),
  update: (id, data) => api.put(`/todolists/${id}`, data),
  delete: (id) => api.delete(`/todolists/${id}`),
};

// TodoItem API
export const todoItemAPI = {
  getAll: (listId, params = {}) => 
    api.get(`/todolists/${listId}/items`, { params }),
  getById: (listId, itemId) => 
    api.get(`/todolists/${listId}/items/${itemId}`),
  create: (listId, data) => 
    api.post(`/todolists/${listId}/items`, data),
  update: (listId, itemId, data) => 
    api.put(`/todolists/${listId}/items/${itemId}`, data),
  markComplete: (listId, itemId) => 
    api.patch(`/todolists/${listId}/items/${itemId}/complete`),
  delete: (listId, itemId) => 
    api.delete(`/todolists/${listId}/items/${itemId}`),
  addDependency: (listId, itemId, dependencyId) => 
    api.post(`/todolists/${listId}/items/${itemId}/dependencies/${dependencyId}`),
  removeDependency: (listId, itemId, dependencyId) => 
    api.delete(`/todolists/${listId}/items/${itemId}/dependencies/${dependencyId}`),
};

export default api;
