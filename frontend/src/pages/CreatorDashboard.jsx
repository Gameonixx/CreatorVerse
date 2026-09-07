import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import { api } from '../services/api';

export default function CreatorDashboard() {
  const { user } = useAuth();
  const [hasProfile, setHasProfile] = useState(null);
  
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (user) {
      api.get(`/creators/profile/${user.id}`)
        .then(() => {
          setHasProfile(true);
          fetchApplications();
        })
        .catch(() => setHasProfile(false));
    }
  }, [user]);

  const fetchApplications = async () => {
    try {
      setLoading(true);
      const res = await api.get('/campaigns/applications/mine');
      setApplications(res.content);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleWithdraw = async (appId) => {
    if (window.confirm("Are you sure you want to withdraw this application?")) {
      try {
        await api.delete(`/campaigns/applications/${appId}`);
        fetchApplications();
      } catch (err) {
        alert(err.data?.message || 'Failed to withdraw');
      }
    }
  };

  if (hasProfile === null) return <div>Loading dashboard...</div>;

  if (hasProfile === false) {
    return (
      <div className="dashboard-container" style={{ padding: '2rem', textAlign: 'center' }}>
        <h2>Creator Dashboard Restricted</h2>
        <p>You need to activate a Creator Profile to access this workspace.</p>
        <Link to={`/user/${user.id}`} className="btn primary" style={{ marginTop: '1rem', display: 'inline-block' }}>Go to Profile Settings</Link>
      </div>
    );
  }

  return (
    <div className="dashboard-container" style={{ padding: '2rem', maxWidth: '1000px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ margin: 0 }}>Creator Dashboard</h1>
          <p style={{ color: 'var(--text-secondary)', margin: '0.5rem 0 0 0' }}>Welcome to your workspace, {user?.displayName || user?.username}.</p>
        </div>
        <Link to="/campaigns" className="btn primary">Discover Campaigns</Link>
      </div>

      <div>
        <h3>My Applications</h3>
        {loading ? <p>Loading...</p> : applications.length === 0 ? <p style={{ color: 'var(--text-secondary)' }}>You haven't applied to any campaigns yet.</p> : (
          <div style={{ display: 'grid', gap: '1rem' }}>
            {applications.map(app => (
              <div key={app.id} style={{ padding: '1.5rem', background: 'var(--bg-secondary)', border: '1px solid var(--border-color)', borderRadius: '12px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', marginBottom: '0.5rem' }}>
                    <span style={{ fontWeight: 'bold', marginRight: '1rem', color: app.status === 'PENDING' ? '#FFC107' : app.status === 'ACCEPTED' ? '#4CAF50' : app.status === 'REJECTED' ? '#ff4444' : 'var(--text-secondary)' }}>
                      {app.status}
                    </span>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Applied: {new Date(app.createdAt).toLocaleDateString()}</span>
                  </div>
                  <h4 style={{ margin: '0 0 0.5rem 0' }}>
                    <Link to={`/campaigns/${app.campaignId}`} style={{ color: 'inherit' }}>Campaign #{app.campaignId}</Link>
                  </h4>
                  <p style={{ margin: 0, fontSize: '0.9rem', color: 'var(--text-secondary)', maxWidth: '500px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    "{app.message}"
                  </p>
                </div>
                <div>
                  {app.status === 'PENDING' && (
                    <button className="btn" style={{ border: '1px solid #ff4444', color: '#ff4444', background: 'transparent' }} onClick={() => handleWithdraw(app.id)}>Withdraw</button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Roadmap Cleanup */}
      <div className="dashboard-grid" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginTop: '3rem', paddingTop: '2rem', borderTop: '1px solid var(--border-color)' }}>
        <div className="dashboard-card" style={{ padding: '1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}>
          <h3>Analytics</h3>
          <p style={{ color: 'var(--text-secondary)' }}>Active (Phase 5.3)</p>
        </div>
        <div className="dashboard-card" style={{ padding: '1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}>
          <h3>Campaigns</h3>
          <p style={{ color: 'var(--text-secondary)' }}>Active (Phase 5.4)</p>
        </div>
        <div className="dashboard-card" style={{ padding: '1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}>
          <h3>Collaborations</h3>
          <p style={{ color: 'var(--text-secondary)' }}>Coming soon in Phase 5.5...</p>
        </div>
      </div>
    </div>
  );
}
