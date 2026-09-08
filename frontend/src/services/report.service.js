import api from './api';

export const reportService = {
  getPasswordHealthReport: async () => {
    const response = await api.get('/reports/password-health');
    return response.data;
  },

  getLoginActivityReport: async () => {
    const response = await api.get('/reports/login-activity');
    return response.data;
  },
};
