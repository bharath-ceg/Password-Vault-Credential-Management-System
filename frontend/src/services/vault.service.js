import api from './api';

export const vaultService = {
  createCredential: async (credentialData) => {
    const response = await api.post('/vault', credentialData);
    return response.data;
  },

  getCredentials: async (category = null) => {
    const url = category ? `/vault?category=${category}` : '/vault';
    const response = await api.get(url);
    return response.data;
  },

  searchCredentials: async (query) => {
    const response = await api.get(`/vault/search?query=${encodeURIComponent(query)}`);
    return response.data;
  },

  updateCredential: async (id, credentialData) => {
    const response = await api.put(`/vault/${id}`, credentialData);
    return response.data;
  },

  deleteCredential: async (id) => {
    const response = await api.delete(`/vault/${id}`);
    return response.data;
  },

  revealPassword: async (id, privacyPassword) => {
    const response = await api.post(`/vault/${id}/reveal`, { privacyPassword });
    return response.data;
  },

  getPrivacyPasswordStatus: async () => {
    const response = await api.get('/vault/privacy-password/status');
    return response.data;
  },

  setPrivacyPassword: async (privacyPassword) => {
    const response = await api.post('/vault/privacy-password', { privacyPassword });
    return response.data;
  },

  changePrivacyPassword: async (changeData) => {
    const response = await api.post('/vault/change-privacy-password', changeData);
    return response.data;
  },
};
