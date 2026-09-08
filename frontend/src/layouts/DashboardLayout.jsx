import React, { useState, useEffect } from 'react';
import { Outlet, NavLink, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ShieldCheck, Lock, LayoutDashboard, LogOut, User, KeyRound, Users, ShieldAlert, BarChart3, FileBarChart, Menu, X } from 'lucide-react';
import { securityService } from '../services/security.service';

const DashboardLayout = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [hasUnreadAlerts, setHasUnreadAlerts] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const fetchUnreadCounts = async () => {
    try {
      const response = await securityService.getUnreadCounts();
      if (response?.data) {
        const unreadAlerts = response.data.securityAlertsUnread || 0;
        setHasUnreadAlerts(unreadAlerts > 0);
      }
    } catch (e) {
      // Ignore errors silently for sidebar badge
    }
  };

  useEffect(() => {
    fetchUnreadCounts();
    const interval = setInterval(fetchUnreadCounts, 10000);
    return () => clearInterval(interval);
  }, [location.pathname]);

  // Close mobile drawer on route change
  useEffect(() => {
    setIsMobileMenuOpen(false);
  }, [location.pathname]);

  const handleLogout = async () => {
    setIsMobileMenuOpen(false);
    await logout();
    navigate('/login');
  };

  const navItems = [
    { label: 'Overview', path: '/dashboard', icon: LayoutDashboard, exact: true },
    { label: 'Password Vault', path: '/dashboard/vault', icon: Lock },
    { label: 'Password Generator', path: '/dashboard/generator', icon: KeyRound },
    { label: 'Credential Sharing', path: '/dashboard/sharing', icon: Users },
    { label: 'Security', path: '/dashboard/security', icon: ShieldAlert, showRedDot: hasUnreadAlerts },
    { label: 'Security Analytics', path: '/dashboard/analytics', icon: BarChart3 },
    { label: 'Reports', path: '/dashboard/reports', icon: FileBarChart },
  ];

  return (
    <div className="h-screen w-full bg-slate-50 text-slate-900 flex flex-col md:flex-row overflow-hidden relative">
      {/* Mobile Top Header Bar */}
      <header className="md:hidden flex items-center justify-between px-4 py-3 bg-white border-b border-slate-200 shadow-xs z-30 flex-shrink-0">
        <div className="flex items-center gap-2.5">
          <div className="p-2 bg-blue-600 rounded-lg text-white shadow-xs">
            <ShieldCheck className="w-5 h-5" />
          </div>
          <div>
            <h1 className="font-bold text-sm text-slate-900 leading-none">SecureVault</h1>
            <span className="text-[9px] text-slate-400 font-semibold uppercase tracking-wider">Credential Security</span>
          </div>
        </div>
        <button
          onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
          className="p-2 text-slate-600 hover:text-slate-900 rounded-lg hover:bg-slate-100 transition-colors cursor-pointer"
          aria-label="Toggle navigation menu"
        >
          {isMobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
        </button>
      </header>

      {/* Mobile Backdrop Overlay */}
      {isMobileMenuOpen && (
        <div
          className="fixed inset-0 bg-slate-900/50 backdrop-blur-xs z-40 md:hidden transition-opacity"
          onClick={() => setIsMobileMenuOpen(false)}
        />
      )}

      {/* Sidebar (Desktop + Mobile Overlay Drawer) */}
      <aside
        className={`fixed top-0 bottom-0 left-0 z-50 w-64 bg-white border-r border-slate-200 flex-shrink-0 flex flex-col justify-between shadow-xl md:shadow-xs transition-transform duration-200 ease-in-out md:translate-x-0 md:static md:z-auto md:h-full ${
          isMobileMenuOpen ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
        }`}
      >
        <div className="flex-1 overflow-y-auto">
          {/* Logo Header */}
          <div className="p-5 border-b border-slate-100 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="p-2.5 bg-blue-600 rounded-xl text-white shadow-xs">
                <ShieldCheck className="w-5 h-5" />
              </div>
              <div>
                <h1 className="font-bold text-base text-slate-900 leading-none">SecureVault</h1>
                <span className="text-[10px] text-slate-400 font-semibold uppercase tracking-wider">Credential Security</span>
              </div>
            </div>
            {/* Close button on mobile inside drawer */}
            <button
              onClick={() => setIsMobileMenuOpen(false)}
              className="md:hidden p-1 text-slate-400 hover:text-slate-600 rounded-md hover:bg-slate-100 cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Navigation Links */}
          <nav className="p-3 space-y-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.path}
                  to={item.path}
                  end={item.exact}
                  onClick={() => setIsMobileMenuOpen(false)}
                  className={({ isActive }) =>
                    `flex items-center justify-between px-3.5 py-2.5 rounded-lg text-sm font-semibold transition-all duration-150 ${
                      isActive
                        ? 'bg-blue-600 text-white font-bold shadow-xs'
                        : 'text-slate-600 hover:text-blue-700 hover:bg-blue-50'
                    }`
                  }
                >
                  <div className="flex items-center gap-3">
                    <Icon className="w-4 h-4" />
                    <span>{item.label}</span>
                  </div>
                  {item.showRedDot && (
                    <span className="w-2.5 h-2.5 bg-red-600 rounded-full flex-shrink-0 ring-2 ring-white shadow-xs animate-pulse"></span>
                  )}
                </NavLink>
              );
            })}
          </nav>
        </div>

        {/* User Profile & Logout Footer */}
        <div className="p-4 border-t border-slate-100 bg-slate-50/50 flex-shrink-0">
          <div className="flex items-center gap-3 mb-3 px-1">
            <div className="w-8 h-8 rounded-full bg-blue-100 border border-blue-200 flex items-center justify-center text-blue-700 font-semibold text-xs flex-shrink-0">
              {user?.fullName?.charAt(0) || <User className="w-4 h-4" />}
            </div>
            <div className="overflow-hidden">
              <p className="text-xs font-semibold text-slate-900 truncate">{user?.fullName}</p>
              <p className="text-[11px] text-slate-500 truncate">{user?.email}</p>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="w-full flex items-center justify-center gap-2 px-3 py-1.5 text-xs font-medium text-slate-700 bg-white border border-slate-200 rounded-lg hover:bg-red-50 hover:text-red-600 hover:border-red-200 transition-colors cursor-pointer"
          >
            <LogOut className="w-3.5 h-3.5" />
            Sign Out
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="flex-1 w-full min-w-0 h-full overflow-y-auto p-4 sm:p-6 md:p-8">
        <Outlet />
      </main>
    </div>
  );
};

export default DashboardLayout;
