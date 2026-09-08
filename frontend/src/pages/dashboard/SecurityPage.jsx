import React, { useState, useEffect } from 'react';
import { 
  ShieldAlert, 
  Activity, 
  AlertTriangle, 
  FileText, 
  Bell, 
  Check, 
  RefreshCw,
  CheckCircle2
} from 'lucide-react';
import { securityService } from '../../services/security.service';

const SecurityPage = () => {
  const [activeTab, setActiveTab] = useState('login-activity');
  
  // Data states
  const [loginLogs, setLoginLogs] = useState([]);
  const [suspiciousActivities, setSuspiciousActivities] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [unreadCounts, setUnreadCounts] = useState({
    login: 0,
    suspicious: 0,
    audit: 0
  });

  // UI states
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [markingReadId, setMarkingReadId] = useState(null);

  const handleTabChange = (tabId) => {
    setActiveTab(tabId);
    
    // Task 1: Login Activity badge disappears ONLY when explicitly switching away from Login Activity tab
    if (tabId !== 'login-activity' && unreadCounts.login > 0) {
      securityService.markLoginActivityAsRead().catch(console.error);
      setUnreadCounts(prev => ({ ...prev, login: 0 }));
    }

    if (tabId === 'suspicious-activity' && unreadCounts.suspicious > 0) {
      securityService.markAllSuspiciousActivitiesAsRead().catch(console.error);
      setUnreadCounts(prev => ({ ...prev, suspicious: 0 }));
    } else if (tabId === 'audit-logs' && unreadCounts.audit > 0) {
      securityService.markAuditLogsAsRead().catch(console.error);
      setUnreadCounts(prev => ({ ...prev, audit: 0 }));
    }
  };

  const fetchSecurityData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [loginRes, suspiciousRes, alertRes, auditRes, unreadRes] = await Promise.all([
        securityService.getLoginActivity(),
        securityService.getSuspiciousActivity(),
        securityService.getSecurityAlerts(),
        securityService.getAuditLogs(),
        securityService.getUnreadCounts(),
      ]);

      setLoginLogs(loginRes?.data || []);
      setSuspiciousActivities(suspiciousRes?.data || []);
      setAlerts(alertRes?.data || []);
      setAuditLogs(auditRes?.data || []);

      const counts = unreadRes?.data || {};
      let initialLoginUnread = counts.loginActivityUnread || 0;
      let initialSuspiciousUnread = counts.suspiciousActivityUnread || 0;
      let initialAuditUnread = counts.auditLogsUnread || 0;

      // Automatically opening/selecting Login Activity on route entry MUST NOT clear its badge.
      // Clear unread on load ONLY for other tabs if they happen to be active.
      if (activeTab === 'suspicious-activity' && initialSuspiciousUnread > 0) {
        securityService.markAllSuspiciousActivitiesAsRead().catch(console.error);
        initialSuspiciousUnread = 0;
      } else if (activeTab === 'audit-logs' && initialAuditUnread > 0) {
        securityService.markAuditLogsAsRead().catch(console.error);
        initialAuditUnread = 0;
      }

      setUnreadCounts({
        login: initialLoginUnread,
        suspicious: initialSuspiciousUnread,
        audit: initialAuditUnread,
      });
    } catch (err) {
      console.error('Error fetching security data:', err);
      setError(err?.response?.data?.message || 'Failed to load security records.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSecurityData();
  }, []);

  const handleMarkAlertAsRead = async (alertId) => {
    setMarkingReadId(alertId);
    try {
      const response = await securityService.markAlertAsRead(alertId);
      setAlerts((prev) =>
        prev.map((a) => (a.id === alertId ? { ...a, status: 'READ', readAt: response?.data?.readAt || new Date().toISOString() } : a))
      );
    } catch (err) {
      console.error('Failed to mark alert as read:', err);
    } finally {
      setMarkingReadId(null);
    }
  };

  const formatDate = (isoString) => {
    if (!isoString) return 'N/A';
    try {
      const date = new Date(isoString);
      const day = String(date.getDate()).padStart(2, '0');
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const year = date.getFullYear();
      return `${day}-${month}-${year}`;
    } catch (e) {
      return isoString;
    }
  };

  const formatTime = (isoString) => {
    if (!isoString) return 'N/A';
    try {
      const date = new Date(isoString);
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const seconds = String(date.getSeconds()).padStart(2, '0');
      return `${hours}:${minutes}:${seconds}`;
    } catch (e) {
      return isoString;
    }
  };

  const getActionBadgeStyle = (action) => {
    const act = (action || '').toUpperCase();
    if (act.includes('SECURITY_ALERT') || act.includes('ALERT_CREATED')) {
      return 'bg-red-50 text-red-700 border-red-200 font-semibold';
    }
    if (act.includes('SUSPICIOUS')) {
      return 'bg-amber-50 text-amber-800 border-amber-200 font-semibold';
    }
    if (act.includes('FAILED')) {
      return 'bg-rose-50 text-rose-700 border-rose-200 font-semibold';
    }
    if (act.includes('SUCCESS')) {
      return 'bg-emerald-50 text-emerald-700 border-emerald-200 font-semibold';
    }
    if (act.includes('VAULT')) {
      return 'bg-blue-50 text-blue-700 border-blue-200 font-semibold';
    }
    if (act.includes('SHARING')) {
      return 'bg-indigo-50 text-indigo-700 border-indigo-200 font-semibold';
    }
    return 'bg-slate-100 text-slate-700 border-slate-200 font-medium';
  };

  const unreadAlertsCount = alerts.filter(a => a.status === 'UNREAD').length;

  const tabs = [
    { 
      id: 'login-activity', 
      label: 'Login Activity', 
      icon: Activity,
      count: unreadCounts.login,
      badgeStyle: 'bg-[#06B6D4] text-white'
    },
    { 
      id: 'suspicious-activity', 
      label: 'Suspicious Activity', 
      icon: AlertTriangle,
      count: unreadCounts.suspicious,
      badgeStyle: 'bg-amber-500 text-white'
    },
    { 
      id: 'security-alerts', 
      label: 'Security Alerts', 
      icon: Bell,
      count: unreadAlertsCount,
      badgeStyle: 'bg-red-600 text-white'
    },
    { 
      id: 'audit-logs', 
      label: 'Audit Logs', 
      icon: FileText,
      count: unreadCounts.audit,
      badgeStyle: 'bg-[#06B6D4] text-white'
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-blue-100 text-blue-700 rounded-lg">
              <ShieldAlert className="w-6 h-6" />
            </div>
            <h1 className="text-2xl font-bold text-slate-900">Security & Auditing</h1>
          </div>
          <p className="text-sm text-slate-500 mt-1 font-medium">
            Monitor authentication activity, security alerts, suspicious events, and audit logs.
          </p>
        </div>
        <button
          onClick={fetchSecurityData}
          disabled={loading}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-semibold hover:bg-blue-700 transition-colors shadow-xs cursor-pointer disabled:opacity-50"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      {/* Error Banner */}
      {error && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm font-medium">
          <p className="font-bold">Security System Error</p>
          <p className="text-xs text-red-700 mt-0.5">{error}</p>
        </div>
      )}

      {/* Security Tabs with Numeric Badges */}
      <div className="flex overflow-x-auto gap-2 border-b border-slate-200 pb-2 max-w-full">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          const showBadge = tab.count !== undefined && tab.count > 0;

          return (
            <button
              key={tab.id}
              onClick={() => handleTabChange(tab.id)}
              className={`flex items-center gap-2 px-3 sm:px-4 py-2 sm:py-2.5 rounded-lg text-xs sm:text-sm font-semibold transition-colors cursor-pointer whitespace-nowrap flex-shrink-0 ${
                isActive
                  ? 'bg-blue-600 text-white font-bold shadow-xs'
                  : 'bg-white text-slate-600 border border-slate-200 hover:bg-blue-50 hover:text-blue-700 hover:border-blue-200'
              }`}
            >
              <Icon className="w-4 h-4" />
              <span>{tab.label}</span>
              {showBadge && (
                <span className={`px-2 py-0.5 text-xs rounded-full font-bold shadow-2xs ${tab.badgeStyle}`}>
                  {tab.count}
                </span>
              )}
            </button>
          );
        })}
      </div>

      {/* Main Content Area */}
      {loading ? (
        <div className="bg-white border border-slate-200 rounded-xl p-12 text-center shadow-xs">
          <RefreshCw className="w-6 h-6 text-blue-600 animate-spin mx-auto mb-3" />
          <p className="text-slate-600 font-medium text-sm">Loading security records...</p>
        </div>
      ) : (
        <div>
          {/* TAB 1: LOGIN ACTIVITY */}
          {activeTab === 'login-activity' && (
            <div className="bg-white border border-slate-200 rounded-xl shadow-xs overflow-hidden">
              <div className="p-4 border-b border-slate-100 bg-slate-50/50 flex items-center justify-between">
                <h3 className="font-bold text-slate-900 text-sm">Login Activity</h3>
                <span className="text-xs text-slate-500 font-semibold">{loginLogs.length} Records</span>
              </div>

              {loginLogs.length === 0 ? (
                <div className="p-12 text-center text-slate-500 text-sm font-medium">
                  No login activities recorded yet.
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-sm">
                    <thead className="bg-slate-50 text-slate-600 uppercase text-[11px] font-bold border-b border-slate-200">
                      <tr>
                        <th className="px-5 py-3">Date</th>
                        <th className="px-5 py-3">Time</th>
                        <th className="px-5 py-3">Status</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {loginLogs.map((log) => (
                        <tr key={log.id} className="hover:bg-blue-50/30 transition-colors">
                          <td className="px-5 py-3.5 font-medium text-slate-900 text-sm whitespace-nowrap">
                            {formatDate(log.createdAt || log.loginDate)}
                          </td>
                          <td className="px-5 py-3.5 font-medium text-slate-700 text-sm whitespace-nowrap">
                            {formatTime(log.createdAt || log.loginTime)}
                          </td>
                          <td className="px-5 py-3.5 whitespace-nowrap">
                            {log.loginStatus === 'SUCCESS' ? (
                              <span className="px-2.5 py-1 rounded-md text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
                                SUCCESS
                              </span>
                            ) : (
                              <span className="px-2.5 py-1 rounded-md text-xs font-bold bg-rose-50 text-rose-700 border border-rose-200">
                                FAILED
                              </span>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {/* TAB 2: SUSPICIOUS ACTIVITY */}
          {activeTab === 'suspicious-activity' && (
            <div className="bg-white border border-slate-200 rounded-xl shadow-xs overflow-hidden">
              <div className="p-4 border-b border-slate-100 bg-slate-50/50 flex items-center justify-between">
                <h3 className="font-bold text-slate-900 text-sm">Suspicious Activity Log</h3>
                <span className="text-xs text-slate-500 font-semibold">{suspiciousActivities.length} Records</span>
              </div>

              {suspiciousActivities.length === 0 ? (
                <div className="p-12 text-center text-slate-500 text-sm font-medium">
                  No suspicious activities detected.
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-sm">
                    <thead className="bg-slate-50 text-slate-600 uppercase text-[11px] font-bold border-b border-slate-200">
                      <tr>
                        <th className="px-5 py-3">Activity</th>
                        <th className="px-5 py-3">Description</th>
                        <th className="px-5 py-3">Date</th>
                        <th className="px-5 py-3">Time</th>
                        <th className="px-5 py-3">Status</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {suspiciousActivities.map((act) => (
                        <tr key={act.id} className="hover:bg-blue-50/30 transition-colors">
                          <td className="px-5 py-3.5 font-bold text-slate-900 text-sm">
                            {act.activityType?.replaceAll('_', ' ')}
                          </td>
                          <td className="px-5 py-3.5 text-slate-700 font-medium text-sm">{act.description}</td>
                          <td className="px-5 py-3.5 text-slate-900 font-medium text-sm whitespace-nowrap">
                            {formatDate(act.detectedAt)}
                          </td>
                          <td className="px-5 py-3.5 text-slate-700 font-medium text-sm whitespace-nowrap">
                            {formatTime(act.detectedAt)}
                          </td>
                          <td className="px-5 py-3.5 whitespace-nowrap">
                            <span className="px-2.5 py-1 rounded-md text-xs font-bold bg-amber-100 text-amber-900 border border-amber-300">
                              FLAGGED
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {/* TAB 3: SECURITY ALERTS */}
          {activeTab === 'security-alerts' && (
            <div className="space-y-4">
              {alerts.length === 0 ? (
                <div className="bg-white border border-slate-200 rounded-xl p-12 text-center text-slate-500 text-sm font-medium shadow-xs">
                  No active security alerts.
                </div>
              ) : (
                alerts.map((alert) => {
                  const isRead = alert.status === 'READ';

                  return (
                    <div
                      key={alert.id}
                      className={`p-5 rounded-xl border transition-all shadow-xs ${
                        !isRead
                          ? 'bg-red-50/60 border-red-200'
                          : 'bg-white border-slate-200'
                      }`}
                    >
                      <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
                        {/* Left Column: Title + Badges inline, Description, Date & Time */}
                        <div className="space-y-1.5 flex-1">
                          <div className="flex items-center gap-2.5 flex-wrap">
                            <h4 className="font-bold text-slate-900 text-base">
                              {alert.alertType?.replaceAll('_', ' ')}
                            </h4>
                            <span className="px-3.5 py-1.5 rounded-md text-[11px] font-bold bg-red-600 text-white uppercase tracking-wider shadow-2xs">
                              {alert.severity ? `${alert.severity} SEVERITY` : 'HIGH SEVERITY'}
                            </span>
                            {!isRead ? (
                              <span className="px-3.5 py-1.5 rounded-md text-[11px] font-bold bg-red-100 text-red-800 border border-red-200 uppercase tracking-wider">
                                UNREAD
                              </span>
                            ) : (
                              <span className="px-3.5 py-1.5 rounded-md text-[11px] font-bold bg-emerald-100 text-emerald-800 border border-emerald-200 uppercase tracking-wider flex items-center gap-1">
                                <Check className="w-3.5 h-3.5 text-emerald-700" />
                                READ
                              </span>
                            )}
                          </div>
                          <p className="text-sm text-slate-700 font-medium">{alert.message}</p>
                          <div className="text-xs text-slate-500 font-medium pt-1 flex items-center gap-4">
                            <span>Date: <strong className="text-slate-800 font-semibold">{formatDate(alert.createdAt)}</strong></span>
                            <span>Time: <strong className="text-slate-800 font-semibold">{formatTime(alert.createdAt)}</strong></span>
                          </div>
                        </div>

                        {/* Right Column: MARK AS READ action button (ONLY when unread, NO duplicate READ badge) */}
                        {!isRead && (
                          <div className="flex-shrink-0 self-start sm:self-center">
                            <button
                              onClick={() => handleMarkAlertAsRead(alert.id)}
                              disabled={markingReadId === alert.id}
                              className="px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white font-bold text-xs rounded-lg transition-colors cursor-pointer shadow-xs flex items-center gap-2 disabled:opacity-50 tracking-wider"
                            >
                              <CheckCircle2 className="w-4 h-4" />
                              <span>MARK AS READ</span>
                            </button>
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          )}

          {/* TAB 4: AUDIT LOGS */}
          {activeTab === 'audit-logs' && (
            <div className="bg-white border border-slate-200 rounded-xl shadow-xs overflow-hidden">
              <div className="p-4 border-b border-slate-100 bg-slate-50/50 flex items-center justify-between">
                <h3 className="font-bold text-slate-900 text-sm">Audit Logs</h3>
                <span className="text-xs text-slate-500 font-semibold">{auditLogs.length} Events</span>
              </div>

              {auditLogs.length === 0 ? (
                <div className="p-12 text-center text-slate-500 text-sm font-medium">
                  No audit logs recorded yet.
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-sm">
                    <thead className="bg-slate-50 text-slate-600 uppercase text-[11px] font-bold border-b border-slate-200">
                      <tr>
                        <th className="px-5 py-3">Action</th>
                        <th className="px-5 py-3">Description</th>
                        <th className="px-5 py-3">Date</th>
                        <th className="px-5 py-3">Time</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {auditLogs.map((log) => (
                        <tr key={log.id} className="hover:bg-blue-50/30 transition-colors">
                          <td className="px-5 py-3.5 whitespace-nowrap">
                            <span className={`px-2.5 py-1 rounded-md text-xs border ${getActionBadgeStyle(log.action)}`}>
                              {log.action?.replaceAll('_', ' ')}
                            </span>
                          </td>
                          <td className="px-5 py-3.5 text-slate-800 font-medium text-sm">{log.description}</td>
                          <td className="px-5 py-3.5 text-slate-900 font-medium text-sm whitespace-nowrap">
                            {formatDate(log.timestamp)}
                          </td>
                          <td className="px-5 py-3.5 text-slate-700 font-medium text-sm whitespace-nowrap">
                            {formatTime(log.timestamp)}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default SecurityPage;
