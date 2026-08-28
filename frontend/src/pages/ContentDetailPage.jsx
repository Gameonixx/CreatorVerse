import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '../services/api';
import LikeButton from '../components/social/LikeButton';
import CommentSection from '../components/social/CommentSection';
import '../styles/content-detail.css';

export default function ContentDetailPage() {
  const { id } = useParams();
  const [content, setContent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchContent = async () => {
      try {
        setLoading(true);
        setError(null);
        const response = await api.get(`/content/${id}`);
        setContent(response);
      } catch (err) {
        setError(err.message || 'Unable to view this content.');
      } finally {
        setLoading(false);
      }
    };

    fetchContent();
  }, [id]);

  if (loading) {
    return (
      <div className="content-detail-page">
        <div className="content-detail-loading">
          Loading content...
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="content-detail-page">
        <div className="content-detail-error card">
          <h2>Content Unavailable</h2>
          <p>{error}</p>
          <Link to="/feed" className="btn primary">Back to Explore</Link>
        </div>
      </div>
    );
  }

  if (!content) {
    return null;
  }

  const {
    creatorId,
    creatorDisplayName,
    title,
    caption,
    contentType,
    mediaUrl,
    publishedAt,
    createdAt,
    likeCount,
    isLikedByCurrentUser
  } = content;

  const dateToFormat = publishedAt || createdAt;
  const publishDate = dateToFormat ? new Date(dateToFormat).toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  }) : '';

  const renderMedia = () => {
    if (!mediaUrl) return null;

    if (contentType === 'IMAGE') {
      return (
        <img 
          src={mediaUrl} 
          alt={title} 
          className="content-detail-media-image"
        />
      );
    }
    
    if (contentType === 'VIDEO') {
      return (
        <video 
          src={mediaUrl} 
          className="content-detail-media-video" 
          controls 
          preload="metadata"
        />
      );
    }

    return (
      <div className="content-detail-media-fallback">
        Unsupported content type
      </div>
    );
  };

  return (
    <article className="content-detail-page">
      <div className="content-detail-nav">
        <Link to="/feed" className="back-link">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" className="back-icon">
            <line x1="19" y1="12" x2="5" y2="12"></line>
            <polyline points="12 19 5 12 12 5"></polyline>
          </svg>
          Back to Explore
        </Link>
      </div>

      <div className="content-detail-container card">
        <div className="content-detail-media-wrapper">
          {renderMedia()}
        </div>

        <div className="content-detail-info">
          <div className="content-detail-header">
            <Link to={`/creator/${creatorId}`} className="content-detail-creator">
              {creatorDisplayName || 'Unknown Creator'}
            </Link>
            {publishDate && <span className="content-detail-date">{publishDate}</span>}
          </div>

          <h1 className="content-detail-title">{title}</h1>
          
          {caption && (
            <div className="content-detail-caption">
              {caption}
            </div>
          )}

          <div className="content-detail-actions">
            <LikeButton 
              contentId={content.id} 
              initialLikeCount={likeCount} 
              initialIsLiked={isLikedByCurrentUser} 
            />
          </div>
        </div>
      </div>
      
      <CommentSection contentId={content.id} />
    </article>
  );
}
