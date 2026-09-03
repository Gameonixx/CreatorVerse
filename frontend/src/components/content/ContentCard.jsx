import React from 'react';
import { Link } from 'react-router-dom';
import LikeButton from '../social/LikeButton';

export default function ContentCard({ content }) {
  const {
    id,
    creatorId,
    creatorDisplayName,
    title,
    caption,
    contentType,
    mediaUrl,
    publishedAt,
    likeCount,
    isLikedByCurrentUser
  } = content;

  // Format date
  const publishDate = publishedAt ? new Date(publishedAt).toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  }) : '';

  const renderMedia = () => {
    if (!mediaUrl) return null;

    if (contentType === 'IMAGE') {
      return (
        <img 
          src={mediaUrl} 
          alt={title} 
          className="content-card-media-image"
          loading="lazy" 
        />
      );
    }
    
    if (contentType === 'VIDEO') {
      return (
        <video 
          src={mediaUrl} 
          className="content-card-media-video" 
          controls 
          preload="metadata"
        />
      );
    }

    return (
      <div className="content-card-media-fallback">
        View Content
      </div>
    );
  };

  return (
    <article className="content-card">
      <div className="content-card-header">
        <Link to={`/user/${creatorId}`} className="content-card-creator">
          {creatorDisplayName || 'Unknown Creator'}
        </Link>
        {publishDate && <span className="content-card-date">{publishDate}</span>}
      </div>

      <Link to={`/content/${id}`} className="content-card-media-link">
        <div className="content-card-media-container">
          {renderMedia()}
        </div>
      </Link>

      <div className="content-card-body">
        <Link to={`/content/${id}`} className="content-card-title-link">
          <h2 className="content-card-title">{title}</h2>
        </Link>
        
        {caption && (
          <p className="content-card-caption">
            {caption}
          </p>
        )}

        <div className="content-card-actions">
          <LikeButton 
            contentId={id} 
            initialLikeCount={likeCount} 
            initialIsLiked={isLikedByCurrentUser} 
          />
        </div>
      </div>
    </article>
  );
}
