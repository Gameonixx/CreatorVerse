import React from 'react';
import { Link } from 'react-router-dom';

export default function UserCard({ user }) {
  const { id, username, displayName, role, avatarUrl } = user;
  
  const isCreator = role === 'CREATOR' || role === 'ADMIN';

  const content = (
    <>
      <div className="user-card-avatar">
        {avatarUrl ? (
          <img src={avatarUrl} alt={username} style={{ width: '100%', height: '100%', borderRadius: '50%', objectFit: 'cover' }} />
        ) : (
          (displayName || username || '?').charAt(0).toUpperCase()
        )}
      </div>
      <div className="user-card-info">
        <p className="user-card-display-name">{displayName || username}</p>
        <p className="user-card-username">@{username}</p>
      </div>
    </>
  );

  if (isCreator) {
    return (
      <Link to={`/user/${id}`} className="user-card clickable">
        {content}
      </Link>
    );
  }

  return (
    <div className="user-card non-clickable">
      {content}
    </div>
  );
}
