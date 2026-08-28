import React from 'react';

export default function CreatorProfileHeader({ profileData, displayName }) {
  if (!profileData) return null;

  const { niche, bio, followerCount, engagementRate } = profileData;
  const nameToDisplay = displayName || 'Creator Profile';

  return (
    <header className="creator-profile-header">
      <h1 className="creator-name">{nameToDisplay}</h1>
      
      {niche && (
        <div className="creator-niche">
          {niche}
        </div>
      )}
      
      {bio && (
        <p className="creator-bio">
          {bio}
        </p>
      )}

      <div className="creator-stats">
        <div className="stat-item">
          <span className="stat-value">{followerCount != null ? followerCount.toLocaleString() : '0'}</span>
          <span className="stat-label">Followers</span>
        </div>
        <div className="stat-item">
          <span className="stat-value">{engagementRate != null ? `${engagementRate.toFixed(1)}%` : '0.0%'}</span>
          <span className="stat-label">Engagement</span>
        </div>
      </div>
    </header>
  );
}
