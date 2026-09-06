import React from 'react';
import { Link } from 'react-router-dom';
import '../../styles/creator-card.css'; // Will create this if it doesn't exist or use inline styles

export default function CreatorCard({ creator }) {
  const { userId, username, displayName, avatarUrl, bio, followerCount, niche, engagementRate } = creator;

  const defaultAvatar = 'https://ui-avatars.com/api/?name=' + encodeURIComponent(displayName || username) + '&background=random';

  return (
    <Link to={`/user/${userId}`} className="creator-card" style={{ textDecoration: 'none', color: 'inherit' }}>
      <div className="creator-card-header">
        <img 
          src={avatarUrl || defaultAvatar} 
          alt={username} 
          className="creator-card-avatar" 
          onError={(e) => { e.target.src = defaultAvatar; }} 
        />
        <div className="creator-card-info">
          <h3 className="creator-card-name">{displayName || username}</h3>
          <span className="creator-card-username">@{username}</span>
          {niche && <div className="creator-card-niche">{niche}</div>}
        </div>
        <div className="creator-card-arrow">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="9 18 15 12 9 6"></polyline>
          </svg>
        </div>
      </div>

      <div className="creator-card-body desktop-only-bio">
        <p className="creator-card-bio">{bio ? bio.substring(0, 100) + (bio.length > 100 ? '...' : '') : 'No bio available'}</p>
      </div>

      <div className="creator-card-footer">
        <div className="creator-stat">
          <span className="stat-icon-wrapper mobile-only-stat">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{marginRight: '6px'}}><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
          </span>
          <span className="stat-value">{followerCount != null ? followerCount.toLocaleString() : '0'}</span>
          <span className="stat-label desktop-only-stat">Followers</span>
        </div>
          <>
            <div className="mobile-only-stat stat-divider"></div>
            <div className="creator-stat" title="Stored Engagement Rate">
              <span className="stat-icon-wrapper mobile-only-stat">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{marginRight: '6px'}}><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><line x1="12" y1="8" x2="12" y2="16"></line><line x1="8" y1="12" x2="8" y2="16"></line><line x1="16" y1="10" x2="16" y2="16"></line></svg>
              </span>
              <span className="stat-value" style={engagementRate == null ? {fontSize: '0.85em', fontWeight: 'normal', color: 'var(--text-secondary, #666)'} : {}}>
                {engagementRate != null ? `${engagementRate.toFixed(2)}%` : 'Not enough data'}
              </span>
              <span className="stat-label desktop-only-stat">Engagement</span>
            </div>
          </>
      </div>
    </Link>
  );
}
