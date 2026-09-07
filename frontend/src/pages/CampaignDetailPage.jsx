import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function CampaignDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [campaign, setCampaign] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const [hasApplied, setHasApplied] = useState(false);
  const [applicationStatus, setApplicationStatus] = useState(null);
  const [applying, setApplying] = useState(false);
  const [applicationMessage, setApplicationMessage] = useState('');
  
  const [isCreator, setIsCreator] = useState(false);
  const [creatorCheckDone, setCreatorCheckDone] = useState(false);

  useEffect(() => {
    const fetchCampaign = async () => {
      try {
        const res = await api.get(`/campaigns/${id}`);
        setCampaign(res);
      } catch (err) {
        setError('Campaign not found or you do not have permission.');
      } finally {
        setLoading(false);
      }
    };
    fetchCampaign();
  }, [id]);

  useEffect(() => {
    if (user && campaign) {
      // Check if user is a creator
      api.get(`/creators/profile/${user.id}`)
        .then(() => setIsCreator(true))
        .catch(() => setIsCreator(false))
        .finally(() => setCreatorCheckDone(true));

      // Check existing applications
      if (campaign.brandUserId !== user.id) {
        api.get(`/campaigns/applications/mine`)
          .then(res => {
            const app = res.content.find(a => a.campaignId === parseInt(id));
            if (app) {
              setHasApplied(true);
              setApplicationStatus(app.status);
            }
          }).catch(() => {});
      }
    } else if (campaign) {
       setCreatorCheckDone(true); // Anonymous viewer
    }
  }, [user, campaign, id]);

  const handleApply = async (e) => {
    e.preventDefault();
    if (!user) {
      navigate('/login');
      return;
    }
    try {
      setApplying(true);
      await api.post(`/campaigns/${id}/applications`, { message: applicationMessage });
      setHasApplied(true);
      setApplicationStatus('PENDING');
    } catch (err) {
      alert(err.data?.message || 'Failed to apply. You may not have a Creator Profile, or the deadline passed.');
    } finally {
      setApplying(false);
    }
  };

  if (loading) return <div style={{ padding: '2rem', textAlign: 'center' }}>Loading campaign...</div>;
  if (error || !campaign) return <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--error-color)' }}>{error}</div>;

  const isOwner = user && user.id === campaign.brandUserId;
  const isExpired = campaign.applicationDeadline && new Date(campaign.applicationDeadline) < new Date();
  const isOpen = campaign.status === 'OPEN';

  return (
    <div className="page-container" style={{ padding: '2rem', maxWidth: '800px', margin: '0 auto' }}>
      <Link to="/campaigns" style={{ color: 'var(--text-secondary)', textDecoration: 'none', marginBottom: '1rem', display: 'inline-block' }}>
        &larr; Back to Campaigns
      </Link>
      
      <div style={{ background: 'var(--bg-secondary)', padding: '2rem', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: '2rem' }}>
          <img src={campaign.brandLogoUrl || `https://ui-avatars.com/api/?name=${campaign.brandName}&background=random`} alt={campaign.brandName} style={{ width: '64px', height: '64px', borderRadius: '50%', marginRight: '1rem' }} />
          <div>
            <h1 style={{ margin: '0 0 0.5rem 0' }}>{campaign.title}</h1>
            <div style={{ color: 'var(--text-secondary)' }}>by <Link to={`/user/${campaign.brandUserId}`} style={{ color: 'inherit' }}>{campaign.brandName}</Link></div>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '2rem', padding: '1rem', background: 'rgba(0,0,0,0.05)', borderRadius: '8px' }}>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Budget</div>
            <div style={{ fontWeight: 'bold', fontSize: '1.2rem', color: 'var(--accent-primary)' }}>${campaign.budget?.toLocaleString() || 'Negotiable'}</div>
          </div>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Deadline</div>
            <div style={{ fontWeight: 'bold' }}>{campaign.applicationDeadline ? new Date(campaign.applicationDeadline).toLocaleDateString() : 'None'}</div>
          </div>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Niche</div>
            <div style={{ fontWeight: 'bold' }}>{campaign.niche}</div>
          </div>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Status</div>
            <div style={{ fontWeight: 'bold', color: campaign.status === 'OPEN' ? '#4CAF50' : 'var(--text-secondary)' }}>{campaign.status}</div>
          </div>
        </div>

        <div style={{ marginBottom: '2rem' }}>
          <h3 style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem' }}>Description</h3>
          <p style={{ whiteSpace: 'pre-wrap', lineHeight: '1.6', color: 'var(--text-primary)' }}>{campaign.description}</p>
        </div>

        {isOwner ? (
          <div style={{ padding: '1rem', background: 'rgba(255,255,255,0.05)', border: '1px dashed var(--accent-primary)', borderRadius: '8px', textAlign: 'center' }}>
            <p>You are the owner of this campaign.</p>
            <Link to="/dashboard/brand" className="btn primary">Manage Applications in Dashboard</Link>
          </div>
        ) : (
          <div style={{ marginTop: '2rem' }}>
            {hasApplied ? (
              <div style={{ padding: '1rem', background: 'rgba(76, 175, 80, 0.1)', border: '1px solid #4CAF50', borderRadius: '8px', textAlign: 'center' }}>
                <h3 style={{ margin: '0 0 0.5rem 0', color: '#4CAF50' }}>Application {applicationStatus}</h3>
                <p style={{ margin: 0 }}>You have already applied to this campaign.</p>
              </div>
            ) : isExpired ? (
              <div style={{ padding: '1rem', background: 'rgba(255, 68, 68, 0.1)', border: '1px solid #ff4444', borderRadius: '8px', textAlign: 'center', color: '#ff4444' }}>
                This campaign is no longer accepting applications.
              </div>
            ) : !isOpen ? (
              <div style={{ padding: '1rem', background: 'rgba(255, 255, 255, 0.05)', border: '1px solid var(--border-color)', borderRadius: '8px', textAlign: 'center', color: 'var(--text-secondary)' }}>
                This campaign is currently {campaign.status}.
              </div>
            ) : user && creatorCheckDone && !isCreator ? (
               <div style={{ padding: '1rem', background: 'rgba(255, 255, 255, 0.05)', border: '1px solid var(--border-color)', borderRadius: '8px', textAlign: 'center', color: 'var(--text-secondary)' }}>
                You must activate your Creator Profile to apply for campaigns.
                <br/><br/>
                <Link to={`/user/${user.id}`} className="btn">Go to Profile</Link>
              </div>
            ) : (
              <form onSubmit={handleApply} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <h3 style={{ margin: 0 }}>Submit Application</h3>
                <textarea 
                  required
                  rows={4}
                  placeholder="Why are you a good fit for this campaign?" 
                  value={applicationMessage}
                  onChange={(e) => setApplicationMessage(e.target.value)}
                  style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--border-color)', background: 'var(--bg-primary)', color: 'var(--text-primary)', fontFamily: 'inherit' }}
                />
                <button type="submit" className="btn primary" disabled={applying || !applicationMessage.trim()} style={{ alignSelf: 'flex-start', padding: '0.75rem 2rem' }}>
                  {applying ? 'Submitting...' : 'Apply Now'}
                </button>
              </form>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
