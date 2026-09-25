import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { collaborationApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import DeliverablesSection from '../components/DeliverablesSection';
import MessagingSection from '../components/collaboration/MessagingSection';

export default function CollaborationDetailPage() {
  const { id } = useParams();
  const { user } = useAuth();
  
  const [collaboration, setCollaboration] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchCollaboration();
  }, [id]);

  const fetchCollaboration = async () => {
    try {
      setLoading(true);
      const res = await collaborationApi.getCollaboration(id);
      setCollaboration(res);
    } catch (err) {
      setError(err.message || 'Failed to load collaboration details');
    } finally {
      setLoading(false);
    }
  };

  const handleStatusUpdate = async (newStatus) => {
    if (window.confirm(`Are you sure you want to mark this collaboration as ${newStatus}? This action cannot be undone.`)) {
      try {
        await collaborationApi.updateStatus(id, newStatus);
        fetchCollaboration();
      } catch (err) {
        alert(err.message || 'Failed to update status');
      }
    }
  };

  if (loading) return <div className="container" style={{ marginTop: '2rem' }}>Loading collaboration...</div>;
  if (error) return <div className="container" style={{ marginTop: '2rem', color: '#ff4444' }}>{error}</div>;
  if (!collaboration) return <div className="container" style={{ marginTop: '2rem' }}>Collaboration not found.</div>;

  const isBrandOwner = user.id === collaboration.brandUserId;
  const isCreator = user.id === collaboration.creatorUserId;

  return (
    <div className="container" style={{ marginTop: '2rem', marginBottom: '4rem' }}>
      <div className="card" style={{ marginBottom: '2rem', padding: 'clamp(1rem, 4vw, 1.5rem)', background: 'var(--bg-primary)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
          <div style={{ minWidth: 0, flex: '1 1 100%' }}>
            <h1 style={{ margin: 0, fontSize: '1.5rem', overflowWrap: 'break-word', wordWrap: 'break-word' }}>Collaboration #{collaboration.id}</h1>
            <div style={{ display: 'flex', alignItems: 'center', marginTop: '0.75rem', gap: '1rem', flexWrap: 'wrap' }}>
              <span style={{ 
                padding: '0.25rem 0.75rem', 
                borderRadius: '20px', 
                fontSize: '0.875rem', 
                fontWeight: 'bold',
                background: collaboration.status === 'ACTIVE' ? '#e8f5e9' : collaboration.status === 'COMPLETED' ? '#e3f2fd' : '#ffebee',
                color: collaboration.status === 'ACTIVE' ? '#2e7d32' : collaboration.status === 'COMPLETED' ? '#1565c0' : '#c62828'
              }}>
                {collaboration.status}
              </span>
              <span style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
                Created: {new Date(collaboration.createdAt).toLocaleDateString()}
              </span>
            </div>
          </div>
          
          {collaboration.status === 'ACTIVE' && (
            <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
              {isBrandOwner && (
                <button 
                  className="btn primary" 
                  style={{ padding: '0.5rem 1rem' }}
                  onClick={() => handleStatusUpdate('COMPLETED')}
                >
                  Mark Completed
                </button>
              )}
              {(isCreator || isBrandOwner) && (
                <button 
                  className="btn text-danger" 
                  style={{ color: '#c62828', borderColor: '#c62828', background: 'transparent', padding: '0.5rem 1rem' }}
                  onClick={() => handleStatusUpdate('CANCELLED')}
                >
                  Cancel Collaboration
                </button>
              )}
            </div>
          )}
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 350px), 1fr))', gap: '2rem' }}>
        {/* Campaign Terms */}
        <div className="card" style={{ padding: 'clamp(1rem, 4vw, 2rem)', display: 'flex', flexDirection: 'column' }}>
          <h3 style={{ marginTop: 0, marginBottom: '1.5rem', borderBottom: '1px solid var(--color-border)', paddingBottom: '0.5rem' }}>Campaign Details</h3>
          <h4 style={{ margin: '0 0 0.5rem 0', overflowWrap: 'break-word', wordWrap: 'break-word' }}>
            <Link to={`/campaigns/${collaboration.campaignId}`} style={{ color: 'inherit' }}>
              {collaboration.campaignTitle}
            </Link>
          </h4>
          <p style={{ color: 'var(--text-secondary)', whiteSpace: 'pre-wrap', marginBottom: '1.5rem', fontSize: '0.95rem', overflowWrap: 'break-word', wordWrap: 'break-word', flexGrow: 1 }}>
            {collaboration.campaignDescription}
          </p>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(120px, 1fr))', gap: '1rem', marginTop: 'auto' }}>
            <div style={{ background: 'var(--color-bg)', padding: '1rem', borderRadius: '8px', border: '1px solid var(--color-border)' }}>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '1px', marginBottom: '0.25rem' }}>Budget</div>
              <div style={{ fontWeight: 'bold', fontSize: '1.1rem', overflowWrap: 'break-word' }}>${collaboration.campaignBudget}</div>
            </div>
            <div style={{ background: 'var(--color-bg)', padding: '1rem', borderRadius: '8px', border: '1px solid var(--color-border)' }}>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '1px', marginBottom: '0.25rem' }}>Niche</div>
              <div style={{ fontWeight: 'bold', fontSize: '1.1rem', overflowWrap: 'break-word' }}>{collaboration.campaignNiche}</div>
            </div>
          </div>
        </div>

        {/* Participants */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div className="card" style={{ padding: 'clamp(1rem, 4vw, 2rem)' }}>
            <h3 style={{ marginTop: 0, marginBottom: '1.5rem', borderBottom: '1px solid var(--color-border)', paddingBottom: '0.5rem' }}>Brand Partner</h3>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <img 
                src={collaboration.brandLogoUrl || `https://ui-avatars.com/api/?name=${collaboration.brandName}&background=random`} 
                alt={collaboration.brandName} 
                style={{ width: '56px', height: '56px', borderRadius: '8px', objectFit: 'cover', border: '1px solid var(--color-border)' }}
              />
              <div style={{ minWidth: 0, flex: 1 }}>
                <div style={{ fontWeight: 'bold', fontSize: '1.1rem', overflowWrap: 'break-word', wordWrap: 'break-word' }}>
                  <Link to={`/user/${collaboration.brandUserId}`} style={{ color: 'inherit', textDecoration: 'none' }}>
                    {collaboration.brandName}
                  </Link>
                </div>
              </div>
            </div>
          </div>

          <div className="card" style={{ padding: 'clamp(1rem, 4vw, 2rem)' }}>
            <h3 style={{ marginTop: 0, marginBottom: '1.5rem', borderBottom: '1px solid var(--color-border)', paddingBottom: '0.5rem' }}>Creator Partner</h3>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <img 
                src={collaboration.creatorAvatarUrl || `https://ui-avatars.com/api/?name=${collaboration.creatorName}&background=random`} 
                alt={collaboration.creatorName} 
                style={{ width: '56px', height: '56px', borderRadius: '50%', objectFit: 'cover', border: '1px solid var(--color-border)' }}
              />
              <div style={{ minWidth: 0, flex: 1 }}>
                <div style={{ fontWeight: 'bold', fontSize: '1.1rem', overflowWrap: 'break-word', wordWrap: 'break-word' }}>
                  <Link to={`/user/${collaboration.creatorUserId}`} style={{ color: 'inherit', textDecoration: 'none' }}>
                    {collaboration.creatorName}
                  </Link>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div style={{ marginTop: '2rem' }}>
        <MessagingSection 
          collaborationId={collaboration.id}
          collaborationStatus={collaboration.status}
          otherParticipantName={isBrandOwner ? collaboration.creatorName : collaboration.brandName}
        />
      </div>

      <DeliverablesSection 
        collaborationId={collaboration.id}
        collaborationStatus={collaboration.status}
        currentUserRole={isBrandOwner ? 'BRAND' : isCreator ? 'CREATOR' : null}
      />
    </div>
  );
}
