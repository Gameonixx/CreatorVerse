import React, { useEffect, useState, useRef } from 'react';
import { socialApi } from '../../services/api';
import UserCard from './UserCard';

export default function UserListModal({ userId, type, isOpen, onClose }) {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const modalRef = useRef(null);

  useEffect(() => {
    if (!isOpen || !userId) {
      setUsers([]);
      return;
    }

    const fetchUsers = async () => {
      setLoading(true);
      setError(null);
      try {
        let data;
        if (type === 'followers') {
          data = await socialApi.getFollowers(userId);
        } else if (type === 'following') {
          data = await socialApi.getFollowing(userId);
        }
        setUsers(data || []);
      } catch (err) {
        setError(err.message || 'Failed to load users');
      } finally {
        setLoading(false);
      }
    };

    fetchUsers();
  }, [isOpen, userId, type]);

  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };

    if (isOpen) {
      document.body.style.overflow = 'hidden';
      document.addEventListener('keydown', handleEscape);
    }

    return () => {
      document.body.style.overflow = 'unset';
      document.removeEventListener('keydown', handleEscape);
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const handleBackdropClick = (e) => {
    if (modalRef.current && !modalRef.current.contains(e.target)) {
      onClose();
    }
  };

  const title = type === 'followers' ? 'Followers' : 'Following';

  return (
    <div className="modal-backdrop" onClick={handleBackdropClick}>
      <div className="modal-content" ref={modalRef} role="dialog" aria-modal="true" aria-labelledby="modal-title">
        <div className="modal-header">
          <h2 id="modal-title" className="modal-title">{title}</h2>
          <button className="modal-close-btn" onClick={onClose} aria-label="Close modal">
            &times;
          </button>
        </div>
        
        <div className="modal-body">
          {loading && <div className="modal-loading">Loading {title.toLowerCase()}...</div>}
          
          {!loading && error && <div className="modal-error">{error}</div>}
          
          {!loading && !error && users.length === 0 && (
            <div className="modal-empty">
              No {title.toLowerCase()} found.
            </div>
          )}
          
          {!loading && !error && users.length > 0 && (
            <div className="modal-user-list">
              {users.map(user => (
                <UserCard key={user.id} user={user} />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
