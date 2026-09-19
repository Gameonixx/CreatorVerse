import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../services/api';

export default function CampaignCard({ campaign }) {
  const { id, title, description, niche, budget, applicationDeadline, brandName, brandLogoUrl } = campaign;
  const defaultLogo = 'https://ui-avatars.com/api/?name=' + encodeURIComponent(brandName) + '&background=random';

  const isExpired = applicationDeadline && new Date(applicationDeadline) < new Date();

  return (
    <Link to={`/campaigns/${id}`} className="campaign-card">
      <div className="campaign-card-header">
        <img 
          src={brandLogoUrl || defaultLogo} 
          alt={brandName} 
          className="campaign-card-brand-logo"
          onError={(e) => { e.target.src = defaultLogo; }} 
        />
        <div className="campaign-card-brand-info">
          <h3 className="campaign-card-title">{title}</h3>
          <div className="campaign-card-brand-name">{brandName} &bull; <span className="campaign-card-niche">{niche}</span></div>
        </div>
      </div>

      <div className="campaign-card-body">
        <p className="campaign-card-desc">
          {description?.substring(0, 100)}{description?.length > 100 ? '...' : ''}
        </p>
      </div>

      <div className="campaign-card-meta">
        <div className="campaign-card-budget">${budget?.toLocaleString() || 'Negotiable'}</div>
        <div className="campaign-card-deadline" style={{ color: isExpired ? '#ff4444' : undefined }}>
          {isExpired ? 'Expired' : applicationDeadline ? new Date(applicationDeadline).toLocaleDateString() : 'No Deadline'}
        </div>
      </div>
    </Link>
  );
}
