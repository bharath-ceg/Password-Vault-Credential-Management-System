import api from './api';

export const generatorService = {
  generatePassword: async (config) => {
    const response = await api.post('/generator/generate', config);
    return response.data;
  },

  analyzePassword: async (password) => {
    const response = await api.post('/generator/analyze', { password });
    return response.data;
  },
};
