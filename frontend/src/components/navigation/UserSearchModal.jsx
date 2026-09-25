import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../../services/api';

export default function UserSearchModal({ isOpen, onClose }) {
  const [search, setSearch] = useState('');
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const modalRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (!isOpen) {
      setSearch('');
      setUsers([]);
    }
  }, [isOpen]);

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

  const handleSearchSubmit = async (e) => {
    e.preventDefault();
    if (!search.trim()) return;

    setLoading(true);
    setError(null);
    try {
      const response = await api.get(`/users/search?q=${encodeURIComponent(search)}&size=20`);
      setUsers(response.content || []);
    } catch (err) {
      console.error('Error fetching users', err);
      setError('Failed to load search results.');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  const handleBackdropClick = (e) => {
    if (modalRef.current && !modalRef.current.contains(e.target)) {
      onClose();
    }
  };

  const handleUserClick = (userId) => {
    onClose();
    navigate(`/user/${userId}`);
  };

  return (
    <div className="modal-backdrop" onClick={handleBackdropClick} style={{ zIndex: 1001, padding: '1rem', boxSizing: 'border-box' }}>
      <div className="modal-content" ref={modalRef} role="dialog" aria-modal="true" aria-labelledby="search-modal-title" style={{ maxWidth: '600px', width: '100%', alignSelf: 'flex-start', marginTop: '10vh', boxSizing: 'border-box', overflow: 'hidden' }}>
        <div className="modal-header" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '1rem', borderBottom: '1px solid var(--color-border)' }}>
          <h2 id="search-modal-title" className="modal-title" style={{ minWidth: 0, margin: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>Search Users</h2>
          <button className="modal-close-btn" onClick={onClose} aria-label="Close search" style={{ flexShrink: 0, background: 'none', border: 'none', fontSize: '1.5rem', cursor: 'pointer', color: 'var(--color-text-primary)' }}>
            &times;
          </button>
        </div>

        <div className="modal-body" style={{ padding: '1rem', boxSizing: 'border-box' }}>
          <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem', width: '100%', boxSizing: 'border-box' }}>
            <input
              type="text"
              placeholder="Search by username or name..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="form-input"
              style={{ flex: 1, minWidth: 0, padding: '0.6rem', borderRadius: '4px', border: '1px solid var(--color-border)', boxSizing: 'border-box' }}
              autoFocus
            />
            <button type="submit" className="btn primary" style={{ flex: '0 0 auto', padding: '0.6rem 1rem' }}>Search</button>
          </form>

          {loading && <div className="modal-loading">Searching...</div>}

          {!loading && error && <div className="modal-error" style={{ color: 'red' }}>{error}</div>}

          {!loading && !error && users.length === 0 && search && (
            <div className="modal-empty">
              No users found matching "{search}".
            </div>
          )}

          {!loading && !error && users.length > 0 && (
            <div className="search-results-list" style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {users.map(user => (
                <div
                  key={user.id}
                  className="user-card clickable"
                  onClick={() => handleUserClick(user.id)}
                  style={{ display: 'flex', alignItems: 'center', padding: '0.75rem', gap: '0.75rem', border: '1px solid var(--color-border)', borderRadius: '8px', cursor: 'pointer', background: 'var(--color-surface)', boxShadow: 'var(--shadow-brutal)', boxSizing: 'border-box', overflow: 'hidden' }}
                >
                  <div className="user-card-avatar" style={{ width: '40px', height: '40px', flexShrink: 0, borderRadius: '50%', backgroundColor: 'var(--color-accent)', display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden', border: '1px solid var(--color-border)' }}>
                    {user.avatarUrl ? (
                      <img src={user.avatarUrl} alt={user.username} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                    ) : (
                      (user.displayName || user.username || '?').charAt(0).toUpperCase()
                    )}
                  </div>
                  <div className="user-card-info" style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
                    <span style={{ fontWeight: '600', color: 'var(--color-text-primary)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{user.displayName || user.username}</span>
                    <span style={{ fontSize: '0.85rem', color: 'var(--color-text-secondary)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>@{user.username}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
