import api from './api';

export const securityService = {
  getLoginActivity: async () => {
    const response = await api.get('/security/login-activity');
    return response.data;
  },

  markLoginActivityAsRead: async () => {
    const response = await api.post('/security/login-activity/read-all');
    return response.data;
  },

  getSuspiciousActivity: async () => {
    const response = await api.get('/security/suspicious-activity');
    return response.data;
  },

  markSuspiciousActivityAsRead: async (id) => {
    const response = await api.patch(`/security/suspicious-activity/${id}/read`);
    return response.data;
  },

  markAllSuspiciousActivitiesAsRead: async () => {
    const response = await api.post('/security/suspicious-activity/read-all');
    return response.data;
  },

  getSecurityAlerts: async () => {
    const response = await api.get('/security/alerts');
    return response.data;
  },

  markAlertAsRead: async (alertId) => {
    const response = await api.patch(`/security/alerts/${alertId}/read`);
    return response.data;
  },

  markAllAlertsAsRead: async () => {
    const response = await api.post('/security/alerts/read-all');
    return response.data;
  },

  getAuditLogs: async () => {
    const response = await api.get('/security/audit-logs');
    return response.data;
  },

  markAuditLogsAsRead: async () => {
    const response = await api.post('/security/audit-logs/read-all');
    return response.data;
  },

  getUnreadCounts: async () => {
    const response = await api.get('/security/unread-counts');
    return response.data;
  },

  getSecurityAnalytics: async () => {
    const response = await api.get('/security/analytics');
    return response.data;
  },
};
