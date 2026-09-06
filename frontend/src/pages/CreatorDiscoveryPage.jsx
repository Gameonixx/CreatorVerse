import React, { useState, useEffect, useCallback } from 'react';
import { api } from '../services/api';
import CreatorCard from '../components/creator/CreatorCard';
import { useLocation, useNavigate } from 'react-router-dom';

export default function CreatorDiscoveryPage() {
  const [creators, setCreators] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Pagination
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const size = 12;

  // Filters
  const [search, setSearch] = useState('');
  const [niche, setNiche] = useState('');
  const [minFollowers, setMinFollowers] = useState('');
  const [sortBy, setSortBy] = useState('followerCount');

  const fetchCreators = useCallback(async (currentPage = page) => {
    try {
      setLoading(true);
      setError(null);
      
      const params = new URLSearchParams();
      params.append('page', currentPage);
      params.append('size', size);
      params.append('sortBy', sortBy);
      params.append('direction', 'desc'); // Standard for discovery

      if (search) params.append('search', search);
      if (niche) params.append('niche', niche);
      if (minFollowers) params.append('minFollowers', minFollowers);

      const response = await api.get(`/discovery/creators?${params.toString()}`);
      
      setCreators(response.content || []);
      setTotalPages(response.totalPages || 0);
      setTotalElements(response.totalElements || 0);
      setPage(currentPage);
    } catch (err) {
      console.error('Error fetching creators', err);
      setError('Failed to load creators. Please try again later.');
    } finally {
      setLoading(false);
    }
  }, [search, niche, minFollowers, sortBy, page]);

  useEffect(() => {
    // Reset to page 0 when filters change, except for initial mount where page is 0 anyway
    fetchCreators(0);
  }, [search, niche, minFollowers, sortBy]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchCreators(0);
  };

  return (
    <div className="discovery-page-container">
      <header className="discovery-header">
        <h1 className="page-title discovery-title">Creator Marketplace</h1>
        <p className="discovery-subtitle">Discover top creators for your next campaign.</p>
      </header>

      {/* Desktop Filters Section */}
      <div className="desktop-only-filters">
        <form onSubmit={handleSearchSubmit}>
          <div className="desktop-search-wrapper">
            <svg className="desktop-search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
            <input 
              type="text" 
              placeholder="Search creators..." 
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="desktop-search-input"
            />
          </div>
          <div className="desktop-controls-row">
            <select 
              value={niche}
              onChange={(e) => setNiche(e.target.value)}
              className="desktop-select-control"
            >
              <option value="">Niche</option>
              <option value="Fashion">Fashion</option>
              <option value="Tech">Tech</option>
              <option value="Gaming">Gaming</option>
              <option value="Lifestyle">Lifestyle</option>
              <option value="Fitness">Fitness</option>
              <option value="Beauty">Beauty</option>
              <option value="Education">Education</option>
            </select>
            <select 
              value={minFollowers}
              onChange={(e) => setMinFollowers(e.target.value)}
              className="desktop-select-control"
            >
              <option value="">Followers</option>
              <option value="100">100+</option>
              <option value="1000">1K+</option>
              <option value="10000">10K+</option>
              <option value="100000">100K+</option>
            </select>
            <button 
              type="button"
              className="desktop-sort-btn"
              onClick={() => setSortBy(sortBy === 'followerCount' ? 'engagementRate' : 'followerCount')}
              aria-label="Toggle Sort"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="m21 16-4 4-4-4"/>
                <path d="M17 20V4"/>
                <path d="m3 8 4-4 4 4"/>
                <path d="M7 4v16"/>
              </svg>
            </button>
          </div>
        </form>
      </div>

      {/* Mobile Filters Section */}
      <div className="mobile-only-filters">
        <div className="mobile-search-wrapper">
          <svg className="mobile-search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="11" cy="11" r="8"></circle>
            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
          </svg>
          <input 
            type="text" 
            placeholder="Search creators..." 
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="mobile-search-input"
          />
        </div>
        <div className="mobile-controls-row">
          <select 
            value={niche}
            onChange={(e) => setNiche(e.target.value)}
            className="mobile-select-control"
          >
            <option value="">Niche</option>
            <option value="Fashion">Fashion</option>
            <option value="Tech">Tech</option>
            <option value="Gaming">Gaming</option>
            <option value="Lifestyle">Lifestyle</option>
            <option value="Fitness">Fitness</option>
            <option value="Beauty">Beauty</option>
            <option value="Education">Education</option>
          </select>
          <select 
            value={minFollowers}
            onChange={(e) => setMinFollowers(e.target.value)}
            className="mobile-select-control"
          >
            <option value="">Followers</option>
            <option value="100">100+</option>
            <option value="1000">1K+</option>
            <option value="10000">10K+</option>
            <option value="100000">100K+</option>
          </select>
          <button 
            className="mobile-sort-btn"
            onClick={() => setSortBy(sortBy === 'followerCount' ? 'engagementRate' : 'followerCount')}
            aria-label="Toggle Sort"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="m21 16-4 4-4-4"/>
              <path d="M17 20V4"/>
              <path d="m3 8 4-4 4 4"/>
              <path d="M7 4v16"/>
            </svg>
          </button>
        </div>
      </div>

      {/* Results Section */}
      <div className="discovery-results-header">
        <h2 className="discovery-results-title">{totalElements > 0 ? `${totalElements} Creators Found` : 'No Creators Found'}</h2>
      </div>

      {loading && creators.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '4rem 0', color: 'var(--text-secondary)' }}>
          Loading creators...
        </div>
      ) : error ? (
        <div style={{ textAlign: 'center', padding: '4rem 0', color: '#ff4444' }}>
          {error}
        </div>
      ) : creators.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '4rem 0', color: 'var(--text-secondary)' }}>
          No creators match your search criteria. Try adjusting your filters.
        </div>
      ) : (
        <div className="discovery-grid">
          {creators.map(creator => (
            <CreatorCard key={creator.userId} creator={creator} />
          ))}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '1rem', marginTop: '2rem' }}>
          <button 
            className="btn" 
            disabled={page === 0 || loading} 
            onClick={() => fetchCreators(page - 1)}
          >
            Previous
          </button>
          <span style={{ fontWeight: 600 }}>Page {page + 1} of {totalPages}</span>
          <button 
            className="btn" 
            disabled={page >= totalPages - 1 || loading} 
            onClick={() => fetchCreators(page + 1)}
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
}
