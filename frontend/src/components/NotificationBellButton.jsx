import React, { useState, useEffect } from 'react';
import { Bell } from 'lucide-react';
import { useNavigate, useLocation } from 'react-router-dom';
import { notificationService } from '../services/notification.service';

const NotificationBellButton = () => {
  const [unreadCount, setUnreadCount] = useState(0);
  const navigate = useNavigate();
  const location = useLocation();

  const fetchUnreadCount = async () => {
    try {
      const res = await notificationService.getUnreadCount();
      if (res?.data) {
        setUnreadCount(res.data.unreadCount || 0);
      }
    } catch (e) {
      // Ignore background fetch errors
    }
  };

  useEffect(() => {
    fetchUnreadCount();
    const interval = setInterval(fetchUnreadCount, 10000);
    return () => clearInterval(interval);
  }, [location.pathname]);

  const handleClick = () => {
    navigate('/dashboard/notifications');
  };

  const isCurrentPage = location.pathname === '/dashboard/notifications';

  return (
    <button
      onClick={handleClick}
      className={`relative p-2 rounded-xl transition-all duration-150 cursor-pointer focus:outline-none flex items-center justify-center ${
        isCurrentPage
          ? 'bg-blue-100/70 text-blue-700 font-bold shadow-2xs'
          : 'text-slate-600 hover:text-blue-600 hover:bg-blue-50/80'
      }`}
      title="Notifications Workspace"
      aria-label="Open notifications workspace"
    >
      <Bell className="w-5 h-5" />
      {unreadCount > 0 && (
        <span className="absolute -top-1 -right-1 min-w-[18px] h-[18px] px-1 bg-red-600 text-white font-extrabold text-[10px] rounded-full flex items-center justify-center border-2 border-white shadow-xs animate-pulse">
          {unreadCount > 99 ? '99+' : unreadCount}
        </span>
      )}
    </button>
  );
};

export default NotificationBellButton;
