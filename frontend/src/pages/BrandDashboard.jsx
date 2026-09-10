import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import { api } from '../services/api';

export default function BrandDashboard() {
  const { user } = useAuth();
  const [hasProfile, setHasProfile] = useState(null);
  
  const [campaigns, setCampaigns] = useState([]);
  const [loading, setLoading] = useState(false);
  
  const [showCreate, setShowCreate] = useState(false);
  const [newCampaign, setNewCampaign] = useState({ title: '', description: '', niche: '', budget: '', applicationDeadline: '' });
  const [creating, setCreating] = useState(false);

  const [selectedCampaign, setSelectedCampaign] = useState(null);
  const [applications, setApplications] = useState([]);
  const [loadingApps, setLoadingApps] = useState(false);

  useEffect(() => {
    if (user) {
      api.get(`/brands/profile/${user.id}`)
        .then(() => {
          setHasProfile(true);
          fetchCampaigns();
        })
        .catch(() => setHasProfile(false));
    }
  }, [user]);

  const fetchCampaigns = async () => {
    try {
      setLoading(true);
      const res = await api.get('/campaigns/mine');
      setCampaigns(res.content);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      setCreating(true);
      await api.post('/campaigns', newCampaign);
      setShowCreate(false);
      setNewCampaign({ title: '', description: '', niche: '', budget: '', applicationDeadline: '' });
      fetchCampaigns();
    } catch (err) {
      alert('Failed to create campaign');
    } finally {
      setCreating(false);
    }
  };

  const handleUpdateStatus = async (id, newStatus) => {
    try {
      await api.put(`/campaigns/${id}`, { status: newStatus });
      fetchCampaigns();
    } catch (err) {
      alert(err.data?.message || 'Failed to update status');
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm("Are you sure you want to delete this draft?")) {
      try {
        await api.delete(`/campaigns/${id}`);
        fetchCampaigns();
      } catch (err) {
        alert(err.data?.message || 'Failed to delete');
      }
    }
  };

  const loadApplications = async (campaign) => {
    setSelectedCampaign(campaign);
    try {
      setLoadingApps(true);
      const res = await api.get(`/campaigns/${campaign.id}/applications`);
      setApplications(res.content);
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingApps(false);
    }
  };

  const handleReview = async (appId, status) => {
    try {
      await api.put(`/campaigns/${selectedCampaign.id}/applications/${appId}/status`, { status });
      // Refresh apps
      loadApplications(selectedCampaign);
    } catch (err) {
      alert('Failed to update application');
    }
  };

  if (hasProfile === null) return <div>Loading dashboard...</div>;

  if (hasProfile === false) {
    return (
      <div className="dashboard-container" style={{ textAlign: 'center' }}>
        <h2>Brand Dashboard Restricted</h2>
        <p>You need to activate a Brand Profile to access this workspace.</p>
        <Link to={`/user/${user.id}`} className="btn primary" style={{ marginTop: '1rem', display: 'inline-block' }}>Go to Profile Settings</Link>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <div>
          <h1 style={{ margin: 0 }}>Brand Dashboard</h1>
          <p style={{ color: 'var(--text-secondary)', margin: '0.5rem 0 0 0' }}>Welcome to your workspace, {user?.displayName || user?.username}.</p>
        </div>
        <button className="btn primary" onClick={() => setShowCreate(!showCreate)}>
          {showCreate ? 'Cancel' : '+ Create Campaign'}
        </button>
      </div>
      
      {showCreate && (
        <div style={{ background: 'var(--bg-secondary)', padding: '2rem', borderRadius: '12px', border: '1px solid var(--border-color)', marginBottom: '2rem' }}>
          <h3>Create New Campaign</h3>
          <form onSubmit={handleCreate} style={{ display: 'grid', gap: '1rem' }}>
            <input required placeholder="Campaign Title" value={newCampaign.title} onChange={e => setNewCampaign({...newCampaign, title: e.target.value})} className="form-input" style={{ padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--border-color)', background: 'var(--bg-primary)', color: 'var(--text-primary)' }}/>
            <textarea required rows={4} placeholder="Description" value={newCampaign.description} onChange={e => setNewCampaign({...newCampaign, description: e.target.value})} style={{ padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--border-color)', background: 'var(--bg-primary)', color: 'var(--text-primary)' }}/>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem' }}>
              <input required placeholder="Niche (e.g., Tech)" value={newCampaign.niche} onChange={e => setNewCampaign({...newCampaign, niche: e.target.value})} className="form-input" style={{ padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--border-color)', background: 'var(--bg-primary)', color: 'var(--text-primary)' }}/>
              <input type="number" required placeholder="Budget ($)" value={newCampaign.budget} onChange={e => setNewCampaign({...newCampaign, budget: e.target.value})} className="form-input" style={{ padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--border-color)', background: 'var(--bg-primary)', color: 'var(--text-primary)' }}/>
              <input type="datetime-local" placeholder="Deadline (optional)" value={newCampaign.applicationDeadline} onChange={e => setNewCampaign({...newCampaign, applicationDeadline: e.target.value})} className="form-input" style={{ padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--border-color)', background: 'var(--bg-primary)', color: 'var(--text-primary)' }}/>
            </div>
            <button type="submit" className="btn primary" disabled={creating} style={{ justifySelf: 'start', padding: '0.75rem 2rem' }}>{creating ? 'Saving...' : 'Save as Draft'}</button>
          </form>
        </div>
      )}

      {selectedCampaign ? (
        <div style={{ background: 'var(--bg-secondary)', padding: '2rem', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
          <button className="btn" onClick={() => setSelectedCampaign(null)} style={{ marginBottom: '1rem' }}>&larr; Back to Campaigns</button>
          <h3>Applications for: {selectedCampaign.title}</h3>
          
          {loadingApps ? <p>Loading applications...</p> : applications.length === 0 ? <p style={{ color: 'var(--text-secondary)' }}>No applications yet.</p> : (
            <div style={{ display: 'grid', gap: '1rem' }}>
              {applications.map(app => (
                <div key={app.id} style={{ padding: '1.5rem', border: '1px solid var(--border-color)', borderRadius: '8px', background: 'var(--bg-primary)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center' }}>
                      <img src={app.creatorAvatarUrl || `https://ui-avatars.com/api/?name=${app.creatorName}&background=random`} alt={app.creatorName} style={{ width: '40px', height: '40px', borderRadius: '50%', marginRight: '12px' }} />
                      <div>
                        <div style={{ fontWeight: 'bold' }}><Link to={`/user/${app.creatorUserId}`} style={{ color: 'inherit' }}>{app.creatorName}</Link></div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Followers: {app.creatorFollowerCount?.toLocaleString() || 0} • Engagement: {app.creatorEngagementRate ? app.creatorEngagementRate + '%' : 'N/A'}</div>
                      </div>
                    </div>
                    <div style={{ fontWeight: 'bold', color: app.status === 'PENDING' ? '#FFC107' : app.status === 'ACCEPTED' ? '#4CAF50' : 'var(--text-secondary)' }}>
                      {app.status}
                    </div>
                  </div>
                  <p style={{ margin: '0 0 1rem 0', whiteSpace: 'pre-wrap', fontSize: '0.9rem' }}>{app.message}</p>
                  
                  {app.status === 'PENDING' && (
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button className="btn" style={{ background: '#4CAF50', color: 'white', border: 'none', padding: '0.5rem 1rem' }} onClick={() => handleReview(app.id, 'ACCEPTED')}>Accept</button>
                      <button className="btn" style={{ background: '#ff4444', color: 'white', border: 'none', padding: '0.5rem 1rem' }} onClick={() => handleReview(app.id, 'REJECTED')}>Reject</button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      ) : (
        <div>
          <h3>My Campaigns</h3>
          {loading ? <p>Loading...</p> : campaigns.length === 0 ? <p style={{ color: 'var(--text-secondary)' }}>You haven't created any campaigns yet.</p> : (
          <div className="dashboard-list">
              {campaigns.map(camp => (
                <div key={camp.id} className="dashboard-list-item">
                  <div className="dashboard-list-item-content">
                    <h4 className="dashboard-list-item-title">{camp.title}</h4>
                    <div className="dashboard-list-item-desc" style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
                      <span style={{ fontWeight: 'bold', color: camp.status === 'OPEN' ? '#4CAF50' : camp.status === 'DRAFT' ? '#FFC107' : 'var(--text-secondary)' }}>{camp.status}</span>
                      <span>Budget: ${camp.budget}</span>
                      <span>Deadline: {camp.applicationDeadline ? new Date(camp.applicationDeadline).toLocaleDateString() : 'None'}</span>
                    </div>
                  </div>
                  <div className="dashboard-list-item-actions">
                    {camp.status === 'DRAFT' && <button className="btn" onClick={() => handleUpdateStatus(camp.id, 'OPEN')}>Open</button>}
                    {camp.status === 'OPEN' && <button className="btn" onClick={() => handleUpdateStatus(camp.id, 'PAUSED')}>Pause</button>}
                    {(camp.status === 'OPEN' || camp.status === 'PAUSED') && <button className="btn" onClick={() => handleUpdateStatus(camp.id, 'CLOSED')}>Close</button>}
                    {camp.status === 'DRAFT' && <button className="btn text-danger" style={{ color: '#ff4444' }} onClick={() => handleDelete(camp.id)}>Delete</button>}
                    
                    <button className="btn primary" onClick={() => loadApplications(camp)}>View Apps</button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Roadmap Cleanup */}
      <div className="dashboard-grid" style={{ marginTop: '3rem', paddingTop: '2rem', borderTop: 'var(--border-width) solid var(--color-border)' }}>
        <div className="card">
          <h3>Creator Discovery</h3>
          <p style={{ color: 'var(--color-text-secondary)' }}>Find and connect with top creators for your brand.</p>
        </div>
        <div className="card">
          <h3>Analytics</h3>
          <p style={{ color: 'var(--color-text-secondary)' }}>Track campaign performance and ROI metrics.</p>
        </div>
        <div className="card">
          <h3>Collaborations</h3>
          <p style={{ color: 'var(--color-text-secondary)' }}>Future functionality for direct creator partnerships.</p>
        </div>
      </div>
    </div>
  );
}
