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

export const claimsApi = {
  getMy: () => api.get('/claims/my'),
  getById: (id) => api.get(`/claims/${id}`),
  approve: (id) => api.put(`/claims/${id}/approve`),
  reject: (id) => api.put(`/claims/${id}/reject`),
  markPaid: (id) => api.put(`/claims/${id}/mark-paid`),
};

export const triggerApi = {
  getLive: () => api.get('/triggers/live'),
  createMockEvent: (data) => api.post('/triggers/mock-event', data),
  evaluateAll: (data) => api.post('/triggers/evaluate-all', data),
  evaluateForUser: (userId, data) => api.post(`/triggers/evaluate/${userId}`, data),
};

export const adminApi = {
  getDashboard: () => api.get('/admin/dashboard'),
  getUsers: () => api.get('/admin/users'),
  getPolicies: () => api.get('/admin/policies'),
  getClaims: () => api.get('/admin/claims'),
  getRiskZones: () => api.get('/admin/risk-zones'),
};
