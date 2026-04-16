import api from './axios';

export const authApi = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  me: () => api.get('/auth/me'),
};

export const policyApi = {
  create: (data) => api.post('/policies/create', data),
  getMy: () => api.get('/policies/my'),
  getById: (id) => api.get(`/policies/${id}`),
  renew: (id) => api.put(`/policies/${id}/renew`),
  pause: (id) => api.put(`/policies/${id}/pause`),
  deactivate: (id) => api.put(`/policies/${id}/deactivate`),
};

export const premiumApi = {
  calculate: () => api.post('/premium/calculate'),
  explain: (userId) => api.get(`/premium/explain/${userId}`),
};

export const userApi = {
  getProfile: () => api.get('/users/profile'),
  updateProfile: (data) => api.put('/users/profile', data),
};

export const claimsApi = {
  getMy: () => api.get('/claims/my'),
  getById: (id) => api.get(`/claims/${id}`),
};

export const triggerApi = {
  getLive: () => api.get('/triggers/live'),
  scanCity: (city) => api.post('/triggers/scan', { city }),
  scanAll: () => api.post('/triggers/scan-all'),
};

export const adminApi = {
  getDashboard: () => api.get('/admin/dashboard'),
  getUsers: () => api.get('/admin/users'),
  getPolicies: () => api.get('/admin/policies'),
  getClaims: () => api.get('/admin/claims'),
  getRiskZones: () => api.get('/admin/risk-zones'),
};
