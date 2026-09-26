import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import ContentCard from '../components/content/ContentCard';
import { useAuth } from '../context/AuthContext';

export default function HomePage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [content, setContent] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/creators?search=${encodeURIComponent(searchQuery)}`);
    }
  };

  useEffect(() => {
    const fetchFeed = async () => {
      try {
        const response = await api.get('/content/feed?page=0&size=5');
        setContent(response.content || []);
      } catch (err) {
        console.error('Failed to load feed', err);
      } finally {
        setLoading(false);
      }
    };
    fetchFeed();
  }, []);

  return (
    <div className="home-page" style={{ padding: '0', display: 'flex', flexDirection: 'column', alignItems: 'center', width: '100%' }}>
      
      {/* 1. SOCIAL CONTENT / DISCOVERY AREA */}
      <div style={{ width: '100%', maxWidth: '600px', padding: '0 0 1.5rem 0', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center', gap: '0' }}>
          <h1 style={{ margin: '0', fontSize: '2.25rem', letterSpacing: '-0.02em', lineHeight: '1.2' }}>Discover</h1>
          <p style={{ margin: 0, color: 'var(--color-text-secondary)', fontSize: '1rem' }}>Find top creators and trending content.</p>
        </div>

        <form onSubmit={handleSearch} style={{ width: '100%' }}>
          <div style={{ display: 'flex', alignItems: 'center', background: 'var(--color-surface)', border: '2px solid var(--color-border)', borderRadius: '12px', padding: '0.25rem 1rem', boxShadow: 'var(--shadow-brutal)' }}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ width: '20px', height: '20px', color: 'var(--color-text-primary)', flexShrink: 0 }}>
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
            <div style={{ width: '2px', height: '24px', backgroundColor: 'var(--color-border)', margin: '0 1rem' }}></div>
            <input 
              type="text" 
              placeholder="Search creators..." 
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={{ flex: 1, border: 'none', background: 'transparent', padding: '0.75rem 0', fontSize: '1rem', outline: 'none', boxShadow: 'none', color: 'var(--color-text-primary)' }}
            />
          </div>
        </form>
        
        {loading ? (
          <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-secondary)' }}>Loading feed...</div>
        ) : content.length > 0 ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '2.5rem' }}>
            {content.map(item => (
              <ContentCard key={item.id} content={item} onDelete={(id) => setContent(prev => prev.filter(c => c.id !== id))} />
            ))}
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: '3rem 2rem', background: 'var(--color-surface)', border: '2px solid var(--color-border)', borderRadius: '12px', boxShadow: 'var(--shadow-brutal)' }}>
            <h3 style={{ margin: '0 0 1rem 0' }}>Welcome to CreatorVerse</h3>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>The feed is currently empty. Be the first to publish content!</p>
            {!user && <Link to="/register" className="btn primary">Join Now</Link>}
          </div>
        )}

        {content.length > 0 && (
          <div style={{ textAlign: 'center', marginTop: '1rem' }}>
            <Link to="/feed" className="btn" style={{ width: '100%' }}>View More Content</Link>
          </div>
        )}
      </div>

      {/* 2. SHORT CREATORVERSE INTRODUCTION */}
      <div style={{ width: '100%', maxWidth: '600px', padding: '0 0 3rem 0' }}>
        <div className="card" style={{ background: 'var(--color-accent)', textAlign: 'center', padding: '1.5rem 1rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <h2 style={{ fontSize: 'clamp(1.5rem, 6vw, 1.75rem)', margin: '0' }}>What is <span style={{ whiteSpace: 'nowrap' }}>CreatorVerse?</span></h2>
          <p style={{ fontSize: '1.05rem', color: 'var(--color-text-primary)', lineHeight: '1.6', margin: '0 auto', maxWidth: '100%' }}>
            A creator-first social platform where creators publish, audiences discover, and brands eventually collaborate.
          </p>
          
          <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', flexWrap: 'wrap', marginTop: '1rem' }}>
            {!user && <Link to="/register" className="btn">Join</Link>}
            <Link to="/campaigns" className="btn">Campaigns</Link>
          </div>
        </div>
      </div>
      
    </div>
  );
}
