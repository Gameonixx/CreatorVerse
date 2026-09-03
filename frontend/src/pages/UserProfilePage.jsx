import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import UserProfileHeader from '../components/user/UserProfileHeader';
import UserContentGrid from '../components/user/UserContentGrid';

export default function UserProfilePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, refreshProfileModes, hasCreatorMode, hasBrandMode } = useAuth();
  
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [displayName, setDisplayName] = useState('');
  const [hasCreatorProfile, setHasCreatorProfile] = useState(false);
  const [hasBrandProfile, setHasBrandProfile] = useState(false);
  const [upgrading, setUpgrading] = useState(false);
  
  const [showBrandForm, setShowBrandForm] = useState(false);
  const [brandForm, setBrandForm] = useState({ companyName: '', industry: '', description: '', websiteUrl: '' });

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
        setHasCreatorProfile(false);
      }
      
      // 3. Attempt to fetch brand profile info
      let brandData = null;
      try {
        brandData = await api.get(`/brands/profile/public/${id}`);
        setHasBrandProfile(true);
      } catch (err) {
        setHasBrandProfile(false);
      }
      
      // Merge data (note: overlapping fields might need care, but for now we merge)
      setProfile({ ...userData, ...creatorData, ...brandData });
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
      if (refreshProfileModes) await refreshProfileModes();
    } catch (err) {
      console.error('Failed to activate creator mode', err);
      alert('Failed to activate creator mode: ' + (err.message || 'Unknown error'));
    } finally {
      setUpgrading(false);
    }
  };

  const handleDowngrade = async () => {
    if (!window.confirm("Are you sure you want to deactivate Creator Mode? Your universal User account, content, followers, following, likes, comments, bio and avatar will remain intact.")) return;
    try {
      setUpgrading(true);
      await api.delete(`/creators/profile/${id}`);
      await fetchProfileData();
      if (refreshProfileModes) await refreshProfileModes();
    } catch (err) {
      console.error('Failed to deactivate creator mode', err);
      alert('Failed to deactivate creator mode: ' + (err.message || 'Unknown error'));
    } finally {
      setUpgrading(false);
    }
  };

  const handleActivateBrand = async (e) => {
    e.preventDefault();
    if (!brandForm.companyName) return alert("Company name is required.");
    try {
      setUpgrading(true);
      await api.post(`/brands/profile?userId=${id}`, brandForm);
      await fetchProfileData();
      if (refreshProfileModes) await refreshProfileModes();
      setShowBrandForm(false);
    } catch (err) {
      console.error('Failed to activate brand mode', err);
      alert('Failed to activate brand mode: ' + (err.message || 'Unknown error'));
    } finally {
      setUpgrading(false);
    }
  };

  const handleDeactivateBrand = async () => {
    if (!window.confirm("Are you sure you want to deactivate Brand Mode? Your universal User account, content, followers, following, likes, comments, bio and avatar will remain intact.")) return;
    try {
      setUpgrading(true);
      await api.delete(`/brands/profile/${id}`);
      await fetchProfileData();
      if (refreshProfileModes) await refreshProfileModes();
    } catch (err) {
      console.error('Failed to deactivate brand mode', err);
      alert('Failed to deactivate brand mode: ' + (err.message || 'Unknown error'));
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
        <>
          <UserProfileHeader 
            profileData={profile} 
            displayName={displayName || profile.displayName || profile.username} 
            onFollowChange={handleFollowChange}
            isOwnProfile={isOwnProfile}
            hasCreatorProfile={isOwnProfile ? hasCreatorMode : hasCreatorProfile}
            hasBrandProfile={isOwnProfile ? hasBrandMode : hasBrandProfile}
            onActivateCreator={handleUpgrade}
            onDeactivateCreator={handleDowngrade}
            onActivateBrand={() => setShowBrandForm(true)}
            onDeactivateBrand={handleDeactivateBrand}
          />
          
          {showBrandForm && !hasBrandProfile && (
            <div className="brand-activation-form card" style={{ maxWidth: '600px', margin: '0 auto 2rem auto', padding: '1.5rem' }}>
              <h3 style={{ marginTop: '0', marginBottom: '1rem', fontSize: '1.1rem' }}>Activate Brand Mode</h3>
              <form onSubmit={handleActivateBrand} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold', fontSize: '0.9rem' }}>Brand / Company Name</label>
                  <input type="text" value={brandForm.companyName} onChange={e => setBrandForm({...brandForm, companyName: e.target.value})} required className="form-input" style={{ width: '100%', padding: '0.6rem', borderRadius: '4px', border: '1px solid var(--border-color)', background: 'var(--bg-secondary)', color: 'var(--text-primary)' }} />
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold', fontSize: '0.9rem' }}>Industry</label>
                  <input type="text" value={brandForm.industry} onChange={e => setBrandForm({...brandForm, industry: e.target.value})} required className="form-input" style={{ width: '100%', padding: '0.6rem', borderRadius: '4px', border: '1px solid var(--border-color)', background: 'var(--bg-secondary)', color: 'var(--text-primary)' }} />
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold', fontSize: '0.9rem' }}>Description</label>
                  <textarea value={brandForm.description} onChange={e => setBrandForm({...brandForm, description: e.target.value})} className="form-input" style={{ width: '100%', padding: '0.6rem', borderRadius: '4px', border: '1px solid var(--border-color)', background: 'var(--bg-secondary)', color: 'var(--text-primary)' }} rows={3}></textarea>
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold', fontSize: '0.9rem' }}>Website</label>
                  <input type="url" value={brandForm.websiteUrl} onChange={e => setBrandForm({...brandForm, websiteUrl: e.target.value})} className="form-input" style={{ width: '100%', padding: '0.6rem', borderRadius: '4px', border: '1px solid var(--border-color)', background: 'var(--bg-secondary)', color: 'var(--text-primary)' }} />
                </div>
                <div style={{ display: 'flex', gap: '1rem', marginTop: '0.5rem' }}>
                  <button type="submit" className="btn primary" disabled={upgrading}>{upgrading ? 'Activating...' : 'Activate Brand Mode'}</button>
                  <button type="button" className="btn" onClick={() => setShowBrandForm(false)} disabled={upgrading}>Cancel</button>
                </div>
              </form>
            </div>
          )}
        </>
      )}

      <UserContentGrid 
        userId={id} 
        onDisplayNameDiscovered={setDisplayName} 
      />
    </div>
  );
}
