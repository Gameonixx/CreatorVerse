import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import ContentCard from '../components/content/ContentCard';

export default function FeedPage() {
  const [content, setContent] = useState([]);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState(null);
  const [hasMore, setHasMore] = useState(false);

  const fetchFeed = async (pageNumber, isInitial = false) => {
    try {
      if (isInitial) {
        setLoading(true);
      } else {
        setLoadingMore(true);
      }
      setError(null);

      const response = await api.get(`/content/feed?page=${pageNumber}&size=10`);
      
      const newContent = response.content || [];
      
      if (isInitial) {
        setContent(newContent);
      } else {
        setContent(prev => [...prev, ...newContent]);
      }
      
      setHasMore(!response.last);
      setPage(pageNumber);
      
    } catch (err) {
      setError(err.message || 'Failed to load feed');
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  };

  useEffect(() => {
    fetchFeed(0, true);
  }, []);

  const handleLoadMore = () => {
    if (!loadingMore && hasMore) {
      fetchFeed(page + 1);
    }
  };

  return (
    <div className="feed-page">
      <div className="feed-header">
        <h1 className="feed-title">Explore</h1>
      </div>

      <div className="feed-content">
        {loading && <div className="feed-loading">Loading content...</div>}
        
        {error && (
          <div className="error-message">
            {error}
            <button className="btn" onClick={() => fetchFeed(0, true)} style={{ marginTop: '1rem', display: 'block' }}>
              Try Again
            </button>
          </div>
        )}

        {!loading && !error && content.length === 0 && (
          <div className="feed-empty">
            <p>No content available right now.</p>
          </div>
        )}

        {!loading && content.length > 0 && (
          <div className="feed-grid">
            {content.map(item => (
              <ContentCard key={item.id} content={item} />
            ))}
          </div>
        )}

        {hasMore && (
          <div className="feed-load-more">
            <button 
              className="btn primary" 
              onClick={handleLoadMore} 
              disabled={loadingMore}
            >
              {loadingMore ? 'Loading...' : 'Load more'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
