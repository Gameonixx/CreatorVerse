import React, { useState, useEffect, useRef } from 'react';
import { collaborationApi } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { Link } from 'react-router-dom';

export default function MessagingSection({ collaborationId, collaborationStatus, otherParticipantName }) {
  const { user } = useAuth();
  const [messages, setMessages] = useState([]);
  const [conversation, setConversation] = useState(null);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const messagesEndRef = useRef(null);

  const fetchMessages = async () => {
    try {
      const convRes = await collaborationApi.getConversation(collaborationId);
      setConversation(convRes);

      const msgRes = await collaborationApi.getMessages(collaborationId, 0, 50);
      // API returns newest first (desc). We want oldest first (asc) for rendering.
      setMessages(msgRes.content.reverse());

      if (convRes.unreadCount > 0) {
        await collaborationApi.markMessagesAsRead(collaborationId);
      }
    } catch (err) {
      console.error("Failed to load messages", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMessages();
    const interval = setInterval(fetchMessages, 5000);
    return () => clearInterval(interval);
  }, [collaborationId]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!newMessage.trim() || sending) return;

    try {
      setSending(true);
      const sentMessage = await collaborationApi.sendMessage(collaborationId, newMessage);
      setMessages(prev => [...prev, sentMessage]);
      setNewMessage('');
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    } catch (err) {
      alert(err.message || 'Failed to send message');
    } finally {
      setSending(false);
    }
  };

  if (loading && !conversation) {
    return <div className="card" style={{ padding: '2rem', textAlign: 'center' }}>Loading messages...</div>;
  }

  return (
    <div className="card" style={{ display: 'flex', flexDirection: 'column', height: '600px', maxHeight: '70vh' }}>
      <div style={{ padding: '1rem 1.5rem', borderBottom: '1px solid var(--color-border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h3 style={{ margin: 0 }}>Conversation with {otherParticipantName}</h3>
      </div>
      
      <div style={{ flex: 1, overflowY: 'auto', padding: '1.5rem', display: 'flex', flexDirection: 'column', gap: '1rem', background: '#fafafa' }}>
        {messages.length === 0 ? (
          <div style={{ margin: 'auto', color: 'var(--text-secondary)', textAlign: 'center' }}>
            No messages yet. Start the conversation!
          </div>
        ) : (
          messages.map(msg => {
            const isMe = msg.senderUserId === user.id;
            return (
              <div key={msg.id} style={{ display: 'flex', flexDirection: 'column', alignItems: isMe ? 'flex-end' : 'flex-start' }}>
                <div style={{ display: 'flex', alignItems: 'flex-end', gap: '0.5rem', maxWidth: '85%' }}>
                  {!isMe && (
                    <img 
                      src={msg.senderAvatarUrl || `https://ui-avatars.com/api/?name=${msg.senderName}&background=random`} 
                      alt={msg.senderName}
                      style={{ width: '32px', height: '32px', borderRadius: '50%', objectFit: 'cover' }}
                    />
                  )}
                  <div style={{
                    background: isMe ? 'var(--color-primary)' : 'white',
                    color: isMe ? 'white' : 'var(--text-primary)',
                    padding: '0.75rem 1rem',
                    borderRadius: '12px',
                    borderBottomRightRadius: isMe ? '4px' : '12px',
                    borderBottomLeftRadius: !isMe ? '4px' : '12px',
                    border: isMe ? 'none' : '1px solid var(--color-border)',
                    boxShadow: '0 1px 2px rgba(0,0,0,0.05)',
                    whiteSpace: 'pre-wrap',
                    wordBreak: 'break-word',
                    lineHeight: '1.4'
                  }}>
                    {msg.content}
                  </div>
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.25rem', padding: '0 0.5rem' }}>
                  {new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </div>
              </div>
            );
          })
        )}
        <div ref={messagesEndRef} />
      </div>

      {collaborationStatus === 'ACTIVE' ? (
        <form onSubmit={handleSendMessage} style={{ padding: '1rem 1.5rem', borderTop: '1px solid var(--color-border)', display: 'flex', gap: '0.5rem', background: 'white' }}>
          <input
            type="text"
            value={newMessage}
            onChange={(e) => setNewMessage(e.target.value)}
            placeholder="Write a message..."
            style={{ flex: 1, padding: '0.75rem 1rem', borderRadius: '24px', border: '1px solid var(--color-border)', outline: 'none', fontSize: '1rem' }}
            disabled={sending}
            maxLength={5000}
          />
          <button 
            type="submit" 
            className="btn primary" 
            disabled={!newMessage.trim() || sending}
            style={{ borderRadius: '24px', padding: '0.5rem 1.25rem' }}
          >
            {sending ? 'Sending...' : 'Send'}
          </button>
        </form>
      ) : (
        <div style={{ padding: '1rem', textAlign: 'center', color: 'var(--text-secondary)', background: 'var(--color-bg)', borderTop: '1px solid var(--color-border)' }}>
          This collaboration is {collaborationStatus.toLowerCase()}. You can no longer send messages.
        </div>
      )}
    </div>
  );
}
