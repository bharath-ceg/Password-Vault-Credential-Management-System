import React, { useState, useEffect, useRef } from 'react';
import { Bell, CheckCheck, ShieldAlert, Key, Users, AlertTriangle, X, Clock, Check, ShieldCheck } from 'lucide-react';
import { notificationService } from '../services/notification.service';

const NotificationDropdown = () => {
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isOpen, setIsOpen] = useState(false);
  const [activeTab, setActiveTab] = useState('all'); // 'all' | 'unread'
  const [loading, setLoading] = useState(false);
  const dropdownRef = useRef(null);

  const fetchNotificationsData = async () => {
    try {
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
      // Fail silently for background polling
    }
  };

  useEffect(() => {
    fetchNotificationsData();
    const interval = setInterval(fetchNotificationsData, 10000);
    return () => clearInterval(interval);
  }, []);

  // Close dropdown on outside click
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleToggle = () => {
    if (!isOpen) {
      fetchNotificationsData();
    }
    setIsOpen(!isOpen);
  };

  const handleMarkAsRead = async (id, e) => {
    e.stopPropagation();
    try {
      await notificationService.markAsRead(id);
      setNotifications(prev =>
        prev.map(n => (n.id === id ? { ...n, isRead: true } : n))
      );
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch (err) {
      console.error('Failed to mark notification as read', err);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      setLoading(true);
      await notificationService.markAllAsRead();
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
      setUnreadCount(0);
    } catch (err) {
      console.error('Failed to mark all notifications as read', err);
    } finally {
      setLoading(false);
    }
  };

  const formatTime = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    const now = new Date();
    const diffSeconds = Math.floor((now - date) / 1000);

    if (diffSeconds < 60) return 'Just now';
    if (diffSeconds < 3600) return `${Math.floor(diffSeconds / 60)}m ago`;
    if (diffSeconds < 86400) return `${Math.floor(diffSeconds / 3600)}h ago`;
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  };

  const getEventStyle = (type) => {
    switch (type) {
      case 'SUCCESSFUL_LOGIN':
        return {
          icon: ShieldCheck,
          bgColor: 'bg-blue-50',
          textColor: 'text-blue-600',
          borderColor: 'border-blue-200'
        };
      case 'FAILED_LOGIN_SECURITY':
      case 'SUSPICIOUS_ACTIVITY':
        return {
          icon: ShieldAlert,
          bgColor: 'bg-red-50',
          textColor: 'text-red-600',
          borderColor: 'border-red-200'
        };
      case 'CREDENTIAL_SHARED':
        return {
          icon: Users,
          bgColor: 'bg-emerald-50',
          textColor: 'text-emerald-600',
          borderColor: 'border-emerald-200'
        };
      case 'PASSWORD_EXPIRATION':
      case 'PASSWORD_HEALTH':
        return {
          icon: Key,
          bgColor: 'bg-amber-50',
          textColor: 'text-amber-600',
          borderColor: 'border-amber-200'
        };
      default:
        return {
          icon: Bell,
          bgColor: 'bg-slate-50',
          textColor: 'text-slate-600',
          borderColor: 'border-slate-200'
        };
    }
  };

  const filteredNotifications = activeTab === 'unread'
    ? notifications.filter(n => !n.isRead)
    : notifications;

  return (
    <div className="relative inline-block text-left" ref={dropdownRef}>
      {/* Bell Icon Button */}
      <button
        onClick={handleToggle}
        className="relative p-2 rounded-xl text-slate-600 hover:text-blue-600 hover:bg-slate-100 transition-all duration-150 cursor-pointer focus:outline-none focus:ring-2 focus:ring-blue-500/20"
        aria-label="View notifications"
      >
        <Bell className="w-5 h-5" />
        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 min-w-[20px] h-[20px] px-1.5 bg-red-600 text-white font-bold text-[10px] rounded-full flex items-center justify-center border-2 border-white shadow-xs animate-pulse">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {/* Dropdown Popover */}
      {isOpen && (
        <div className="absolute right-0 sm:right-0 mt-2 w-80 sm:w-96 max-w-[calc(100vw-2rem)] bg-white rounded-2xl shadow-xl border border-slate-200 z-50 overflow-hidden transition-all duration-200 transform origin-top-right">
          {/* Header */}
          <div className="p-4 bg-slate-50/80 border-b border-slate-100 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <h3 className="font-bold text-slate-900 text-sm">Notifications</h3>
              {unreadCount > 0 && (
                <span className="px-2 py-0.5 text-[11px] font-bold bg-blue-100 text-blue-700 rounded-full">
                  {unreadCount} new
                </span>
              )}
            </div>
            <div className="flex items-center gap-2">
              {unreadCount > 0 && (
                <button
                  onClick={handleMarkAllAsRead}
                  disabled={loading}
                  className="flex items-center gap-1 text-xs font-semibold text-blue-600 hover:text-blue-800 transition-colors cursor-pointer disabled:opacity-50"
                  title="Mark all as read"
                >
                  <CheckCheck className="w-3.5 h-3.5" />
                  <span>Mark all read</span>
                </button>
              )}
              <button
                onClick={() => setIsOpen(false)}
                className="p-1 text-slate-400 hover:text-slate-600 rounded-lg hover:bg-slate-200/50 cursor-pointer"
                aria-label="Close notifications"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>

          {/* Filter Tabs */}
          <div className="flex border-b border-slate-100 px-4 pt-2 gap-4 bg-white text-xs font-semibold text-slate-500">
            <button
              onClick={() => setActiveTab('all')}
              className={`pb-2 border-b-2 transition-all cursor-pointer ${
                activeTab === 'all'
                  ? 'border-blue-600 text-blue-600 font-bold'
                  : 'border-transparent hover:text-slate-900'
              }`}
            >
              All ({notifications.length})
            </button>
            <button
              onClick={() => setActiveTab('unread')}
              className={`pb-2 border-b-2 transition-all cursor-pointer ${
                activeTab === 'unread'
                  ? 'border-blue-600 text-blue-600 font-bold'
                  : 'border-transparent hover:text-slate-900'
              }`}
            >
              Unread ({unreadCount})
            </button>
          </div>

          {/* Notification Items List */}
          <div className="max-h-[360px] overflow-y-auto divide-y divide-slate-100">
            {filteredNotifications.length === 0 ? (
              <div className="p-8 text-center">
                <div className="w-12 h-12 rounded-full bg-slate-100 text-slate-400 flex items-center justify-center mx-auto mb-3">
                  <Bell className="w-6 h-6" />
                </div>
                <p className="text-xs font-semibold text-slate-700">No notifications found</p>
                <p className="text-[11px] text-slate-400 mt-0.5">
                  {activeTab === 'unread' ? 'You have no unread notifications' : 'Your notification inbox is currently empty'}
                </p>
              </div>
            ) : (
              filteredNotifications.map((notif) => {
                const style = getEventStyle(notif.type);
                const IconComponent = style.icon;

                return (
                  <div
                    key={notif.id}
                    className={`p-3.5 transition-colors flex items-start gap-3 group relative ${
                      notif.isRead ? 'bg-white hover:bg-slate-50/80' : 'bg-blue-50/40 hover:bg-blue-50/70'
                    }`}
                  >
                    {/* Event Icon */}
                    <div className={`p-2 rounded-xl flex-shrink-0 ${style.bgColor} ${style.textColor} border ${style.borderColor}`}>
                      <IconComponent className="w-4 h-4" />
                    </div>

                    {/* Notification Content */}
                    <div className="flex-1 min-w-0 pr-6">
                      <div className="flex items-center justify-between gap-2">
                        <h4 className={`text-xs font-bold truncate ${notif.isRead ? 'text-slate-800' : 'text-slate-900'}`}>
                          {notif.title}
                        </h4>
                        {!notif.isRead && (
                          <span className="w-2 h-2 rounded-full bg-blue-600 flex-shrink-0"></span>
                        )}
                      </div>
                      <p className="text-xs text-slate-600 mt-0.5 leading-relaxed break-words">
                        {notif.message}
                      </p>
                      <div className="flex items-center gap-1 text-[10px] text-slate-400 mt-1.5 font-medium">
                        <Clock className="w-3 h-3 text-slate-400" />
                        <span>{formatTime(notif.createdAt)}</span>
                      </div>
                    </div>

                    {/* Mark as read individual button */}
                    {!notif.isRead && (
                      <button
                        onClick={(e) => handleMarkAsRead(notif.id, e)}
                        className="opacity-0 group-hover:opacity-100 p-1 text-slate-400 hover:text-blue-600 rounded-md hover:bg-blue-100/60 transition-all cursor-pointer absolute right-3 top-3.5"
                        title="Mark as read"
                      >
                        <Check className="w-3.5 h-3.5" />
                      </button>
                    )}
                  </div>
                );
              })
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default NotificationDropdown;
