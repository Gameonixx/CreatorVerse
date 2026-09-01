import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import CreatorProfileHeader from '../components/creator/CreatorProfileHeader';
import CreatorContentGrid from '../components/creator/CreatorContentGrid';

export default function CreatorProfilePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [displayName, setDisplayName] = useState('');
  const [hasCreatorProfile, setHasCreatorProfile] = useState(false);
  const [upgrading, setUpgrading] = useState(false);

  const fetchProfileData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // 1. Fetch standard public user info
      const userData = await api.get(`/users/public/${id}`);
      
      // 2. Attempt to fetch creator profile info
      let creatorData = null;
      try {
        creatorData = await api.get(`/creators/profile/public/${id}`);
        setHasCreatorProfile(true);
      } catch (err) {
        // 404 is normal if they are just a standard user
        setHasCreatorProfile(false);
      }
      
      // Merge data
      setProfile({ ...userData, ...creatorData });
    } catch (err) {
      setError('Failed to load user profile');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (id) {
      fetchProfileData();
    }
  }, [id]);

  const handleBack = () => {
    navigate('/feed');
  };

  const handleFollowChange = (isFollowing) => {
    setProfile((prev) => {
      if (!prev) return prev;
      return {
        ...prev,
        isFollowedByCurrentUser: isFollowing,
        followerCount: Math.max(0, (prev.followerCount || 0) + (isFollowing ? 1 : -1))
      };
    });
  };

  const handleUpgrade = async () => {
    try {
      setUpgrading(true);
      await api.post(`/creators/profile?userId=${id}`, {
        niche: 'Digital Creator'
      });
      await fetchProfileData();
    } catch (err) {
      console.error('Failed to upgrade account', err);
      alert('Failed to upgrade account: ' + (err.message || 'Unknown error'));
    } finally {
      setUpgrading(false);
    }
  };

  const handleDowngrade = async () => {
    if (!window.confirm("Are you sure you want to switch to a Personal Account? Your Creator Profile metadata will be permanently deleted. Your content will remain intact.")) return;
    try {
      setUpgrading(true);
      await api.delete(`/creators/profile/${id}`);
      await fetchProfileData();
    } catch (err) {
      console.error('Failed to switch to personal account', err);
      alert('Failed to switch to personal account: ' + (err.message || 'Unknown error'));
    } finally {
      setUpgrading(false);
    }
  };

  const isOwnProfile = user?.id === parseInt(id, 10);

  return (
    <div className="creator-profile-page">
      <div className="creator-profile-actions">
        <button className="btn back-btn" onClick={handleBack} style={{ background: 'transparent', padding: '0', border: 'none', textDecoration: 'underline', cursor: 'pointer' }}>
          &larr; Back to Explore
        </button>
        <div className="creator-action-buttons">
          {isOwnProfile && !hasCreatorProfile && !loading && !error && (
            <button 
              className="btn btn-primary action-btn" 
              onClick={handleUpgrade}
              disabled={upgrading}
            >
              {upgrading ? 'Upgrading...' : 'Switch to Professional Account'}
            </button>
          )}
          {isOwnProfile && hasCreatorProfile && !loading && !error && (
            <button 
              className="btn action-btn" 
              onClick={handleDowngrade}
              disabled={upgrading}
            >
              {upgrading ? 'Switching...' : 'Switch to Personal Account'}
            </button>
          )}
        </div>
      </div>

      {loading && (
        <div className="creator-loading" style={{ marginBottom: '2rem' }}>
          Loading profile...
        </div>
      )}

      {!loading && error && (
        <div className="creator-error" style={{ marginBottom: '2rem' }}>
          {error}
        </div>
      )}

      {!loading && !error && profile && (
        <CreatorProfileHeader 
          profileData={profile} 
          displayName={displayName || profile.displayName || profile.username} 
          onFollowChange={handleFollowChange}
          isOwnProfile={isOwnProfile}
        />
      )}

      <CreatorContentGrid 
        creatorId={id} 
        onDisplayNameDiscovered={setDisplayName} 
      />
    </div>
  );
}
