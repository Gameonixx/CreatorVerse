import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../services/api';

export default function CampaignCard({ campaign }) {
  const { id, title, description, niche, budget, applicationDeadline, brandName, brandLogoUrl } = campaign;
  const defaultLogo = 'https://ui-avatars.com/api/?name=' + encodeURIComponent(brandName) + '&background=random';

  const isExpired = applicationDeadline && new Date(applicationDeadline) < new Date();

  return (
    <Link to={`/campaigns/${id}`} className="campaign-card" style={{ textDecoration: 'none', color: 'inherit', display: 'flex', flexDirection: 'column', border: '1px solid var(--border-color)', borderRadius: '12px', padding: '1rem', background: 'var(--bg-secondary)', transition: 'transform 0.2s, box-shadow 0.2s', cursor: 'pointer', height: '100%' }}
          onMouseOver={(e) => { e.currentTarget.style.transform = 'translateY(-4px)'; e.currentTarget.style.boxShadow = '0 8px 16px rgba(0,0,0,0.1)'; }}
          onMouseOut={(e) => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = 'none'; }}>
      
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem' }}>
        <img 
          src={brandLogoUrl || defaultLogo} 
          alt={brandName} 
          style={{ width: '40px', height: '40px', borderRadius: '50%', marginRight: '12px', objectFit: 'cover' }} 
          onError={(e) => { e.target.src = defaultLogo; }} 
        />
        <div>
          <div style={{ fontWeight: 'bold', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>{brandName}</div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-tertiary)' }}>{niche}</div>
        </div>
      </div>

      <h3 style={{ margin: '0 0 0.5rem 0', fontSize: '1.1rem', color: 'var(--text-primary)' }}>{title}</h3>
      <p style={{ margin: '0 0 1rem 0', fontSize: '0.9rem', color: 'var(--text-secondary)', flexGrow: 1 }}>
        {description?.substring(0, 100)}{description?.length > 100 ? '...' : ''}
      </p>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid var(--border-color)', paddingTop: '0.75rem' }}>
        <div style={{ fontWeight: 'bold', color: 'var(--accent-primary)' }}>${budget?.toLocaleString() || 'Negotiable'}</div>
        <div style={{ fontSize: '0.8rem', color: isExpired ? 'var(--error-color, #ff4444)' : 'var(--text-secondary)' }}>
          {isExpired ? 'Expired' : applicationDeadline ? new Date(applicationDeadline).toLocaleDateString() : 'No Deadline'}
        </div>
      </div>
    </Link>
  );
}
