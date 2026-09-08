import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { vaultService } from '../../services/vault.service';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import { ShieldCheck, Key, Lock, ArrowRight, ShieldAlert, KeyRound, Users, BarChart3, FileBarChart } from 'lucide-react';
import { Link } from 'react-router-dom';

const DashboardOverviewPage = () => {
  const { user } = useAuth();
  const [credentialCount, setCredentialCount] = useState(0);
  const [hasPrivacyPassword, setHasPrivacyPassword] = useState(true);

  useEffect(() => {
    const fetchOverviewData = async () => {
      try {
        const credentialsRes = await vaultService.getCredentials();
        if (credentialsRes.success && Array.isArray(credentialsRes.data)) {
          setCredentialCount(credentialsRes.data.length);
        }

        const statusRes = await vaultService.getPrivacyPasswordStatus();
        if (statusRes.success) {
          setHasPrivacyPassword(statusRes.data);
        }
      } catch (err) {
        console.error('Failed to load overview data', err);
      }
    };

    fetchOverviewData();
  }, []);

  return (
    <div className="space-y-6">
      {/* Header Welcome Banner */}
      <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-xs relative">
        <div className="max-w-2xl">
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">
            Welcome to Overview, {user?.fullName}!
          </h2>
          <p className="text-xs text-slate-500 mt-1 font-medium">
            Your SecureVault credential management and security overview.
          </p>
        </div>
      </div>

      {/* Quick Metrics Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-5">
        <Card className="flex items-center justify-between border-l-4 border-l-blue-600">
          <div className="min-w-0 flex-1 pr-2">
            <span className="text-[11px] sm:text-xs font-semibold uppercase tracking-wider text-slate-500 block truncate">Vault Items</span>
            <h4 className="text-xl sm:text-2xl font-bold text-slate-900 mt-0.5">{credentialCount}</h4>
            <p className="text-[11px] text-slate-500 mt-0.5 truncate">Stored credentials</p>
          </div>
          <div className="p-2.5 sm:p-3 bg-blue-50 rounded-lg text-blue-600 flex-shrink-0">
            <Key className="w-5 h-5" />
          </div>
        </Card>

        <Card className="flex items-center justify-between border-l-4 border-l-emerald-600">
          <div className="min-w-0 flex-1 pr-2">
            <span className="text-[11px] sm:text-xs font-semibold uppercase tracking-wider text-slate-500 block truncate">Email Verification</span>
            <h4 className="text-xl sm:text-2xl font-bold text-emerald-600 mt-0.5 truncate">Verified</h4>
            <p className="text-[11px] text-slate-500 mt-0.5 truncate">{user?.email}</p>
          </div>
          <div className="p-2.5 sm:p-3 bg-emerald-50 rounded-lg text-emerald-600 flex-shrink-0">
            <ShieldCheck className="w-5 h-5" />
          </div>
        </Card>

        <Card className={`flex items-center justify-between border-l-4 ${hasPrivacyPassword ? 'border-l-blue-600' : 'border-l-amber-500'}`}>
          <div className="min-w-0 flex-1 pr-2">
            <span className="text-[11px] sm:text-xs font-semibold uppercase tracking-wider text-slate-500 block truncate">Privacy Password</span>
            <h4 className={`text-xl sm:text-2xl font-bold mt-0.5 truncate ${hasPrivacyPassword ? 'text-slate-900' : 'text-amber-600'}`}>
              {hasPrivacyPassword ? 'Configured' : 'Setup Required'}
            </h4>
            <p className="text-[11px] text-slate-500 mt-0.5 truncate">Secondary disclosure shield</p>
          </div>
          <div className={`p-2.5 sm:p-3 rounded-lg flex-shrink-0 ${hasPrivacyPassword ? 'bg-blue-50 text-blue-600' : 'bg-amber-50 text-amber-600'}`}>
            {hasPrivacyPassword ? <Lock className="w-5 h-5" /> : <ShieldAlert className="w-5 h-5" />}
          </div>
        </Card>
      </div>

      {/* Feature Action Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
        <Card className="space-y-4 border-slate-200 flex flex-col justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-blue-50 border border-blue-100 rounded-lg text-blue-600">
              <Lock className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-base font-bold text-slate-900">Password Vault</h4>
              <p className="text-xs text-slate-500">View, add, edit, and reveal stored credentials</p>
            </div>
          </div>
          <Link to="/dashboard/vault" className="pt-2">
            <Button variant="primary" className="w-full" icon={ArrowRight}>
              Open Vault
            </Button>
          </Link>
        </Card>

        <Card className="space-y-4 border-slate-200 flex flex-col justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-blue-50 border border-blue-100 rounded-lg text-blue-600">
              <KeyRound className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-base font-bold text-slate-900">Password Generator</h4>
              <p className="text-xs text-slate-500">Generate secure random passwords &amp; check strength</p>
            </div>
          </div>
          <Link to="/dashboard/generator" className="pt-2">
            <Button variant="primary" className="w-full" icon={ArrowRight}>
              Open Generator
            </Button>
          </Link>
        </Card>

        <Card className="space-y-4 border-slate-200 flex flex-col justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-blue-50 border border-blue-100 rounded-lg text-blue-600">
              <Users className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-base font-bold text-slate-900">Credential Sharing</h4>
              <p className="text-xs text-slate-500">Share vault credentials with permission control</p>
            </div>
          </div>
          <Link to="/dashboard/sharing" className="pt-2">
            <Button variant="primary" className="w-full" icon={ArrowRight}>
              Open Sharing
            </Button>
          </Link>
        </Card>

        <Card className="space-y-4 border-slate-200 flex flex-col justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-blue-50 border border-blue-100 rounded-lg text-blue-600">
              <BarChart3 className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-base font-bold text-slate-900">Security Analytics</h4>
              <p className="text-xs text-slate-500">Real-time security analytics and statistics</p>
            </div>
          </div>
          <Link to="/dashboard/analytics" className="pt-2">
            <Button variant="primary" className="w-full" icon={ArrowRight}>
              View Analytics
            </Button>
          </Link>
        </Card>

        <Card className="space-y-4 border-slate-200 flex flex-col justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-blue-50 border border-blue-100 rounded-lg text-blue-600">
              <FileBarChart className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-base font-bold text-slate-900">Security Reports</h4>
              <p className="text-xs text-slate-500">Password Health &amp; Login Activity reports</p>
            </div>
          </div>
          <Link to="/dashboard/reports" className="pt-2">
            <Button variant="primary" className="w-full" icon={ArrowRight}>
              View Reports
            </Button>
          </Link>
        </Card>
      </div>
    </div>
  );
};

export default DashboardOverviewPage;
