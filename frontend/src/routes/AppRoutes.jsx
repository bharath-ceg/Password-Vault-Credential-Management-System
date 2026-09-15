import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import AuthLayout from '../layouts/AuthLayout';
import DashboardLayout from '../layouts/DashboardLayout';

// Auth Pages
import LoginPage from '../pages/auth/LoginPage';
import RegisterPage from '../pages/auth/RegisterPage';
import VerifyEmailPage from '../pages/auth/VerifyEmailPage';
import ForgotPasswordPage from '../pages/auth/ForgotPasswordPage';
import ResetPasswordPage from '../pages/auth/ResetPasswordPage';

// Dashboard Pages
import DashboardOverviewPage from '../pages/dashboard/DashboardOverviewPage';
import VaultPage from '../pages/dashboard/VaultPage';
import PasswordGeneratorPage from '../pages/dashboard/PasswordGeneratorPage';
import CredentialSharingPage from '../pages/dashboard/CredentialSharingPage';
import SecurityPage from '../pages/dashboard/SecurityPage';
import SecurityAnalyticsPage from '../pages/dashboard/SecurityAnalyticsPage';
import SecurityReportsPage from '../pages/dashboard/SecurityReportsPage';
import NotificationsPage from '../pages/dashboard/NotificationsPage';

// Route Guards
import ProtectedRoute from './ProtectedRoute';
import PublicRoute from './PublicRoute';

const AppRoutes = () => {
  return (
    <Routes>
      {/* Public Auth Routes */}
      <Route element={<PublicRoute />}>
        <Route element={<AuthLayout />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/verify-email" element={<VerifyEmailPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />
        </Route>
      </Route>

      {/* Protected Dashboard Routes */}
      <Route element={<ProtectedRoute />}>
        <Route element={<DashboardLayout />}>
          <Route path="/dashboard" element={<DashboardOverviewPage />} />
          <Route path="/dashboard/vault" element={<VaultPage />} />
          <Route path="/dashboard/generator" element={<PasswordGeneratorPage />} />
          <Route path="/dashboard/sharing" element={<CredentialSharingPage />} />
          <Route path="/dashboard/security" element={<SecurityPage />} />
          <Route path="/dashboard/analytics" element={<SecurityAnalyticsPage />} />
          <Route path="/dashboard/reports" element={<SecurityReportsPage />} />
          <Route path="/dashboard/notifications" element={<NotificationsPage />} />
        </Route>
      </Route>

      {/* Default Catch-all Redirect */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
};

export default AppRoutes;
