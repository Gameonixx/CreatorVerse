import React, { useState } from 'react';
import FollowButton from '../social/FollowButton';
import UserListModal from '../social/UserListModal';
import { api } from '../../services/api';

export default function UserProfileHeader({ profileData, displayName, onFollowChange, isOwnProfile }) {
  const [modalState, setModalState] = useState({ isOpen: false, type: 'followers' });
  const [isEditingBio, setIsEditingBio] = useState(false);
  const [editBioText, setEditBioText] = useState('');
  const [isSavingBio, setIsSavingBio] = useState(false);

  if (!profileData) return null;

  const { niche, bio, followerCount, engagementRate, isFollowedByCurrentUser } = profileData;
  const targetUserId = profileData.userId || profileData.id;
  const nameToDisplay = displayName || 'Creator Profile';

  const openFollowers = () => setModalState({ isOpen: true, type: 'followers' });
  
  // Assuming there's no followingCount provided currently, we only implement Followers.
  // If followingCount becomes available in the future, it can be added identically.

  const closeModal = () => setModalState({ ...modalState, isOpen: false });

  const handleEditBioClick = () => {
    setEditBioText(bio || '');
    setIsEditingBio(true);
  };

  const handleSaveBio = async () => {
    try {
      setIsSavingBio(true);
      await api.put(`/users/${targetUserId}`, { bio: editBioText });
      profileData.bio = editBioText; // Optimistic update
      setIsEditingBio(false);
    } catch (err) {
      alert('Failed to update bio');
    } finally {
      setIsSavingBio(false);
    }
  };

  const handleCancelEditBio = () => {
    setIsEditingBio(false);
    setEditBioText(bio || '');
  };

  return (
    <header className="creator-profile-header">
      <h1 className="creator-name">{nameToDisplay}</h1>
      
      {niche && (
        <div className="creator-niche">
          {niche}
        </div>
      )}
      
      {isOwnProfile && isEditingBio ? (
        <div className="creator-bio-edit" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', width: '100%', marginBottom: '1rem' }}>
          <textarea 
            value={editBioText} 
            onChange={(e) => setEditBioText(e.target.value)} 
            placeholder="Write a bio..."
            maxLength={500}
            rows={3}
            style={{ width: '100%', maxWidth: '500px', marginBottom: '0.5rem', padding: '0.5rem', borderRadius: '4px', border: '1px solid var(--border-color)', background: 'var(--bg-secondary)', color: 'var(--text-primary)', fontFamily: 'inherit' }}
          />
          <div className="creator-bio-edit-actions" style={{ display: 'flex', gap: '0.5rem', justifyContent: 'center' }}>
            <button className="btn primary" style={{ padding: '0.4rem 1rem', fontSize: '0.9rem' }} onClick={handleSaveBio} disabled={isSavingBio}>
              {isSavingBio ? 'Saving...' : 'Save'}
            </button>
            <button className="btn" style={{ padding: '0.4rem 1rem', fontSize: '0.9rem' }} onClick={handleCancelEditBio} disabled={isSavingBio}>
              Cancel
            </button>
          </div>
        </div>
      ) : (
        <div className="creator-bio-container" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginBottom: '1rem' }}>
          {bio ? (
            <p className="creator-bio" style={{ marginBottom: '0.5rem' }}>
              {bio}
            </p>
          ) : null}
          {isOwnProfile && (
            <button className="btn" onClick={handleEditBioClick} style={{ background: 'transparent', border: '1px solid var(--border-color)', fontSize: '0.85rem', padding: '0.3rem 0.8rem', marginTop: bio ? '0' : '0.5rem' }}>
              {bio ? 'Edit Bio' : 'Add Bio'}
            </button>
          )}
        </div>
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

      {!isOwnProfile && (
        <FollowButton 
          userId={targetUserId} 
          isFollowed={isFollowedByCurrentUser} 
          onFollowChange={onFollowChange} 
        />
      )}

      <UserListModal 
        userId={targetUserId} 
        type={modalState.type}
        isOpen={modalState.isOpen}
        onClose={closeModal}
      />
    </header>
  );
}
