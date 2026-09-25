import React, { useState, useEffect } from 'react';
import { collaborationApi } from '../services/api';

export default function DeliverablesSection({ collaborationId, collaborationStatus, currentUserRole }) {
  const [deliverables, setDeliverables] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Brand Form State
  const [showAddForm, setShowAddForm] = useState(false);
  const [editId, setEditId] = useState(null);
  const [formData, setFormData] = useState({ title: '', description: '' });

  // Creator Submit State
  const [submitId, setSubmitId] = useState(null);
  const [submissionUrl, setSubmissionUrl] = useState('');

  // Brand Review State
  const [reviewId, setReviewId] = useState(null);
  const [reviewData, setReviewData] = useState({ status: 'APPROVED', feedback: '' });

  const isBrand = currentUserRole === 'BRAND';
  const isCreator = currentUserRole === 'CREATOR';
  const isActive = collaborationStatus === 'ACTIVE';

  useEffect(() => {
    fetchDeliverables();
  }, [collaborationId]);

  const fetchDeliverables = async () => {
    try {
      setLoading(true);
      const res = await collaborationApi.getDeliverables(collaborationId);
      setDeliverables(res);
      setError(null);
    } catch (err) {
      setError(err.message || 'Failed to load deliverables');
    } finally {
      setLoading(false);
    }
  };

  const handleSaveDeliverable = async (e) => {
    e.preventDefault();
    try {
      if (editId) {
        await collaborationApi.updateDeliverable(collaborationId, editId, formData);
      } else {
        await collaborationApi.createDeliverable(collaborationId, formData);
      }
      setFormData({ title: '', description: '' });
      setShowAddForm(false);
      setEditId(null);
      fetchDeliverables();
    } catch (err) {
      alert(err.message || 'Failed to save deliverable');
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this deliverable?')) {
      try {
        await collaborationApi.deleteDeliverable(collaborationId, id);
        fetchDeliverables();
      } catch (err) {
        alert(err.message || 'Failed to delete deliverable');
      }
    }
  };

  const handleSubmitWork = async (e) => {
    e.preventDefault();
    try {
      await collaborationApi.submitDeliverable(collaborationId, submitId, submissionUrl);
      setSubmitId(null);
      setSubmissionUrl('');
      fetchDeliverables();
    } catch (err) {
      alert(err.message || 'Failed to submit work');
    }
  };

  const handleReview = async (e) => {
    e.preventDefault();
    try {
      await collaborationApi.reviewDeliverable(collaborationId, reviewId, reviewData.status, reviewData.feedback);
      setReviewId(null);
      setReviewData({ status: 'APPROVED', feedback: '' });
      fetchDeliverables();
    } catch (err) {
      alert(err.message || 'Failed to review deliverable');
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING': return { bg: '#fff8e1', color: '#f57c00', border: '#ffe082' };
      case 'SUBMITTED': return { bg: '#e3f2fd', color: '#1976d2', border: '#90caf9' };
      case 'APPROVED': return { bg: '#e8f5e9', color: '#2e7d32', border: '#a5d6a7' };
      case 'REJECTED': return { bg: '#ffebee', color: '#c62828', border: '#ef9a9a' };
      default: return { bg: '#f5f5f5', color: '#616161', border: '#e0e0e0' };
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column' }}>
      {isBrand && isActive && !showAddForm && (
        <div style={{ marginBottom: '1.5rem' }}>
          <button className="btn primary" onClick={() => { setShowAddForm(true); setEditId(null); setFormData({ title: '', description: '' }); }}>
            + Add Deliverable
          </button>
        </div>
      )}

      {!isActive && deliverables.length > 0 && (
        <div style={{ marginBottom: '1.5rem', padding: '1rem', background: '#f5f5f5', borderRadius: '4px', fontSize: '0.9rem', color: '#616161' }}>
          This collaboration is {collaborationStatus.toLowerCase()}. Deliverables are read-only.
        </div>
      )}

      {error && <div style={{ color: '#c62828', marginBottom: '1rem' }}>{error}</div>}
      
      {showAddForm && isBrand && isActive && (
        <div style={{ background: 'var(--color-bg)', padding: '1.5rem', borderRadius: '8px', border: '1px solid var(--color-border)', marginBottom: '1.5rem' }}>
          <h4 style={{ margin: '0 0 1rem 0' }}>{editId ? 'Edit Deliverable' : 'New Deliverable'}</h4>
          <form onSubmit={handleSaveDeliverable} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <label style={{ fontWeight: 'bold', fontSize: '0.9rem' }}>Title</label>
              <input 
                required 
                className="form-input" 
                value={formData.title} 
                onChange={e => setFormData({...formData, title: e.target.value})} 
                placeholder="e.g. 1x TikTok Video"
              />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <label style={{ fontWeight: 'bold', fontSize: '0.9rem' }}>Description (Optional)</label>
              <textarea 
                className="form-input" 
                rows={3} 
                value={formData.description} 
                onChange={e => setFormData({...formData, description: e.target.value})}
                placeholder="Specific requirements, tags, or concepts to include."
              />
            </div>
            <div style={{ display: 'flex', gap: '1rem', marginTop: '0.5rem' }}>
              <button type="submit" className="btn primary">Save</button>
              <button type="button" className="btn" onClick={() => { setShowAddForm(false); setEditId(null); }}>Cancel</button>
            </div>
          </form>
        </div>
      )}

      {loading ? (
        <p>Loading deliverables...</p>
      ) : deliverables.length === 0 && !showAddForm ? (
        <p style={{ color: 'var(--text-secondary)' }}>No deliverables have been defined yet.</p>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {deliverables.map(d => {
            const colors = getStatusColor(d.status);
            return (
              <div key={d.id} style={{ border: '1px solid var(--color-border)', borderRadius: '8px', padding: 'clamp(1rem, 3vw, 1.5rem)', background: 'var(--color-surface)' }}>
                
                {/* Header */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem', marginBottom: '1rem' }}>
                  <div style={{ minWidth: 0, flex: '1 1 150px' }}>
                    <h4 style={{ margin: '0 0 0.5rem 0', overflowWrap: 'break-word', wordWrap: 'break-word' }}>{d.title}</h4>
                    {d.description && <p style={{ margin: 0, color: 'var(--text-secondary)', fontSize: '0.95rem', whiteSpace: 'pre-wrap', overflowWrap: 'break-word' }}>{d.description}</p>}
                  </div>
                  <div style={{ 
                    fontWeight: 'bold', fontSize: '0.85rem', padding: '0.35rem 0.75rem', borderRadius: '20px',
                    background: colors.bg, color: colors.color, border: `1px solid ${colors.border}`,
                    whiteSpace: 'nowrap'
                  }}>
                    {d.status}
                  </div>
                </div>

                {/* Submission Info */}
                {d.submissionUrl && (
                  <div style={{ marginBottom: '1rem', padding: '1rem', background: 'var(--color-bg)', borderRadius: '4px', border: '1px solid var(--color-border)' }}>
                    <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.25rem', fontWeight: 'bold' }}>Submission Link</div>
                    <a href={d.submissionUrl} target="_blank" rel="noopener noreferrer" style={{ overflowWrap: 'anywhere', wordBreak: 'break-word' }}>
                      {d.submissionUrl}
                    </a>
                  </div>
                )}

                {/* Feedback */}
                {d.feedback && (
                  <div style={{ marginBottom: '1rem', padding: '1rem', background: '#fff3e0', borderLeft: '4px solid #ff9800', borderRadius: '4px' }}>
                    <div style={{ fontSize: '0.85rem', color: '#e65100', marginBottom: '0.25rem', fontWeight: 'bold' }}>Review Feedback</div>
                    <p style={{ margin: 0, whiteSpace: 'pre-wrap', fontSize: '0.95rem', overflowWrap: 'break-word' }}>{d.feedback}</p>
                  </div>
                )}

                {/* Controls */}
                {isActive && (
                  <div style={{ marginTop: '1rem', display: 'flex', gap: '0.75rem', flexWrap: 'wrap', borderTop: '1px solid var(--color-border)', paddingTop: '1rem' }}>
                    
                    {/* Brand Controls */}
                    {isBrand && (
                      <>
                        {(d.status === 'PENDING' || d.status === 'REJECTED') && (
                          <button className="btn" onClick={() => {
                            setEditId(d.id);
                            setFormData({ title: d.title, description: d.description || '' });
                            setShowAddForm(true);
                          }}>
                            Edit
                          </button>
                        )}
                        {d.status === 'PENDING' && (
                          <button className="btn text-danger" style={{ color: '#c62828' }} onClick={() => handleDelete(d.id)}>
                            Delete
                          </button>
                        )}
                        {d.status === 'SUBMITTED' && reviewId !== d.id && (
                          <button className="btn primary" onClick={() => { setReviewId(d.id); setReviewData({ status: 'APPROVED', feedback: '' }); }}>
                            Review Submission
                          </button>
                        )}
                      </>
                    )}

                    {/* Creator Controls */}
                    {isCreator && (
                      <>
                        {(d.status === 'PENDING' || d.status === 'REJECTED') && submitId !== d.id && (
                          <button className="btn primary" onClick={() => { setSubmitId(d.id); setSubmissionUrl(d.submissionUrl || ''); }}>
                            {d.status === 'PENDING' ? 'Submit Work' : 'Resubmit Work'}
                          </button>
                        )}
                      </>
                    )}

                  </div>
                )}

                {/* Creator Submit Form */}
                {submitId === d.id && (
                  <div style={{ marginTop: '1rem', padding: '1rem', background: 'var(--color-bg)', borderRadius: '8px', border: '1px solid var(--color-border)' }}>
                    <h5 style={{ margin: '0 0 1rem 0' }}>Submit Work URL</h5>
                    <form onSubmit={handleSubmitWork} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                      <input 
                        type="url" 
                        required 
                        className="form-input" 
                        placeholder="https://..." 
                        value={submissionUrl} 
                        onChange={e => setSubmissionUrl(e.target.value)} 
                      />
                      <div style={{ display: 'flex', gap: '1rem' }}>
                        <button type="submit" className="btn primary">Submit</button>
                        <button type="button" className="btn" onClick={() => setSubmitId(null)}>Cancel</button>
                      </div>
                    </form>
                  </div>
                )}

                {/* Brand Review Form */}
                {reviewId === d.id && (
                  <div style={{ marginTop: '1rem', padding: '1.5rem', background: 'var(--color-bg)', borderRadius: '8px', border: '1px solid var(--color-border)' }}>
                    <h5 style={{ margin: '0 0 1rem 0' }}>Review Submission</h5>
                    <form onSubmit={handleReview} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                      
                      <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
                        <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                          <input 
                            type="radio" 
                            name="reviewStatus" 
                            value="APPROVED" 
                            checked={reviewData.status === 'APPROVED'} 
                            onChange={e => setReviewData({...reviewData, status: e.target.value})} 
                          />
                          <span style={{ fontWeight: 'bold', color: '#2e7d32' }}>Approve</span>
                        </label>
                        <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                          <input 
                            type="radio" 
                            name="reviewStatus" 
                            value="REJECTED" 
                            checked={reviewData.status === 'REJECTED'} 
                            onChange={e => setReviewData({...reviewData, status: e.target.value})} 
                          />
                          <span style={{ fontWeight: 'bold', color: '#c62828' }}>Reject</span>
                        </label>
                      </div>

                      {reviewData.status === 'REJECTED' && (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                          <label style={{ fontWeight: 'bold', fontSize: '0.9rem' }}>Rejection Feedback (Required)</label>
                          <textarea 
                            required 
                            className="form-input" 
                            rows={3} 
                            placeholder="Explain what needs to be changed..."
                            value={reviewData.feedback}
                            onChange={e => setReviewData({...reviewData, feedback: e.target.value})}
                          />
                        </div>
                      )}

                      <div style={{ display: 'flex', gap: '1rem', marginTop: '0.5rem' }}>
                        <button type="submit" className="btn primary">Submit Review</button>
                        <button type="button" className="btn" onClick={() => setReviewId(null)}>Cancel</button>
                      </div>
                    </form>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
