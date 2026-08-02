import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { vaultService } from '../../services/vault.service';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import { ShieldCheck, Key, Lock, ArrowRight, ShieldAlert } from 'lucide-react';
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
            Welcome, {user?.fullName}!
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Your SecureVault account is ready.
          </p>
        </div>
      </div>

      {/* Quick Metrics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        <Card className="flex items-center justify-between border-l-4 border-l-blue-600">
          <div>
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">Vault Items</span>
            <h4 className="text-2xl font-bold text-slate-900 mt-0.5">{credentialCount}</h4>
            <p className="text-[11px] text-slate-500 mt-0.5">Stored credentials</p>
          </div>
          <div className="p-3 bg-blue-50 rounded-lg text-blue-600">
            <Key className="w-5 h-5" />
          </div>
        </Card>

        <Card className="flex items-center justify-between border-l-4 border-l-emerald-600">
          <div>
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">Email Verification</span>
            <h4 className="text-2xl font-bold text-emerald-600 mt-0.5">Verified</h4>
            <p className="text-[11px] text-slate-500 mt-0.5">{user?.email}</p>
          </div>
          <div className="p-3 bg-emerald-50 rounded-lg text-emerald-600">
            <ShieldCheck className="w-5 h-5" />
          </div>
        </Card>

        <Card className={`flex items-center justify-between border-l-4 ${hasPrivacyPassword ? 'border-l-blue-600' : 'border-l-amber-500'}`}>
          <div>
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">Privacy Password</span>
            <h4 className={`text-2xl font-bold mt-0.5 ${hasPrivacyPassword ? 'text-slate-900' : 'text-amber-600'}`}>
              {hasPrivacyPassword ? 'Configured' : 'Setup Required'}
            </h4>
            <p className="text-[11px] text-slate-500 mt-0.5">Secondary disclosure shield</p>
          </div>
          <div className={`p-3 rounded-lg ${hasPrivacyPassword ? 'bg-blue-50 text-blue-600' : 'bg-amber-50 text-amber-600'}`}>
            {hasPrivacyPassword ? <Lock className="w-5 h-5" /> : <ShieldAlert className="w-5 h-5" />}
          </div>
        </Card>
      </div>

      {/* Primary Vault Action Card */}
      <Card className="space-y-4 border-slate-200">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-blue-50 border border-blue-100 rounded-lg text-blue-600">
              <Lock className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-base font-bold text-slate-900">Password Vault</h4>
              <p className="text-xs text-slate-500">View, add, edit, reveal, and manage credentials</p>
            </div>
          </div>
          <Link to="/dashboard/vault">
            <Button variant="primary" icon={ArrowRight}>
              Access Password Vault
            </Button>
          </Link>
        </div>
      </Card>
    </div>
  );
};

export default DashboardOverviewPage;
