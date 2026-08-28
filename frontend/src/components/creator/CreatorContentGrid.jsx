import React, { useState, useEffect } from 'react';
import { api } from '../../services/api';
import ContentCard from '../content/ContentCard';

export default function CreatorContentGrid({ creatorId, onDisplayNameDiscovered }) {
  const [content, setContent] = useState([]);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState(null);
  const [hasMore, setHasMore] = useState(false);

  const fetchCreatorContent = async (pageNumber, isInitial = false) => {
    try {
      if (isInitial) {
        setLoading(true);
      } else {
        setLoadingMore(true);
      }
      setError(null);

      const response = await api.get(`/content/creator/${creatorId}?page=${pageNumber}&size=10`);
      
      const newContent = response.content || [];
      
      if (isInitial) {
        setContent(newContent);
        // If we found content and it has a display name, pass it up to the header
        if (newContent.length > 0 && newContent[0].creatorDisplayName && onDisplayNameDiscovered) {
          onDisplayNameDiscovered(newContent[0].creatorDisplayName);
        }
      } else {
        setContent(prev => [...prev, ...newContent]);
      }
      
      setHasMore(!response.last);
      setPage(pageNumber);
      
    } catch (err) {
      setError(err.message || 'Failed to load creator content');
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  };

  useEffect(() => {
    if (creatorId) {
      fetchCreatorContent(0, true);
    }
  }, [creatorId]);

  const handleLoadMore = () => {
    if (!loadingMore && hasMore) {
      fetchCreatorContent(page + 1);
    }
  };

  if (loading) {
    return <div className="creator-loading">Loading portfolio...</div>;
  }

  if (error) {
    return (
      <div className="creator-error">
        {error}
        <button className="btn" onClick={() => fetchCreatorContent(0, true)} style={{ marginTop: '1rem', display: 'block', margin: '1rem auto 0 auto' }}>
          Try Again
        </button>
      </div>
    );
  }

  if (content.length === 0) {
    return (
      <div className="creator-empty">
        No public content yet.
      </div>
    );
  }

  return (
    <div className="creator-content-section">
      <h2 className="creator-content-section-title">Portfolio</h2>
      
      <div className="creator-content-grid">
        {content.map(item => (
          <ContentCard key={item.id} content={item} />
        ))}
      </div>

      {hasMore && (
        <div className="creator-load-more">
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
  );
}
