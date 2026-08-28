import React, { useState } from 'react';
import FollowButton from '../social/FollowButton';
import UserListModal from '../social/UserListModal';

export default function CreatorProfileHeader({ profileData, displayName, onFollowChange }) {
  const [modalState, setModalState] = useState({ isOpen: false, type: 'followers' });

  if (!profileData) return null;

  const { userId, niche, bio, followerCount, engagementRate, isFollowedByCurrentUser } = profileData;
  const nameToDisplay = displayName || 'Creator Profile';

  const openFollowers = () => setModalState({ isOpen: true, type: 'followers' });
  
  // Assuming there's no followingCount provided currently, we only implement Followers.
  // If followingCount becomes available in the future, it can be added identically.

  const closeModal = () => setModalState({ ...modalState, isOpen: false });

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
        <div 
          className="stat-item" 
          onClick={openFollowers}
          style={{ cursor: 'pointer', transition: 'transform 0.1s ease' }}
          onMouseOver={(e) => e.currentTarget.style.transform = 'scale(1.05)'}
          onMouseOut={(e) => e.currentTarget.style.transform = 'scale(1)'}
          title="View Followers"
        >
          <span className="stat-value">{followerCount != null ? followerCount.toLocaleString() : '0'}</span>
          <span className="stat-label" style={{ textDecoration: 'underline' }}>Followers</span>
        </div>
        <div className="stat-item">
          <span className="stat-value">{engagementRate != null ? `${engagementRate.toFixed(1)}%` : '0.0%'}</span>
          <span className="stat-label">Engagement</span>
        </div>
      </div>

      <FollowButton 
        userId={userId} 
        isFollowed={isFollowedByCurrentUser} 
        onFollowChange={onFollowChange} 
      />

      <UserListModal 
        userId={userId} 
        type={modalState.type}
        isOpen={modalState.isOpen}
        onClose={closeModal}
      />
    </header>
  );
}
