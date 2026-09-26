import { useState, useEffect } from 'react';
import { api } from '../../services/api';

export default function CreatorContentList() {
  const [contents, setContents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const handleDelete = async (id) => {
    if (window.confirm("Delete this content?")) {
      try {
        await api.delete(`/content/${id}`);
        setContents(prev => prev.filter(c => c.id !== id));
      } catch (err) {
        alert("Failed to delete content.");
      }
    }
  };

  const handlePublish = async (id) => {
    try {
      const response = await api.post(`/content/${id}/publish`);
      setContents(prev => prev.map(c => c.id === id ? { ...c, status: response.status || 'PUBLISHED' } : c));
    } catch (err) {
      alert("Failed to publish content.");
    }
  };

  useEffect(() => {
    const fetchContent = async () => {
      try {
        const data = await api.get('/content/me');
        setContents(data);
      } catch (err) {
        setError(err.message || 'Error loading content.');
      } finally {
        setLoading(false);
      }
    };
    
    fetchContent();
  }, []);

  if (loading) return <div>Loading content...</div>;
  if (error) return <div className="error-message">{error}</div>;

  return (
    <div>
      {contents.length === 0 ? (
        <p>No content found.</p>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '2rem' }}>
          {contents.map(item => (
            <div key={item.id} className="card" style={{ padding: '0', overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
              {item.thumbnailUrl && <img src={item.thumbnailUrl} alt={item.title} style={{ width: '100%', height: '180px', objectFit: 'cover', borderBottom: 'var(--border-width) solid var(--color-border)' }} />}
              <div style={{ padding: '1rem' }}>
                <h3 style={{ margin: '0 0 0.5rem 0' }}>{item.title}</h3>
                <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.5rem' }}>
                  <span style={{ fontSize: '0.8rem', padding: '0.2rem 0.5rem', backgroundColor: '#e2e2e2', borderRadius: '4px', border: '1px solid var(--color-border)', fontWeight: 'bold' }}>{item.contentType}</span>
                  <span style={{ fontSize: '0.8rem', padding: '0.2rem 0.5rem', backgroundColor: item.status === 'PUBLISHED' ? 'var(--color-accent)' : '#e2e2e2', borderRadius: '4px', border: '1px solid var(--color-border)', fontWeight: 'bold' }}>{item.status}</span>
                </div>
              </div>
              <div style={{ padding: '0.75rem 1rem', borderTop: 'var(--border-width) solid var(--color-border)', display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                {item.status === 'DRAFT' && (
                  <button onClick={() => handlePublish(item.id)} className="btn primary" style={{ flex: 1, padding: '0.5rem', fontSize: '0.9rem', minWidth: '100px' }}>Publish</button>
                )}
                <button onClick={() => handleDelete(item.id)} className="btn" style={{ flex: item.status === 'DRAFT' ? 1 : 'none', width: item.status === 'DRAFT' ? 'auto' : '100%', padding: '0.5rem', fontSize: '0.9rem', backgroundColor: '#ff4444', color: 'white', borderColor: '#cc0000', minWidth: '100px' }}>Delete</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

