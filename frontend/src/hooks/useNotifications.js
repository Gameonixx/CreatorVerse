import { useState, useEffect, useCallback } from 'react';
import { notificationApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

export const useNotifications = (pollingInterval = 30000) => {
  const { user } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [isOpen, setIsOpen] = useState(false);

  const fetchUnreadCount = useCallback(async () => {
    if (!user) return;
    try {
      const res = await notificationApi.getUnreadCount();
      setUnreadCount(res.unreadCount);
    } catch (err) {
      console.error('Failed to fetch unread count', err);
    }
  }, [user]);

  const fetchNotifications = useCallback(async (reset = false) => {
    if (!user) return;
    setIsLoading(true);
    try {
      const targetPage = reset ? 0 : page;
      const res = await notificationApi.getNotifications(targetPage, 20);
      if (reset) {
        setNotifications(res.content);
      } else {
        setNotifications(prev => {
          // avoid duplicates
          const newItems = res.content.filter(newItem => !prev.some(oldItem => oldItem.id === newItem.id));
          return [...prev, ...newItems];
        });
      }
      setPage(targetPage + 1);
      setHasMore(!res.last);
    } catch (err) {
      console.error('Failed to fetch notifications', err);
    } finally {
      setIsLoading(false);
    }
  }, [user, page]);

  useEffect(() => {
    fetchUnreadCount();
    const interval = setInterval(fetchUnreadCount, pollingInterval);
    return () => clearInterval(interval);
  }, [fetchUnreadCount, pollingInterval]);

  const markAsRead = async (id) => {
    try {
      await notificationApi.markAsRead(id);
      setNotifications(prev => prev.map(n => n.id === id ? { ...n, read: true } : n));
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch (err) {
      console.error('Failed to mark as read', err);
    }
  };

  const markAllAsRead = async () => {
    try {
      await notificationApi.markAllAsRead();
      setNotifications(prev => prev.map(n => ({ ...n, read: true })));
      setUnreadCount(0);
    } catch (err) {
      console.error('Failed to mark all as read', err);
    }
  };

  const toggleOpen = () => {
    setIsOpen(prev => {
      const next = !prev;
      if (next) {
        fetchNotifications(true);
      }
      return next;
    });
  };

  return {
    unreadCount,
    notifications,
    isLoading,
    hasMore,
    isOpen,
    setIsOpen,
    toggleOpen,
    fetchNotifications,
    markAsRead,
    markAllAsRead,
  };
};
