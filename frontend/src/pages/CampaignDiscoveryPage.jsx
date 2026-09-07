import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import CampaignCard from '../components/campaign/CampaignCard';

export default function CampaignDiscoveryPage() {
  const [campaigns, setCampaigns] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const [search, setSearch] = useState('');
  const [niche, setNiche] = useState('');
  
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchCampaigns = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams();
      if (search) params.append('search', search);
      if (niche) params.append('niche', niche);
      params.append('page', page);
      params.append('size', 12);
      
      const response = await api.get(`/campaigns?${params.toString()}`);
      setCampaigns(response.content);
      setTotalPages(response.totalPages);
    } catch (err) {
      setError('Failed to load campaigns.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCampaigns();
  }, [page, search, niche]);

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    fetchCampaigns();
  };

  return (
    <div className="page-container" style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
      <header style={{ textAlign: 'center', marginBottom: '3rem' }}>
        <h1 className="page-title">Campaign Marketplace</h1>
        <p className="discovery-subtitle" style={{ color: 'var(--text-secondary)' }}>Discover opportunities from top brands.</p>
      </header>

      <form onSubmit={handleSearch} style={{ display: 'flex', gap: '1rem', marginBottom: '2rem', flexWrap: 'wrap' }}>
        <input 
          type="text" 
          placeholder="Search campaigns..." 
          value={search} 
          onChange={(e) => setSearch(e.target.value)} 
          className="form-input"
          style={{ flexGrow: 1, padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--border-color)', background: 'var(--bg-secondary)', color: 'var(--text-primary)' }}
        />
        <input 
          type="text" 
          placeholder="Niche (e.g., Tech)" 
          value={niche} 
          onChange={(e) => setNiche(e.target.value)} 
          className="form-input"
          style={{ width: '200px', padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--border-color)', background: 'var(--bg-secondary)', color: 'var(--text-primary)' }}
        />
        <button type="submit" className="btn primary" style={{ padding: '0.75rem 1.5rem', borderRadius: '8px' }}>Search</button>
      </form>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem' }}>Loading campaigns...</div>
      ) : error ? (
        <div style={{ color: 'var(--error-color, #ff4444)', textAlign: 'center' }}>{error}</div>
      ) : campaigns.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-secondary)' }}>No campaigns found matching your criteria.</div>
      ) : (
        <>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1.5rem' }}>
            {campaigns.map(campaign => (
              <CampaignCard key={campaign.id} campaign={campaign} />
            ))}
          </div>
          
          {totalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'center', marginTop: '3rem', gap: '1rem' }}>
              <button 
                onClick={() => setPage(p => Math.max(0, p - 1))} 
                disabled={page === 0}
                className="btn"
              >
                Previous
              </button>
              <span style={{ padding: '0.5rem', color: 'var(--text-secondary)' }}>Page {page + 1} of {totalPages}</span>
              <button 
                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} 
                disabled={page >= totalPages - 1}
                className="btn"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
