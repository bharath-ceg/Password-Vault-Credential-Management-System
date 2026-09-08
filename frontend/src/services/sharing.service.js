import api from './api';

export const sharingService = {
  createShare: async (shareData) => {
    const response = await api.post('/shares', shareData);
    return response.data;
  },

  getMyShares: async () => {
    const response = await api.get('/shares/my-shares');
    return response.data;
  },

  getSharedWithMe: async () => {
    const response = await api.get('/shares/shared-with-me');
    return response.data;
  },

  updateShare: async (id, updateData) => {
    const response = await api.put(`/shares/${id}`, updateData);
    return response.data;
  },

  revokeShare: async (id) => {
    const response = await api.delete(`/shares/${id}`);
    return response.data;
  },

  revealSharedPassword: async (id, privacyPassword) => {
    const response = await api.post(`/shares/${id}/reveal`, { privacyPassword });
    return response.data;
  },
};
