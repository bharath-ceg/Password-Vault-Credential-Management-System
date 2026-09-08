import React, { useState, useEffect } from 'react';
import { 
  BarChart3, 
  Activity, 
  AlertTriangle, 
  Bell, 
  FileText, 
  CheckCircle2, 
  XCircle, 
  RefreshCw, 
  ShieldCheck,
  Lock,
  Users,
  ShieldAlert
} from 'lucide-react';
import { securityService } from '../../services/security.service';
import Card from '../../components/ui/Card';

const SecurityAnalyticsPage = () => {
  const [analytics, setAnalytics] = useState(null);
  const [activeTab, setActiveTab] = useState('overview');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchAnalytics = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await securityService.getSecurityAnalytics();
      if (response?.data) {
        setAnalytics(response.data);
      } else {
        setAnalytics(null);
      }
    } catch (err) {
      console.error('Failed to load security analytics data:', err);
      setError(err?.response?.data?.message || 'Failed to load security analytics.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAnalytics();
  }, []);

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

  const totalLoginAttempts = analytics?.totalLoginAttempts || 0;
  const successfulLogins = analytics?.successfulLogins || 0;
  const failedLogins = analytics?.failedLogins || 0;
  const suspiciousActivities = analytics?.suspiciousActivities || 0;
  const securityAlerts = analytics?.securityAlerts || 0;

  const recentActivities = analytics?.recentActivities || [];
  const loginLogs = analytics?.loginLogs || [];
  const suspiciousLogs = analytics?.suspiciousActivityLogs || [];
  const alertLogs = analytics?.securityAlertLogs || [];

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-blue-100 text-blue-700 rounded-lg">
              <BarChart3 className="w-6 h-6" />
            </div>
            <h1 className="text-2xl font-bold text-slate-900">Security Analytics Dashboard</h1>
          </div>
          <p className="text-sm text-slate-500 mt-1 font-medium">
            Real-time security analytics, authentication metrics, suspicious activity detection, and alert statistics.
          </p>
        </div>
        <button
          onClick={fetchAnalytics}
          disabled={loading}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-semibold hover:bg-blue-700 transition-colors shadow-xs cursor-pointer disabled:opacity-50"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          Refresh Data
        </button>
      </div>

      {/* Error Notification */}
      {error && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm font-medium">
          <p className="font-bold">Analytics Load Failure</p>
          <p className="text-xs text-red-700 mt-0.5">{error}</p>
        </div>
      )}

      {loading ? (
        <div className="bg-white border border-slate-200 rounded-xl p-12 text-center shadow-xs">
          <RefreshCw className="w-6 h-6 text-blue-600 animate-spin mx-auto mb-3" />
          <p className="text-slate-600 font-medium text-sm">Loading security analytics from database...</p>
        </div>
      ) : (
        <div className="space-y-6">
          {/* Summary Statistics Grid (5 Key Cards) */}
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-5 gap-3 sm:gap-4">
            {/* 1. Total Login Attempts */}
            <Card className="border-l-4 border-l-blue-600">
              <div className="flex items-center justify-between">
                <div className="min-w-0 flex-1 pr-1">
                  <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500 block truncate">Total Logins</span>
                  <h3 className="text-xl sm:text-2xl font-bold text-slate-900 mt-1">{totalLoginAttempts}</h3>
                  <p className="text-[11px] text-slate-500 mt-0.5 truncate">Attempted sign-ins</p>
                </div>
                <div className="p-2.5 bg-blue-50 text-blue-600 rounded-lg flex-shrink-0">
                  <Activity className="w-5 h-5" />
                </div>
              </div>
            </Card>

            {/* 2. Successful Logins */}
            <Card className="border-l-4 border-l-emerald-600">
              <div className="flex items-center justify-between">
                <div className="min-w-0 flex-1 pr-1">
                  <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500 block truncate">Successful</span>
                  <h3 className="text-xl sm:text-2xl font-bold text-emerald-600 mt-1">{successfulLogins}</h3>
                  <p className="text-[11px] text-slate-500 mt-0.5 truncate">Authenticated sessions</p>
                </div>
                <div className="p-2.5 bg-emerald-50 text-emerald-600 rounded-lg flex-shrink-0">
                  <CheckCircle2 className="w-5 h-5" />
                </div>
              </div>
            </Card>

            {/* 3. Failed Logins */}
            <Card className="border-l-4 border-l-rose-500">
              <div className="flex items-center justify-between">
                <div className="min-w-0 flex-1 pr-1">
                  <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500 block truncate">Failed Logins</span>
                  <h3 className="text-xl sm:text-2xl font-bold text-rose-600 mt-1">{failedLogins}</h3>
                  <p className="text-[11px] text-slate-500 mt-0.5 truncate">Rejected attempts</p>
                </div>
                <div className="p-2.5 bg-rose-50 text-rose-600 rounded-lg flex-shrink-0">
                  <XCircle className="w-5 h-5" />
                </div>
              </div>
            </Card>

            {/* 4. Suspicious Activities */}
            <Card className="border-l-4 border-l-amber-500">
              <div className="flex items-center justify-between">
                <div className="min-w-0 flex-1 pr-1">
                  <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500 block truncate">Suspicious</span>
                  <h3 className="text-xl sm:text-2xl font-bold text-amber-600 mt-1">{suspiciousActivities}</h3>
                  <p className="text-[11px] text-slate-500 mt-0.5 truncate">Flagged events</p>
                </div>
                <div className="p-2.5 bg-amber-50 text-amber-600 rounded-lg flex-shrink-0">
                  <AlertTriangle className="w-5 h-5" />
                </div>
              </div>
            </Card>

            {/* 5. Security Alerts */}
            <Card className="border-l-4 border-l-red-600">
              <div className="flex items-center justify-between">
                <div className="min-w-0 flex-1 pr-1">
                  <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500 block truncate">Security Alerts</span>
                  <h3 className="text-xl sm:text-2xl font-bold text-red-600 mt-1">{securityAlerts}</h3>
                  <p className="text-[11px] text-slate-500 mt-0.5 truncate">Generated alerts</p>
                </div>
                <div className="p-2.5 bg-red-50 text-red-600 rounded-lg flex-shrink-0">
                  <Bell className="w-5 h-5" />
                </div>
              </div>
            </Card>
          </div>

          {/* Sub-Navigation Tabs */}
          <div className="flex overflow-x-auto gap-2 border-b border-slate-200 pb-2 max-w-full">
            {[
              { id: 'overview', label: 'Overview & Audit', icon: FileText },
              { id: 'login-activity', label: 'Login Activity', icon: Activity },
              { id: 'suspicious-activity', label: 'Suspicious Activity', icon: AlertTriangle },
              { id: 'security-alerts', label: 'Security Alerts', icon: Bell },
            ].map((tab) => {
              const Icon = tab.icon;
              const isActive = activeTab === tab.id;
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`flex items-center gap-2 px-3 sm:px-4 py-2 sm:py-2.5 rounded-lg text-xs sm:text-sm font-semibold transition-colors cursor-pointer whitespace-nowrap flex-shrink-0 ${
                    isActive
                      ? 'bg-blue-600 text-white font-bold shadow-xs'
                      : 'bg-white text-slate-600 border border-slate-200 hover:bg-blue-50 hover:text-blue-700 hover:border-blue-200'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  <span>{tab.label}</span>
                </button>
              );
            })}
          </div>

          {/* TAB 1: OVERVIEW & RECENT AUDIT ACTIVITIES */}
          {activeTab === 'overview' && (
            <div className="space-y-6">
              <div className="bg-white border border-slate-200 rounded-xl shadow-xs overflow-hidden">
                <div className="p-4 border-b border-slate-100 bg-slate-50/50 flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <FileText className="w-4 h-4 text-blue-600" />
                    <h3 className="font-bold text-slate-900 text-sm">Recent Audit & Security Activity</h3>
                  </div>
                  <span className="text-xs text-slate-500 font-semibold">{recentActivities.length} Recent Events</span>
                </div>

                {recentActivities.length === 0 ? (
                  <div className="p-12 text-center text-slate-500 text-sm font-medium">
                    No recent audit activities logged.
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
                        {recentActivities.map((log) => (
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
            </div>
          )}

          {/* TAB 2: LOGIN ACTIVITY */}
          {activeTab === 'login-activity' && (
            <div className="bg-white border border-slate-200 rounded-xl shadow-xs overflow-hidden">
              <div className="p-4 border-b border-slate-100 bg-slate-50/50 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Activity className="w-4 h-4 text-blue-600" />
                  <h3 className="font-bold text-slate-900 text-sm">Login Activity Stream</h3>
                </div>
                <span className="text-xs text-slate-500 font-semibold">{loginLogs.length} Records</span>
              </div>

              {loginLogs.length === 0 ? (
                <div className="p-12 text-center text-slate-500 text-sm font-medium">
                  No login activity recorded.
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-sm">
                    <thead className="bg-slate-50 text-slate-600 uppercase text-[11px] font-bold border-b border-slate-200">
                      <tr>
                        <th className="px-5 py-3">Date</th>
                        <th className="px-5 py-3">Time</th>
                        <th className="px-5 py-3">User Email</th>
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
                          <td className="px-5 py-3.5 text-slate-700 font-medium text-sm whitespace-nowrap">
                            {log.userEmail}
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

          {/* TAB 3: SUSPICIOUS ACTIVITY */}
          {activeTab === 'suspicious-activity' && (
            <div className="bg-white border border-slate-200 rounded-xl shadow-xs overflow-hidden">
              <div className="p-4 border-b border-slate-100 bg-slate-50/50 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <AlertTriangle className="w-4 h-4 text-amber-600" />
                  <h3 className="font-bold text-slate-900 text-sm">Suspicious Activity Log</h3>
                </div>
                <span className="text-xs text-slate-500 font-semibold">{suspiciousLogs.length} Records</span>
              </div>

              {suspiciousLogs.length === 0 ? (
                <div className="p-12 text-center text-slate-500 text-sm font-medium">
                  No suspicious activity detected.
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-sm">
                    <thead className="bg-slate-50 text-slate-600 uppercase text-[11px] font-bold border-b border-slate-200">
                      <tr>
                        <th className="px-5 py-3">Activity</th>
                        <th className="px-5 py-3">Description</th>
                        <th className="px-5 py-3">Detected At</th>
                        <th className="px-5 py-3">Status</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {suspiciousLogs.map((act) => (
                        <tr key={act.id} className="hover:bg-blue-50/30 transition-colors">
                          <td className="px-5 py-3.5 font-bold text-slate-900 text-sm">
                            {act.activityType?.replaceAll('_', ' ')}
                          </td>
                          <td className="px-5 py-3.5 text-slate-700 font-medium text-sm">{act.description}</td>
                          <td className="px-5 py-3.5 text-slate-900 font-medium text-sm whitespace-nowrap">
                            {formatDate(act.detectedAt)} {formatTime(act.detectedAt)}
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

          {/* TAB 4: SECURITY ALERTS */}
          {activeTab === 'security-alerts' && (
            <div className="space-y-4">
              {alertLogs.length === 0 ? (
                <div className="bg-white border border-slate-200 rounded-xl p-12 text-center text-slate-500 text-sm font-medium shadow-xs">
                  No security alerts generated.
                </div>
              ) : (
                alertLogs.map((alert) => (
                  <div key={alert.id} className="p-5 rounded-xl border bg-white border-slate-200 shadow-xs space-y-2">
                    <div className="flex items-center justify-between flex-wrap gap-2">
                      <div className="flex items-center gap-2">
                        <Bell className="w-4 h-4 text-red-600" />
                        <h4 className="font-bold text-slate-900 text-base">{alert.alertType?.replaceAll('_', ' ')}</h4>
                      </div>
                      <span className="px-3 py-1 rounded-md text-[11px] font-bold bg-red-600 text-white uppercase tracking-wider shadow-2xs">
                        {alert.severity ? `${alert.severity} SEVERITY` : 'HIGH SEVERITY'}
                      </span>
                    </div>
                    <p className="text-sm text-slate-700 font-medium">{alert.message}</p>
                    <div className="text-xs text-slate-500 font-medium pt-1 flex items-center gap-4">
                      <span>Date: <strong className="text-slate-800 font-semibold">{formatDate(alert.createdAt)}</strong></span>
                      <span>Time: <strong className="text-slate-800 font-semibold">{formatTime(alert.createdAt)}</strong></span>
                    </div>
                  </div>
                ))
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default SecurityAnalyticsPage;
