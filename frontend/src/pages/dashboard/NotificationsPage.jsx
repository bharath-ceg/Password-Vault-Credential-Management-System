import React, { useState, useEffect } from 'react';
import { notificationService } from '../../services/notification.service';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Spinner from '../../components/ui/Spinner';
import {
  Bell,
  CheckCheck,
  ShieldCheck,
  ShieldAlert,
  Users,
  Key,
  AlertTriangle,
  Clock,
  Check,
  Filter,
  RefreshCw
} from 'lucide-react';

const NotificationsPage = () => {
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('all'); // 'all' | 'unread'

  const fetchNotifications = async () => {
    try {
      setLoading(true);
      const [listRes, countRes] = await Promise.all([
        notificationService.getUserNotifications(),
        notificationService.getUnreadCount()
      ]);

      if (listRes?.data) {
        setNotifications(listRes.data);
      }
      if (countRes?.data) {
        setUnreadCount(countRes.data.unreadCount || 0);
      }
    } catch (err) {
      console.error('Failed to load notifications:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
    const interval = setInterval(fetchNotifications, 10000);
    return () => clearInterval(interval);
  }, []);

  const handleMarkAsRead = async (id) => {
    try {
      await notificationService.markAsRead(id);
      setNotifications(prev =>
        prev.map(n => (n.id === id ? { ...n, isRead: true } : n))
      );
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch (err) {
      console.error('Failed to mark notification as read:', err);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      setActionLoading(true);
      await notificationService.markAllAsRead();
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
      setUnreadCount(0);
    } catch (err) {
      console.error('Failed to mark all notifications as read:', err);
    } finally {
      setActionLoading(false);
    }
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${day}-${month}-${year} • ${hours}:${minutes}:${seconds}`;
  };

  const getEventBadge = (type) => {
    switch (type) {
      case 'SUCCESSFUL_LOGIN':
        return {
          icon: ShieldCheck,
          label: '🔐 New login detected',
          bgColor: 'bg-blue-50',
          textColor: 'text-blue-700',
          borderColor: 'border-blue-200',
          pillBg: 'bg-blue-100 text-blue-800'
        };
      case 'FAILED_LOGIN_SECURITY':
        return {
          icon: ShieldAlert,
          label: '🚨 Security alert',
          bgColor: 'bg-red-50',
          textColor: 'text-red-700',
          borderColor: 'border-red-200',
          pillBg: 'bg-red-100 text-red-800'
        };
      case 'CREDENTIAL_SHARED':
        return {
          icon: Users,
          label: '🔗 Credential shared with you',
          bgColor: 'bg-emerald-50',
          textColor: 'text-emerald-700',
          borderColor: 'border-emerald-200',
          pillBg: 'bg-emerald-100 text-emerald-800'
        };
      case 'PASSWORD_EXPIRATION':
        return {
          icon: Key,
          label: '🔑 Password update reminder',
          bgColor: 'bg-amber-50',
          textColor: 'text-amber-700',
          borderColor: 'border-amber-200',
          pillBg: 'bg-amber-100 text-amber-800'
        };
      case 'PASSWORD_HEALTH':
        return {
          icon: AlertTriangle,
          label: '⚠️ Weak password detected',
          bgColor: 'bg-amber-50',
          textColor: 'text-amber-700',
          borderColor: 'border-amber-200',
          pillBg: 'bg-amber-100 text-amber-800'
        };
      case 'SUSPICIOUS_ACTIVITY':
        return {
          icon: AlertTriangle,
          label: '🛡️ Suspicious activity detected',
          bgColor: 'bg-red-50',
          textColor: 'text-red-700',
          borderColor: 'border-red-200',
          pillBg: 'bg-red-100 text-red-800'
        };
      default:
        return {
          icon: Bell,
          label: '🔔 Notification',
          bgColor: 'bg-slate-50',
          textColor: 'text-slate-700',
          borderColor: 'border-slate-200',
          pillBg: 'bg-slate-100 text-slate-800'
        };
    }
  };

  const filteredNotifications = activeTab === 'unread'
    ? notifications.filter(n => !n.isRead)
    : notifications;

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Notifications</h2>
            {unreadCount > 0 && (
              <span className="px-2.5 py-0.5 text-xs font-bold bg-blue-100 text-blue-700 rounded-full border border-blue-200">
                {unreadCount} unread
              </span>
            )}
          </div>
          <p className="text-xs text-slate-500 mt-1 font-medium">
            Real-time in-app security alerts and activity notifications.
          </p>
        </div>

        <div className="flex items-center gap-2 self-start sm:self-auto">
          <Button
            variant="secondary"
            size="sm"
            onClick={fetchNotifications}
            icon={RefreshCw}
            disabled={loading}
          >
            Refresh
          </Button>
          {unreadCount > 0 && (
            <Button
              variant="primary"
              size="sm"
              onClick={handleMarkAllAsRead}
              loading={actionLoading}
              icon={CheckCheck}
            >
              Mark all read
            </Button>
          )}
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="flex border-b border-slate-200 gap-6 bg-white px-6 rounded-xl shadow-xs">
        <button
          onClick={() => setActiveTab('all')}
          className={`py-3 text-xs sm:text-sm font-semibold border-b-2 transition-all cursor-pointer ${
            activeTab === 'all'
              ? 'border-blue-600 text-blue-600 font-bold'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          All Notifications ({notifications.length})
        </button>
        <button
          onClick={() => setActiveTab('unread')}
          className={`py-3 text-xs sm:text-sm font-semibold border-b-2 transition-all cursor-pointer ${
            activeTab === 'unread'
              ? 'border-blue-600 text-blue-600 font-bold'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          Unread Only ({unreadCount})
        </button>
      </div>

      {/* Main List */}
      {loading && notifications.length === 0 ? (
        <Card className="flex items-center justify-center p-12">
          <Spinner size="lg" className="text-blue-600" />
        </Card>
      ) : filteredNotifications.length === 0 ? (
        <Card className="text-center py-16 px-6">
          <div className="w-14 h-14 rounded-full bg-slate-100 text-slate-400 flex items-center justify-center mx-auto mb-4 border border-slate-200">
            <Bell className="w-7 h-7" />
          </div>
          <h3 className="text-base font-bold text-slate-800">No Notifications</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto mt-1">
            {activeTab === 'unread'
              ? 'You have read all your notifications! Check back later for updates.'
              : 'You do not have any notifications yet.'}
          </p>
        </Card>
      ) : (
        <div className="space-y-3">
          {filteredNotifications.map((notif) => {
            const badge = getEventBadge(notif.type);
            const IconComp = badge.icon;

            return (
              <Card
                key={notif.id}
                className={`transition-all duration-150 relative ${
                  notif.isRead
                    ? 'bg-white border-slate-200'
                    : 'bg-blue-50/40 border-blue-200 shadow-xs ring-1 ring-blue-500/10'
                }`}
              >
                <div className="flex items-start gap-4">
                  {/* Icon Badge */}
                  <div className={`p-3 rounded-xl ${badge.bgColor} ${badge.textColor} border ${badge.borderColor} flex-shrink-0`}>
                    <IconComp className="w-5 h-5" />
                  </div>

                  {/* Body Content */}
                  <div className="flex-1 min-w-0 pr-12">
                    <div className="flex items-center gap-2 flex-wrap mb-1">
                      <span className={`text-[11px] font-bold px-2.5 py-0.5 rounded-full ${badge.pillBg}`}>
                        {badge.label}
                      </span>
                      {!notif.isRead && (
                        <span className="inline-flex items-center gap-1 text-[10px] font-extrabold uppercase tracking-wider text-blue-700 bg-blue-100 px-2 py-0.5 rounded-full">
                          Unread
                        </span>
                      )}
                    </div>

                    <h3 className={`text-base font-bold ${notif.isRead ? 'text-slate-800' : 'text-slate-900'}`}>
                      {notif.title}
                    </h3>

                    <p className="text-xs sm:text-sm text-slate-600 mt-1 leading-relaxed break-words font-normal">
                      {notif.message}
                    </p>

                    <div className="flex items-center gap-1.5 text-xs text-slate-400 mt-3 font-medium">
                      <Clock className="w-3.5 h-3.5 text-slate-400" />
                      <span>{formatDateTime(notif.createdAt)}</span>
                    </div>
                  </div>

                  {/* Mark as read action */}
                  {!notif.isRead && (
                    <button
                      onClick={() => handleMarkAsRead(notif.id)}
                      className="absolute top-5 right-5 flex items-center gap-1 px-2.5 py-1 text-xs font-semibold text-blue-600 hover:text-blue-800 bg-white hover:bg-blue-50 border border-blue-200 rounded-lg shadow-2xs transition-all cursor-pointer"
                      title="Mark as read"
                    >
                      <Check className="w-3.5 h-3.5" />
                      <span className="hidden sm:inline">Mark as read</span>
                    </button>
                  )}
                </div>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default NotificationsPage;
