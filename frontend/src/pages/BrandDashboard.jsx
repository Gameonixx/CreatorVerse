import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import { api } from '../services/api';

export default function BrandDashboard() {
  const { user } = useAuth();
  const [hasProfile, setHasProfile] = useState(null);

  useEffect(() => {
    if (user) {
      api.get(`/brands/profile/${user.id}`)
        .then(() => setHasProfile(true))
        .catch(() => setHasProfile(false));
    }
  }, [user]);

  if (hasProfile === null) return <div>Loading dashboard...</div>;

  if (hasProfile === false) {
    return (
      <div className="dashboard-container" style={{ padding: '2rem', textAlign: 'center' }}>
        <h2>Brand Dashboard Restricted</h2>
        <p>You need to activate a Brand Profile to access this workspace.</p>
        <Link to={`/user/${user.id}`} className="btn primary" style={{ marginTop: '1rem', display: 'inline-block' }}>Go to Profile Settings</Link>
      </div>
    );
  }

  // Basic shell for Brand Dashboard
  return (
    <div className="dashboard-container" style={{ padding: '2rem' }}>
      <h1>Brand Dashboard</h1>
      <p>Welcome to your professional brand workspace, {user?.displayName || user?.username}.</p>
      
      <div className="dashboard-grid" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginTop: '2rem' }}>
        <div className="dashboard-card" style={{ padding: '1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}>
          <h3>Creator Discovery</h3>
          <p style={{ color: 'var(--text-secondary)' }}>Coming soon in Phase 5.2...</p>
        </div>
        
        <div className="dashboard-card" style={{ padding: '1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}>
          <h3>Campaigns</h3>
          <p style={{ color: 'var(--text-secondary)' }}>Coming soon in Phase 5.3...</p>
        </div>

        <div className="dashboard-card" style={{ padding: '1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}>
          <h3>Collaborations</h3>
          <p style={{ color: 'var(--text-secondary)' }}>Coming soon in Phase 5.4...</p>
        </div>

        <div className="dashboard-card" style={{ padding: '1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}>
          <h3>Analytics</h3>
          <p style={{ color: 'var(--text-secondary)' }}>Coming soon in Phase 5.5...</p>
        </div>
      </div>
    </div>
  );
}
