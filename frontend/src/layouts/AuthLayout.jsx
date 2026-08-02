import React from 'react';
import { ShieldCheck } from 'lucide-react';
import { Outlet } from 'react-router-dom';

const AuthLayout = () => {
  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <div className="inline-flex items-center justify-center p-2.5 bg-blue-600 rounded-xl shadow-sm text-white mb-3">
          <ShieldCheck className="w-8 h-8" />
        </div>
        <h2 className="text-2xl font-bold tracking-tight text-slate-900">
          SecureVault
        </h2>
        <p className="mt-1 text-sm text-slate-500">
          Password Vault and Credential Management System
        </p>
      </div>

      <div className="mt-6 sm:mx-auto sm:w-full sm:max-w-md px-4">
        <Outlet />
      </div>

      <div className="mt-10 text-center text-xs text-slate-400">
        &copy; 2026 SecureVault
      </div>
    </div>
  );
};

export default AuthLayout;
