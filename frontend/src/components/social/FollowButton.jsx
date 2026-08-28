import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { socialApi } from '../../services/api';

export default function FollowButton({ userId, isFollowed, onFollowChange }) {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleClick = async () => {
    if (!user) {
      navigate('/login');
      return;
    }

    if (loading) return;

    setLoading(true);
    setError(null);

    try {
      if (isFollowed) {
        await socialApi.unfollowUser(userId);
        onFollowChange(false);
      } else {
        await socialApi.followUser(userId);
        onFollowChange(true);
      }
    } catch (err) {
      console.error('Follow action failed:', err);
      setError('Failed to update follow state. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="follow-btn-container">
      <button 
        className={`btn follow-btn ${isFollowed ? 'following' : 'primary'}`}
        onClick={handleClick}
        disabled={loading}
      >
        {loading ? '...' : isFollowed ? 'Following' : 'Follow'}
      </button>
      {error && <p className="follow-error-text">{error}</p>}
    </div>
  );
}
