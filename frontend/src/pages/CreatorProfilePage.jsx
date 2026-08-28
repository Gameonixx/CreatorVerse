import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import CreatorProfileHeader from '../components/creator/CreatorProfileHeader';
import CreatorContentGrid from '../components/creator/CreatorContentGrid';

export default function CreatorProfilePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [displayName, setDisplayName] = useState('');

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        setLoading(true);
        setError(null);
        // Using centralized API service to fetch public profile
        const data = await api.get(`/creators/profile/public/${id}`);
        setProfile(data);
      } catch (err) {
        // If they don't have a profile, we might get a 404. We can still let the ContentGrid show their content.
        // We'll store the error to show a message in the header section, but won't crash the page.
        setError(err.message || 'Failed to load profile details');
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchProfile();
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
        followerCount: Math.max(0, prev.followerCount + (isFollowing ? 1 : -1))
      };
    });
  };

  return (
    <div className="creator-profile-page">
      <div style={{ marginBottom: '1rem' }}>
        <button className="btn" onClick={handleBack} style={{ background: 'transparent', padding: '0', border: 'none', textDecoration: 'underline', cursor: 'pointer' }}>
          &larr; Back to Explore
        </button>
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
          displayName={displayName} 
          onFollowChange={handleFollowChange}
        />
      )}

      <CreatorContentGrid 
        creatorId={id} 
        onDisplayNameDiscovered={setDisplayName} 
      />
    </div>
  );
}
