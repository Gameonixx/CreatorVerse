import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { api } from '../../services/api';

export default function LikeButton({ contentId, initialLikeCount, initialIsLiked }) {
  const [likeCount, setLikeCount] = useState(initialLikeCount || 0);
  const [isLiked, setIsLiked] = useState(initialIsLiked || false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);
  
  const { user } = useAuth();
  const navigate = useNavigate();

  const handleLikeClick = async (e) => {
    e.preventDefault();
    e.stopPropagation();

    if (!user) {
      navigate('/login');
      return;
    }

    if (loading) return;

    setLoading(true);
    setError(false);

    try {
      if (isLiked) {
        await api.delete(`/social/content/${contentId}/unlike`);
        setIsLiked(false);
        setLikeCount((prev) => Math.max(0, prev - 1));
      } else {
        await api.post(`/social/content/${contentId}/like`);
        setIsLiked(true);
        setLikeCount((prev) => prev + 1);
      }
    } catch (err) {
      console.error('Failed to toggle like status:', err);
      setError(true);
      setTimeout(() => setError(false), 2000);
    } finally {
      setLoading(false);
    }
  };

  return (
    <button 
      className={`like-button ${isLiked ? 'liked' : ''} ${error ? 'error' : ''} ${loading ? 'loading' : ''}`}
      onClick={handleLikeClick}
      disabled={loading}
      aria-label={isLiked ? 'Unlike' : 'Like'}
    >
      <svg 
        xmlns="http://www.w3.org/2000/svg" 
        width="24" 
        height="24" 
        viewBox="0 0 24 24" 
        fill={isLiked ? "currentColor" : "none"} 
        stroke="currentColor" 
        strokeWidth="2.5" 
        strokeLinecap="round" 
        strokeLinejoin="round" 
        className="like-icon"
      >
        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
      </svg>
      <span className="like-count">{likeCount}</span>
    </button>
  );
}
