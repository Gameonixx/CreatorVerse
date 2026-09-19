import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import { api, collaborationApi } from '../services/api';

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

  const [collaborations, setCollaborations] = useState([]);
  const [loadingCollabs, setLoadingCollabs] = useState(false);

  useEffect(() => {
    if (user) {
      api.get(`/brands/profile/${user.id}`)
        .then(() => {
          setHasProfile(true);
          fetchCampaigns();
          fetchCollaborations();
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

  const fetchCollaborations = async () => {
    try {
      setLoadingCollabs(true);
      const res = await collaborationApi.getMyCollaborations();
      // Filter to only show those where the user is the brand
      setCollaborations(res.content.filter(c => c.brandUserId === user.id));
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingCollabs(false);
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
      // Refresh apps and collabs
      loadApplications(selectedCampaign);
      fetchCollaborations();
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
        <div className="card" style={{ marginBottom: '2rem' }}>
          <h3 style={{ marginTop: 0, marginBottom: '1.5rem', borderBottom: '1px solid var(--color-border)', paddingBottom: '0.5rem' }}>Create New Campaign</h3>
          <form onSubmit={handleCreate} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <label style={{ fontWeight: 'bold', fontSize: '0.9rem' }}>Campaign Title</label>
              <input required placeholder="Enter a compelling title" value={newCampaign.title} onChange={e => setNewCampaign({...newCampaign, title: e.target.value})} className="form-input" style={{ width: '100%', boxSizing: 'border-box' }}/>
            </div>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <label style={{ fontWeight: 'bold', fontSize: '0.9rem' }}>Description</label>
              <textarea required rows={4} placeholder="Describe your campaign goals, requirements, and expectations..." value={newCampaign.description} onChange={e => setNewCampaign({...newCampaign, description: e.target.value})} className="form-input" style={{ width: '100%', boxSizing: 'border-box', resize: 'vertical' }}/>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 200px), 1fr))', gap: '1rem' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                <label style={{ fontWeight: 'bold', fontSize: '0.9rem' }}>Niche</label>
                <input required placeholder="e.g., Tech, Fashion, Food" value={newCampaign.niche} onChange={e => setNewCampaign({...newCampaign, niche: e.target.value})} className="form-input" style={{ width: '100%', boxSizing: 'border-box' }}/>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                <label style={{ fontWeight: 'bold', fontSize: '0.9rem' }}>Budget ($)</label>
                <input type="number" required placeholder="e.g., 500" value={newCampaign.budget} onChange={e => setNewCampaign({...newCampaign, budget: e.target.value})} className="form-input" style={{ width: '100%', boxSizing: 'border-box' }}/>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                <label style={{ fontWeight: 'bold', fontSize: '0.9rem' }}>Application Deadline</label>
                <input type="datetime-local" value={newCampaign.applicationDeadline} onChange={e => setNewCampaign({...newCampaign, applicationDeadline: e.target.value})} className="form-input" style={{ width: '100%', boxSizing: 'border-box' }}/>
              </div>
            </div>

            <button type="submit" className="btn primary" disabled={creating} style={{ alignSelf: 'flex-start', padding: '0.75rem 2rem', marginTop: '0.5rem' }}>
              {creating ? 'Saving...' : 'Save as Draft'}
            </button>
          </form>
        </div>
      )}

      {selectedCampaign ? (
        <div className="card" style={{ marginBottom: '2rem' }}>
          <button className="btn" onClick={() => setSelectedCampaign(null)} style={{ marginBottom: '1.5rem' }}>&larr; Back to Campaigns</button>
          <h3 style={{ marginTop: 0, marginBottom: '1.5rem', borderBottom: '1px solid var(--color-border)', paddingBottom: '0.5rem' }}>Applications for: {selectedCampaign.title}</h3>
          
          {loadingApps ? <p>Loading applications...</p> : applications.length === 0 ? <p style={{ color: 'var(--text-secondary)' }}>No applications yet.</p> : (
            <div style={{ display: 'grid', gap: '1.5rem' }}>
              {applications.map(app => (
                <div key={app.id} className="card" style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', gap: '1rem', background: 'var(--color-surface)' }}>
                  <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', minWidth: 0, flex: 1 }}>
                      <img src={app.creatorAvatarUrl || `https://ui-avatars.com/api/?name=${app.creatorName}&background=random`} alt={app.creatorName} style={{ width: '56px', height: '56px', borderRadius: '50%', objectFit: 'cover', flexShrink: 0, border: '1px solid var(--color-border)' }} />
                      <div style={{ minWidth: 0 }}>
                        <div style={{ fontWeight: 'bold', fontSize: '1.1rem', overflowWrap: 'break-word', wordWrap: 'break-word' }}>
                          <Link to={`/user/${app.creatorUserId}`} style={{ color: 'inherit', textDecoration: 'none' }}>{app.creatorName}</Link>
                        </div>
                        <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '0.25rem', overflowWrap: 'break-word' }}>
                          Followers: {app.creatorFollowerCount?.toLocaleString() || 0} &nbsp;&bull;&nbsp; Engagement: {app.creatorEngagementRate ? app.creatorEngagementRate + '%' : 'N/A'}
                        </div>
                      </div>
                    </div>
                    <div style={{ 
                      fontWeight: 'bold', 
                      fontSize: '0.85rem',
                      padding: '0.25rem 0.75rem',
                      borderRadius: '20px',
                      background: app.status === 'PENDING' ? '#fff8e1' : app.status === 'ACCEPTED' ? '#e8f5e9' : '#ffebee',
                      color: app.status === 'PENDING' ? '#f57c00' : app.status === 'ACCEPTED' ? '#2e7d32' : '#c62828',
                      border: `1px solid ${app.status === 'PENDING' ? '#ffe082' : app.status === 'ACCEPTED' ? '#a5d6a7' : '#ef9a9a'}`,
                      whiteSpace: 'nowrap'
                    }}>
                      {app.status}
                    </div>
                  </div>
                  
                  <p style={{ margin: '0', whiteSpace: 'pre-wrap', fontSize: '0.95rem', overflowWrap: 'break-word', wordWrap: 'break-word' }}>{app.message}</p>
                  
                  <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', marginTop: '0.5rem' }}>
                    <Link to={`/user/${app.creatorUserId}`} className="btn" style={{ padding: '0.5rem 1rem', flex: '1 1 auto', textAlign: 'center' }}>View Profile</Link>
                    {app.status === 'PENDING' && (
                      <>
                        <button className="btn" style={{ background: '#4CAF50', color: 'white', border: '1px solid #388E3C', padding: '0.5rem 1rem', flex: '1 1 auto' }} onClick={() => handleReview(app.id, 'ACCEPTED')}>Accept</button>
                        <button className="btn" style={{ background: '#ff4444', color: 'white', border: '1px solid #d32f2f', padding: '0.5rem 1rem', flex: '1 1 auto' }} onClick={() => handleReview(app.id, 'REJECTED')}>Reject</button>
                      </>
                    )}
                    {app.status === 'ACCEPTED' && (
                      <button className="btn primary" style={{ padding: '0.5rem 1rem', flex: '1 1 auto' }}>Message</button>
                    )}
                  </div>
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

      <div style={{ marginTop: '3rem' }}>
        <h3>Active Collaborations</h3>
        {loadingCollabs ? <p>Loading collaborations...</p> : collaborations.length === 0 ? <p style={{ color: 'var(--text-secondary)' }}>You don't have any active collaborations yet.</p> : (
          <div className="dashboard-list">
            {collaborations.map(collab => (
              <div key={collab.id} className="dashboard-list-item">
                <div className="dashboard-list-item-content">
                  <div style={{ display: 'flex', alignItems: 'center', marginBottom: '0.5rem' }}>
                    <span style={{ fontWeight: 'bold', marginRight: '1rem', color: collab.status === 'ACTIVE' ? '#4CAF50' : collab.status === 'CANCELLED' ? '#ff4444' : 'var(--text-secondary)' }}>
                      {collab.status}
                    </span>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Created: {new Date(collab.createdAt).toLocaleDateString()}</span>
                  </div>
                  <h4 className="dashboard-list-item-title">
                    <Link to={`/collaborations/${collab.id}`} style={{ color: 'inherit' }}>{collab.campaignTitle}</Link>
                  </h4>
                  <p className="dashboard-list-item-desc" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <img src={collab.creatorAvatarUrl || 'https://via.placeholder.com/24'} alt={collab.creatorName} style={{ width: '24px', height: '24px', borderRadius: '50%', objectFit: 'cover' }} />
                    Creator: <strong>{collab.creatorName}</strong>
                  </p>
                </div>
                <div className="dashboard-list-item-actions">
                  <Link to={`/collaborations/${collab.id}`} className="btn primary">View Details</Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

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
      </div>
    </div>
  );
}
