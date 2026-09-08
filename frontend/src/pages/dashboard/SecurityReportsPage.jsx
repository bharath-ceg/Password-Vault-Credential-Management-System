import React, { useState, useEffect } from 'react';
import { 
  FileBarChart, 
  ShieldCheck, 
  ShieldAlert, 
  Key, 
  Activity, 
  CheckCircle2, 
  XCircle, 
  RefreshCw,
  Award,
  AlertTriangle
} from 'lucide-react';
import { reportService } from '../../services/report.service';
import Card from '../../components/ui/Card';

const SecurityReportsPage = () => {
  const [passwordHealth, setPasswordHealth] = useState(null);
  const [loginReport, setLoginReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchReports = async () => {
    setLoading(true);
    setError(null);
    try {
      const [healthRes, loginRes] = await Promise.all([
        reportService.getPasswordHealthReport(),
        reportService.getLoginActivityReport(),
      ]);

      if (healthRes?.data) setPasswordHealth(healthRes.data);
      if (loginRes?.data) setLoginReport(loginRes.data);
    } catch (err) {
      console.error('Failed to load security reports:', err);
      setError(err?.response?.data?.message || 'Failed to load security reports.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReports();
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

  const getHealthBadge = (score) => {
    if (score >= 80) return { label: 'Excellent Health', color: 'bg-emerald-100 text-emerald-800 border-emerald-300' };
    if (score >= 60) return { label: 'Good Health', color: 'bg-blue-100 text-blue-800 border-blue-300' };
    if (score >= 40) return { label: 'Fair / Action Required', color: 'bg-amber-100 text-amber-800 border-amber-300' };
    return { label: 'Poor Health / Action Needed', color: 'bg-red-100 text-red-800 border-red-300' };
  };

  const healthScore = passwordHealth?.healthScore ?? 100;
  const totalCredentials = passwordHealth?.totalCredentials ?? 0;
  const strongPasswords = passwordHealth?.strongPasswords ?? 0;
  const mediumPasswords = passwordHealth?.mediumPasswords ?? 0;
  const weakPasswords = passwordHealth?.weakPasswords ?? 0;

  const totalAttempts = loginReport?.totalAttempts ?? 0;
  const successfulLogins = loginReport?.successfulLogins ?? 0;
  const failedLogins = loginReport?.failedLogins ?? 0;
  const recentActivities = loginReport?.recentActivities ?? [];

  const healthStatus = getHealthBadge(healthScore);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-blue-100 text-blue-700 rounded-lg">
              <FileBarChart className="w-6 h-6" />
            </div>
            <h1 className="text-2xl font-bold text-slate-900">Security Reports</h1>
          </div>
          <p className="text-sm text-slate-500 mt-1 font-medium">
            Comprehensive audit reports covering Password Health and Login Activity analytics.
          </p>
        </div>
        <button
          onClick={fetchReports}
          disabled={loading}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-semibold hover:bg-blue-700 transition-colors shadow-xs cursor-pointer disabled:opacity-50"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          Refresh Reports
        </button>
      </div>

      {/* Error Message */}
      {error && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm font-medium">
          <p className="font-bold">Report Generation Error</p>
          <p className="text-xs text-red-700 mt-0.5">{error}</p>
        </div>
      )}

      {loading ? (
        <div className="bg-white border border-slate-200 rounded-xl p-12 text-center shadow-xs">
          <RefreshCw className="w-6 h-6 text-blue-600 animate-spin mx-auto mb-3" />
          <p className="text-slate-600 font-medium text-sm">Generating reports from PostgreSQL database...</p>
        </div>
      ) : (
        <div className="space-y-8">
          {/* ====================================================== */}
          {/* SECTION 1: PASSWORD HEALTH REPORT */}
          {/* ====================================================== */}
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <ShieldCheck className="w-5 h-5 text-blue-600" />
                <h2 className="text-lg font-bold text-slate-900">Password Health Report</h2>
              </div>
              <span className={`px-3 py-1 rounded-full text-xs font-bold border ${healthStatus.color}`}>
                {healthStatus.label}
              </span>
            </div>

            {/* Overall Health Score Card */}
            <Card className="bg-gradient-to-r from-slate-900 to-slate-800 text-white border-none p-5 sm:p-6 shadow-md">
              <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6">
                <div className="space-y-2 flex-1 min-w-0">
                  <span className="text-xs font-semibold uppercase tracking-wider text-slate-400 block truncate">Vault Password Health Summary</span>
                  <div className="flex items-baseline gap-3 flex-wrap">
                    <h3 className="text-3xl sm:text-4xl font-extrabold text-white">{healthScore}%</h3>
                    <span className="text-xs sm:text-sm font-medium text-slate-300">Overall Vault Health Score</span>
                  </div>
                  <p className="text-xs text-slate-400 max-w-xl leading-relaxed">
                    Evaluates all stored passwords in your vault using SecureVault strength metrics (length, character sets, predictability, and pattern analysis).
                  </p>
                </div>

                <div className="w-full lg:w-64 space-y-2 flex-shrink-0">
                  <div className="flex justify-between text-xs font-bold text-slate-300">
                    <span>Health Rating</span>
                    <span>{healthScore}/100</span>
                  </div>
                  <div className="w-full h-3 bg-slate-700 rounded-full overflow-hidden">
                    <div 
                      className={`h-full rounded-full transition-all duration-500 ${
                        healthScore >= 80 ? 'bg-emerald-500' : healthScore >= 60 ? 'bg-blue-500' : healthScore >= 40 ? 'bg-amber-500' : 'bg-red-500'
                      }`}
                      style={{ width: `${Math.max(5, Math.min(100, healthScore))}%` }}
                    />
                  </div>
                </div>
              </div>
            </Card>

            {/* Password Health Breakdown Cards */}
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-2 xl:grid-cols-4 gap-4">
              {/* Total Credentials */}
              <Card className="border-l-4 border-l-blue-600">
                <div className="flex items-center justify-between">
                  <div>
                    <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">Total Credentials</span>
                    <h4 className="text-2xl font-bold text-slate-900 mt-1">{totalCredentials}</h4>
                    <p className="text-[11px] text-slate-500 mt-0.5">Stored in Vault</p>
                  </div>
                  <div className="p-3 bg-blue-50 text-blue-600 rounded-lg">
                    <Key className="w-5 h-5" />
                  </div>
                </div>
              </Card>

              {/* Strong Passwords */}
              <Card className="border-l-4 border-l-emerald-600">
                <div className="flex items-center justify-between">
                  <div>
                    <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">Strong Passwords</span>
                    <h4 className="text-2xl font-bold text-emerald-600 mt-1">{strongPasswords}</h4>
                    <p className="text-[11px] text-slate-500 mt-0.5">Optimal protection</p>
                  </div>
                  <div className="p-3 bg-emerald-50 text-emerald-600 rounded-lg">
                    <ShieldCheck className="w-5 h-5" />
                  </div>
                </div>
              </Card>

              {/* Medium Passwords */}
              <Card className="border-l-4 border-l-amber-500">
                <div className="flex items-center justify-between">
                  <div>
                    <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">Medium Passwords</span>
                    <h4 className="text-2xl font-bold text-amber-600 mt-1">{mediumPasswords}</h4>
                    <p className="text-[11px] text-slate-500 mt-0.5">Moderate strength</p>
                  </div>
                  <div className="p-3 bg-amber-50 text-amber-600 rounded-lg">
                    <ShieldAlert className="w-5 h-5" />
                  </div>
                </div>
              </Card>

              {/* Weak Passwords */}
              <Card className="border-l-4 border-l-rose-600">
                <div className="flex items-center justify-between">
                  <div>
                    <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">Weak Passwords</span>
                    <h4 className="text-2xl font-bold text-rose-600 mt-1">{weakPasswords}</h4>
                    <p className="text-[11px] text-slate-500 mt-0.5">At-risk credentials</p>
                  </div>
                  <div className="p-3 bg-rose-50 text-rose-600 rounded-lg">
                    <AlertTriangle className="w-5 h-5" />
                  </div>
                </div>
              </Card>
            </div>
          </div>

          {/* ====================================================== */}
          {/* SECTION 2: LOGIN ACTIVITY REPORT */}
          {/* ====================================================== */}
          <div className="space-y-4 pt-4 border-t border-slate-200">
            <div className="flex items-center gap-2">
              <Activity className="w-5 h-5 text-blue-600" />
              <h2 className="text-lg font-bold text-slate-900">Login Activity Report</h2>
            </div>

            {/* Login Activity Summary Cards */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <Card className="border-l-4 border-l-blue-600">
                <div className="flex items-center justify-between">
                  <div>
                    <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">Total Attempts</span>
                    <h4 className="text-2xl font-bold text-slate-900 mt-1">{totalAttempts}</h4>
                    <p className="text-[11px] text-slate-500 mt-0.5">Recorded log entries</p>
                  </div>
                  <div className="p-3 bg-blue-50 text-blue-600 rounded-lg">
                    <Activity className="w-5 h-5" />
                  </div>
                </div>
              </Card>

              <Card className="border-l-4 border-l-emerald-600">
                <div className="flex items-center justify-between">
                  <div>
                    <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">Successful Logins</span>
                    <h4 className="text-2xl font-bold text-emerald-600 mt-1">{successfulLogins}</h4>
                    <p className="text-[11px] text-slate-500 mt-0.5">Valid authentication</p>
                  </div>
                  <div className="p-3 bg-emerald-50 text-emerald-600 rounded-lg">
                    <CheckCircle2 className="w-5 h-5" />
                  </div>
                </div>
              </Card>

              <Card className="border-l-4 border-l-rose-600">
                <div className="flex items-center justify-between">
                  <div>
                    <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">Failed Logins</span>
                    <h4 className="text-2xl font-bold text-rose-600 mt-1">{failedLogins}</h4>
                    <p className="text-[11px] text-slate-500 mt-0.5">Invalid credentials</p>
                  </div>
                  <div className="p-3 bg-rose-50 text-rose-600 rounded-lg">
                    <XCircle className="w-5 h-5" />
                  </div>
                </div>
              </Card>
            </div>

            {/* Recent Login Activities Table */}
            <div className="bg-white border border-slate-200 rounded-xl shadow-xs overflow-hidden">
              <div className="p-4 border-b border-slate-100 bg-slate-50/50 flex items-center justify-between">
                <h3 className="font-bold text-slate-900 text-sm">Recent Login Activities</h3>
                <span className="text-xs text-slate-500 font-semibold">{recentActivities.length} Records</span>
              </div>

              {recentActivities.length === 0 ? (
                <div className="p-12 text-center text-slate-500 text-sm font-medium">
                  No login activities found in report.
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
                      {recentActivities.map((log) => (
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
          </div>
        </div>
      )}
    </div>
  );
};

export default SecurityReportsPage;
