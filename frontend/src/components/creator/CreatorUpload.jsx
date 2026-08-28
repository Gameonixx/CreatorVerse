import { useState } from 'react';
import { api } from '../../services/api';

export default function CreatorUpload() {
  const [title, setTitle] = useState('');
  const [caption, setCaption] = useState('');
  const [contentType, setContentType] = useState('IMAGE');
  const [file, setFile] = useState(null);
  const [status, setStatus] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e, publishNow) => {
    e.preventDefault();
    if (!file) {
      setStatus('Please select a file.');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);
    
    const metadata = {
      title,
      caption,
      contentType,
      visibility: 'PUBLIC'
    };
    
    formData.append('metadata', new Blob([JSON.stringify(metadata)], { type: 'application/json' }));
    formData.append('publishNow', publishNow);

    setStatus('Uploading...');
    setIsLoading(true);
    
    try {
      await api.post('/content', formData);
      setStatus(`Content ${publishNow ? 'published' : 'saved as draft'} successfully!`);
      setTitle('');
      setCaption('');
      setFile(null);
    } catch (error) {
      setStatus(error.message || 'Error uploading content.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="card" style={{ maxWidth: '600px', margin: '0 auto' }}>
      <form className="auth-form">
        <div className="form-group">
          <label htmlFor="title">Title</label>
          <input id="title" type="text" value={title} onChange={e => setTitle(e.target.value)} required />
        </div>
        
        <div className="form-group">
          <label htmlFor="caption">Caption</label>
          <textarea id="caption" value={caption} onChange={e => setCaption(e.target.value)} rows="3" />
        </div>

        <div className="form-group">
          <label htmlFor="contentType">Type</label>
          <select id="contentType" value={contentType} onChange={e => setContentType(e.target.value)}>
            <option value="IMAGE">Image</option>
            <option value="VIDEO">Video</option>
            <option value="REEL">Reel</option>
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="file">Media File</label>
          <input id="file" type="file" onChange={e => setFile(e.target.files[0])} required />
        </div>

        <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
          <button type="button" className="btn" onClick={(e) => handleSubmit(e, false)} disabled={isLoading}>
            Save Draft
          </button>
          <button type="button" className="btn primary" onClick={(e) => handleSubmit(e, true)} disabled={isLoading}>
            Publish
          </button>
        </div>
        
        {status && <div className={status.includes('Error') || status.includes('Failed') || status.includes('Please') ? 'error-message' : ''} style={{ marginTop: '1rem', fontWeight: '500' }}>
          {status}
        </div>}
      </form>
    </div>
  );
}

