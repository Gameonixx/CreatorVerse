import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { api } from '../../services/api';

export default function CommentSection({ contentId }) {
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [newCommentText, setNewCommentText] = useState('');
  
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchComments = async () => {
      try {
        setLoading(true);
        setError(null);
        const data = await api.get(`/social/content/${contentId}/comments`);
        setComments(data);
      } catch (err) {
        setError('Failed to load comments.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    if (contentId) {
      fetchComments();
    }
  }, [contentId]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!user) {
      navigate('/login');
      return;
    }

    if (!newCommentText.trim() || submitting) return;

    try {
      setSubmitting(true);
      setError(null);
      
      const newComment = await api.post(`/social/content/${contentId}/comments`, {
        text: newCommentText.trim()
      });
      
      setComments((prev) => [...prev, newComment]);
      setNewCommentText('');
    } catch (err) {
      console.error('Failed to post comment:', err);
      setError('Failed to post comment. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (commentId) => {
    if (!window.confirm('Are you sure you want to delete this comment?')) return;
    
    try {
      await api.delete(`/social/comments/${commentId}`);
      setComments((prev) => prev.filter(c => c.id !== commentId));
    } catch (err) {
      console.error('Failed to delete comment:', err);
      alert('Failed to delete comment.');
    }
  };

  return (
    <section className="comment-section">
      <h3 className="comment-section-title">Comments ({comments.length})</h3>
      
      {error && <div className="comment-error">{error}</div>}

      <form className="comment-form" onSubmit={handleSubmit}>
        <textarea
          className="comment-input"
          placeholder={user ? "Add a comment..." : "Log in to add a comment..."}
          value={newCommentText}
          onChange={(e) => setNewCommentText(e.target.value)}
          onFocus={() => {
            if (!user) {
              navigate('/login');
            }
          }}
          disabled={submitting}
          rows="3"
        />
        <button 
          type="submit" 
          className="btn primary comment-submit-btn" 
          disabled={submitting || !newCommentText.trim() || !user}
        >
          {submitting ? 'Posting...' : 'Post Comment'}
        </button>
      </form>

      <div className="comment-list">
        {loading ? (
          <div className="comment-loading">Loading comments...</div>
        ) : comments.length === 0 ? (
          <div className="comment-empty">No comments yet. Be the first to share your thoughts!</div>
        ) : (
          comments.map((comment) => (
            <div key={comment.id} className="comment-item">
              <div className="comment-header">
                <span className="comment-author">{comment.userDisplayName || comment.username}</span>
                <span className="comment-date">
                  {new Date(comment.createdAt).toLocaleDateString(undefined, { 
                    year: 'numeric', month: 'short', day: 'numeric' 
                  })}
                </span>
                {user && user.username === comment.username && (
                  <button 
                    className="comment-delete-btn"
                    onClick={() => handleDelete(comment.id)}
                    aria-label="Delete comment"
                  >
                    Delete
                  </button>
                )}
              </div>
              <div className="comment-body">
                {comment.text}
              </div>
            </div>
          ))
        )}
      </div>
    </section>
  );
}
