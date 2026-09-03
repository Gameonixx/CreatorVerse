import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import FollowButton from '../social/FollowButton';
import UserListModal from '../social/UserListModal';
import { api } from '../../services/api';

export default function UserProfileHeader({ 
  profileData, 
  displayName, 
  onFollowChange, 
  isOwnProfile,
  hasCreatorProfile,
  hasBrandProfile,
  onActivateCreator,
  onDeactivateCreator,
  onActivateBrand,
  onDeactivateBrand
}) {
  const navigate = useNavigate();
  const [modalState, setModalState] = useState({ isOpen: false, type: 'followers' });
  const [isEditingBio, setIsEditingBio] = useState(false);
  const [editBioText, setEditBioText] = useState('');
  const [isSavingBio, setIsSavingBio] = useState(false);
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const menuRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setIsMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  if (!profileData) return null;

  const { niche, bio, followerCount, engagementRate, isFollowedByCurrentUser, companyName, industry, websiteUrl, logoUrl } = profileData;
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
    <header className="creator-profile-header" style={{ position: 'relative' }}>
      
      {isOwnProfile && (
        <div className="profile-actions-menu" ref={menuRef} style={{ position: 'absolute', top: '0', right: '0' }}>
          <button 
            className="btn icon-btn" 
            onClick={() => setIsMenuOpen(!isMenuOpen)} 
            aria-label="More profile actions"
            style={{ background: 'transparent', border: 'none', fontSize: '1.5rem', padding: '0.2rem 0.5rem', cursor: 'pointer', color: 'var(--text-secondary)' }}
          >
            ⋮
          </button>
          
          {isMenuOpen && (
            <div className="card" style={{ position: 'absolute', top: '100%', right: '0', zIndex: 10, minWidth: '220px', padding: '0.5rem 0', display: 'flex', flexDirection: 'column', boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }}>
              <div style={{ padding: '0.5rem 1rem', fontSize: '0.8rem', fontWeight: 'bold', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                Professional
              </div>
              <hr style={{ margin: '0.25rem 0', border: 'none', borderTop: '1px solid var(--border-color)' }} />
              
              {!hasCreatorProfile && (
                 <button className="menu-item text-left" style={{ width: '100%', background: 'none', border: 'none', padding: '0.75rem 1rem', cursor: 'pointer', fontFamily: 'inherit', fontSize: '0.95rem' }} onClick={() => { onActivateCreator(); setIsMenuOpen(false); }}>Activate Creator Mode</button>
              )}
              {hasCreatorProfile && (
                 <button className="menu-item text-left" style={{ width: '100%', background: 'none', border: 'none', padding: '0.75rem 1rem', cursor: 'pointer', fontFamily: 'inherit', fontSize: '0.95rem' }} onClick={() => { navigate('/dashboard/creator'); setIsMenuOpen(false); }}>Creator Dashboard</button>
              )}
              
              {!hasBrandProfile && (
                 <button className="menu-item text-left" style={{ width: '100%', background: 'none', border: 'none', padding: '0.75rem 1rem', cursor: 'pointer', fontFamily: 'inherit', fontSize: '0.95rem' }} onClick={() => { onActivateBrand(); setIsMenuOpen(false); }}>Activate Brand Mode</button>
              )}
              {hasBrandProfile && (
                 <button className="menu-item text-left" style={{ width: '100%', background: 'none', border: 'none', padding: '0.75rem 1rem', cursor: 'pointer', fontFamily: 'inherit', fontSize: '0.95rem' }} onClick={() => { navigate('/dashboard/brand'); setIsMenuOpen(false); }}>Brand Dashboard</button>
              )}
              
              {(hasCreatorProfile || hasBrandProfile) && (
                <>
                  <hr style={{ margin: '0.5rem 0', border: 'none', borderTop: '1px solid var(--border-color)' }} />
                  {hasCreatorProfile && (
                    <button className="menu-item text-left text-danger" style={{ width: '100%', background: 'none', border: 'none', padding: '0.75rem 1rem', cursor: 'pointer', fontFamily: 'inherit', fontSize: '0.95rem', color: '#ff4444' }} onClick={() => { onDeactivateCreator(); setIsMenuOpen(false); }}>Deactivate Creator Mode</button>
                  )}
                  {hasBrandProfile && (
                    <button className="menu-item text-left text-danger" style={{ width: '100%', background: 'none', border: 'none', padding: '0.75rem 1rem', cursor: 'pointer', fontFamily: 'inherit', fontSize: '0.95rem', color: '#ff4444' }} onClick={() => { onDeactivateBrand(); setIsMenuOpen(false); }}>Deactivate Brand Mode</button>
                  )}
                </>
              )}
            </div>
          )}
        </div>
      )}

      <h1 className="creator-name">{nameToDisplay}</h1>
      
      {niche && (
        <div className="creator-niche" style={{ marginBottom: '0.5rem' }}>
          {niche}
        </div>
      )}
      
      {companyName && (
        <div className="brand-metadata" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginBottom: '1rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
          <div style={{ fontWeight: 'bold' }}>{companyName} {industry ? `• ${industry}` : ''}</div>
          {websiteUrl && <a href={websiteUrl} target="_blank" rel="noreferrer" style={{ color: 'var(--accent-primary)' }}>{websiteUrl}</a>}
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
