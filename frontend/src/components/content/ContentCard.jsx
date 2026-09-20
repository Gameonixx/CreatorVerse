import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import LikeButton from '../social/LikeButton';
import CommentSection from '../social/CommentSection';

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
    isLikedByCurrentUser,
    commentCount
  } = content;

  const [showComments, setShowComments] = useState(false);
  const [showMenu, setShowMenu] = useState(false);

  // Format date
  const publishDate = publishedAt ? new Date(publishedAt).toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  }) : '';

  // Get initials for avatar
  const getInitials = (name) => {
    if (!name) return '??';
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  };
  const initials = getInitials(creatorDisplayName || 'Unknown Creator');


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
    <article className="content-card" style={{ padding: '1rem', border: '2px solid var(--color-border)', borderRadius: '12px', background: 'var(--color-surface)', boxShadow: 'var(--shadow-brutal)', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
      
      {/* 1. Header (Creator Identity & Date) */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Link to={`/user/${creatorId}`} style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', textDecoration: 'none', color: 'inherit', minWidth: 0 }}>
          <div style={{ width: '40px', height: '40px', borderRadius: '50%', backgroundColor: '#fcdcb4', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold', fontSize: '1rem', flexShrink: 0, border: '1px solid var(--color-border)' }}>
            {initials}
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
            <span style={{ fontWeight: 'bold', fontSize: '1rem', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{creatorDisplayName || 'Unknown Creator'}</span>
            <span style={{ fontSize: '0.85rem', color: 'var(--color-text-secondary)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>@{creatorDisplayName ? creatorDisplayName.replace(/\s+/g, '').toLowerCase() : 'user'}</span>
          </div>
        </Link>
        {publishDate && <span style={{ fontSize: '0.85rem', color: 'var(--color-text-secondary)', whiteSpace: 'nowrap', flexShrink: 0, marginLeft: '0.5rem' }}>{publishDate}</span>}
      </div>

      {/* 2. Media */}
      <Link to={`/content/${id}`} style={{ display: 'block', borderRadius: '8px', overflow: 'hidden' }}>
        <div style={{ width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden' }}>
          {renderMedia()}
        </div>
      </Link>

      {/* 3. Caption / Context */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
        <Link to={`/content/${id}`} style={{ textDecoration: 'none', color: 'var(--color-text-primary)' }}>
          <h2 style={{ fontSize: '1.25rem', margin: 0 }}>{title}</h2>
        </Link>
        {caption && (
          <p style={{ margin: 0, fontSize: '1rem', color: 'var(--color-text-primary)', wordWrap: 'break-word', overflowWrap: 'break-word', whiteSpace: 'pre-wrap' }}>
            {caption}
          </p>
        )}
      </div>

      {/* 4. Engagement Controls */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '0.25rem' }}>
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <LikeButton 
            contentId={id} 
            initialLikeCount={likeCount} 
            initialIsLiked={isLikedByCurrentUser} 
          />
          <button 
            onClick={() => setShowComments(!showComments)}
            style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', color: 'var(--color-text-primary)', background: 'none', border: 'none', padding: '0', cursor: 'pointer', boxShadow: 'none' }}
            aria-label="Toggle Comments"
          >
            <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round">
              <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"></path>
            </svg>
            <span style={{ fontSize: '0.9rem', fontWeight: '500' }}>{commentCount || 0}</span>
          </button>
        </div>
        
        <div style={{ position: 'relative' }}>
          <button 
            onClick={() => setShowMenu(!showMenu)}
            style={{ background: 'none', border: 'none', padding: '0.25rem', cursor: 'pointer', color: 'var(--color-text-primary)', boxShadow: 'none' }}
            aria-label="More Options"
          >
            <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="12" cy="12" r="1"></circle>
              <circle cx="12" cy="5" r="1"></circle>
              <circle cx="12" cy="19" r="1"></circle>
            </svg>
          </button>
          
          {showMenu && (
            <div style={{ position: 'absolute', bottom: '100%', right: '0', marginBottom: '0.5rem', background: 'var(--color-surface)', border: '2px solid var(--color-border)', borderRadius: '8px', padding: '0.5rem 0', display: 'flex', flexDirection: 'column', minWidth: '150px', zIndex: 10, boxShadow: 'var(--shadow-brutal)' }}>
              <Link to={`/user/${creatorId}`} style={{ padding: '0.5rem 1rem', textDecoration: 'none', color: 'var(--color-text-primary)', fontSize: '0.9rem', fontWeight: 600, display: 'block' }}>
                View Profile
              </Link>
              <button 
                onClick={() => {
                  navigator.clipboard.writeText(window.location.origin + '/content/' + id);
                  setShowMenu(false);
                  alert('Link copied to clipboard!');
                }}
                style={{ background: 'none', border: 'none', padding: '0.5rem 1rem', textAlign: 'left', cursor: 'pointer', color: 'var(--color-text-primary)', fontSize: '0.9rem', fontWeight: 600, width: '100%', boxShadow: 'none' }}
              >
                Copy Link
              </button>
            </div>
          )}
        </div>
      </div>

      {showComments && (
        <div style={{ marginTop: '0.5rem', borderTop: '1px solid var(--color-border)', paddingTop: '1rem' }}>
          <CommentSection contentId={id} />
        </div>
      )}

    </article>
  );
}
