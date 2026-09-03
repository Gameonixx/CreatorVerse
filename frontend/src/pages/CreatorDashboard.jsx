import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Navigate, Link } from 'react-router-dom';
import { api } from '../services/api';

export default function CreatorDashboard() {
  const { user } = useAuth();
  const [hasProfile, setHasProfile] = useState(null);

  useEffect(() => {
    if (user) {
      api.get(`/creators/profile/${user.id}`)
        .then(() => setHasProfile(true))
        .catch(() => setHasProfile(false));
    }
  }, [user]);

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

  // Basic shell for Creator Dashboard
  return (
    <div className="dashboard-container" style={{ padding: '2rem' }}>
      <h1>Creator Dashboard</h1>
      <p>Welcome to your professional creator workspace, {user?.displayName || user?.username}.</p>
      
      <div className="dashboard-grid" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginTop: '2rem' }}>
        <div className="dashboard-card" style={{ padding: '1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}>
          <h3>Analytics</h3>
          <p style={{ color: 'var(--text-secondary)' }}>Coming soon in Phase 5.5...</p>
        </div>
        
        <div className="dashboard-card" style={{ padding: '1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}>
          <h3>Campaigns</h3>
          <p style={{ color: 'var(--text-secondary)' }}>Coming soon in Phase 5.3...</p>
        </div>

        <div className="dashboard-card" style={{ padding: '1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}>
          <h3>Collaborations</h3>
          <p style={{ color: 'var(--text-secondary)' }}>Coming soon in Phase 5.4...</p>
        </div>
      </div>
    </div>
  );
}
