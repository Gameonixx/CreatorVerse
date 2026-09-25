import { useState } from 'react';
import { NavLink, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import NotificationDropdown from '../notification/NotificationDropdown';
import UserSearchModal from './UserSearchModal';
import { useNotifications } from '../../hooks/useNotifications';

export default function Navbar() {
  const { user, logout, hasCreatorMode, hasBrandMode } = useAuth();
  const navigate = useNavigate();
  const [isMoreMenuOpen, setIsMoreMenuOpen] = useState(false);
  const [isContextDropdownOpen, setIsContextDropdownOpen] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);

  const notificationState = useNotifications();

  const handleLogout = () => {
    logout();
    navigate('/');
    setIsMoreMenuOpen(false);
  };

  const closeMenu = () => setIsMoreMenuOpen(false);

  const NavIcon = ({ path, polyline, circle, polygon, paths }) => (
    <svg className="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
      {path && <path d={path} />}
      {polyline && <polyline points={polyline} />}
      {circle && <circle {...circle} />}
      {polygon && <polygon points={polygon} />}
      {paths && paths.map((p, i) => <path key={i} d={p} />)}
    </svg>
  );

  return (
    <>
      <nav className="navbar">
        <Link to="/" className="navbar-brand">
          <span className="navbar-brand-accent">CV</span>
          <span className="navbar-brand-text">CreatorVerse</span>
        </Link>

        {/* Desktop Links (Hidden on Mobile) */}
        <div className="navbar-links desktop-only">
          <NavLink to="/" onClick={closeMenu} className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')} aria-label="Home">
            <NavIcon path="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" polyline="9 22 9 12 15 12 15 22" />
            <span className="nav-text">Home</span>
          </NavLink>
          <NavLink to="/feed" onClick={closeMenu} className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')} aria-label="Explore">
            <NavIcon circle={{cx: "12", cy: "12", r: "10"}} polygon="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76" />
            <span className="nav-text">Explore</span>
          </NavLink>
          <NavLink to="/creators" onClick={closeMenu} className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')} aria-label="Creators">
            <span className="nav-text">Creators</span>
          </NavLink>
          <NavLink to="/campaigns" onClick={closeMenu} className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')} aria-label="Campaigns">
            <span className="nav-text">Campaigns</span>
          </NavLink>
        </div>

        <div className="navbar-actions">
          {user ? (
            <>
              {/* Header Action Area (Always Visible) */}
              <button
                className="nav-action-btn"
                onClick={() => setIsSearchOpen(true)}
                aria-label="Search"
                style={{ background: 'none', border: 'none', cursor: 'pointer', padding: '0.25rem', color: 'var(--color-text-primary)' }}
              >
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="11" cy="11" r="8"></circle>
                  <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                </svg>
              </button>

              <NotificationDropdown notificationState={notificationState} />

              <Link to={`/user/${user.id}`} className="nav-avatar-btn" style={{ marginLeft: '0.25rem', display: 'flex', alignItems: 'center', justifyContent: 'center', width: '32px', height: '32px', borderRadius: '50%', backgroundColor: 'var(--color-accent)', overflow: 'hidden' }}>
                {user.avatarUrl ? (
                  <img src={user.avatarUrl} alt="Profile" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                ) : (
                  <span style={{ fontSize: '14px', fontWeight: 'bold' }}>
                    {(user.displayName || user.username || '?').charAt(0).toUpperCase()}
                  </span>
                )}
              </Link>

              <Link to="/upload" className="btn primary desktop-only">Upload</Link>
              <Link to="/my-content" className="btn desktop-only">My Content</Link>

              {hasCreatorMode && !hasBrandMode && (
                <Link to="/dashboard/creator" className="btn desktop-only" title="Creator Dashboard">Creator Mode</Link>
              )}
              {!hasCreatorMode && hasBrandMode && (
                <Link to="/dashboard/brand" className="btn desktop-only" title="Brand Dashboard">Brand Mode</Link>
              )}
              {hasCreatorMode && hasBrandMode && (
                <div className="desktop-only" style={{ position: 'relative' }}>
                  <button
                    className="btn"
                    onClick={() => setIsContextDropdownOpen(!isContextDropdownOpen)}
                  >
                    Professional Context ▼
                  </button>
                  {isContextDropdownOpen && (
                    <div className="card" style={{ position: 'absolute', top: '100%', right: 0, marginTop: '0.5rem', display: 'flex', flexDirection: 'column', minWidth: '180px', zIndex: 10 }}>
                      <Link to="/dashboard/creator" className="menu-item" onClick={() => setIsContextDropdownOpen(false)}>Creator Dashboard</Link>
                      <Link to="/dashboard/brand" className="menu-item" onClick={() => setIsContextDropdownOpen(false)}>Brand Dashboard</Link>
                    </div>
                  )}
                </div>
              )}

              <button onClick={handleLogout} className="btn desktop-only">Log Out</button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn desktop-only">Log In</Link>
              <Link to="/register" className="btn primary desktop-only">Sign Up</Link>
            </>
          )}
        </div>
      </nav>

      {/* Mobile More Menu */}
      {isMoreMenuOpen && (
        <div className="mobile-more-menu mobile-only-nav">
          <div className="mobile-more-menu-content card">
            <div className="menu-group">
              <Link to="/creators" className="menu-item" onClick={closeMenu}>Creators</Link>
              <Link to="/campaigns" className="menu-item" onClick={closeMenu}>Campaigns</Link>
            </div>

            {user ? (
              <>
                <div className="menu-group">
                  <Link to="/upload" className="menu-item" onClick={closeMenu}>Upload</Link>
                  <Link to="/my-content" className="menu-item" onClick={closeMenu}>My Content</Link>
                  <Link to={`/user/${user.id}`} className="menu-item" onClick={closeMenu}>Profile</Link>
                  {hasCreatorMode && <Link to="/dashboard/creator" className="menu-item" onClick={closeMenu}>Creator Dashboard</Link>}
                  {hasBrandMode && <Link to="/dashboard/brand" className="menu-item" onClick={closeMenu}>Brand Dashboard</Link>}
                </div>
                <div className="menu-group">
                  <button className="menu-item text-left" style={{ width: '100%', background: 'none', border: 'none', padding: '1rem', cursor: 'pointer', fontWeight: 600, fontFamily: 'var(--font-body)', fontSize: '1rem' }} onClick={handleLogout}>Log Out</button>
                </div>
              </>
            ) : (
              <div className="menu-group" style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', padding: '1rem' }}>
                <Link to="/login" className="btn text-center" onClick={closeMenu}>Log In</Link>
                <Link to="/register" className="btn primary text-center" onClick={closeMenu}>Sign Up</Link>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Search Modal */}
      <UserSearchModal isOpen={isSearchOpen} onClose={() => setIsSearchOpen(false)} />

      {/* Mobile Bottom Navigation */}
      {user && (
        <div className="mobile-bottom-nav mobile-only-nav">
          {/* Home */}
          <NavLink to="/" onClick={closeMenu} className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')} style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', width: '100%', height: '100%', color: 'var(--color-text-primary)', textDecoration: 'none' }}>
            <NavIcon path="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" polyline="9 22 9 12 15 12 15 22" />
          </NavLink>

          {/* Explore */}
          <NavLink to="/feed" onClick={closeMenu} className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')} style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', width: '100%', height: '100%', color: 'var(--color-text-primary)', textDecoration: 'none' }}>
            <NavIcon circle={{cx: "12", cy: "12", r: "10"}} polygon="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76" />
          </NavLink>

          {/* Add / Upload */}
          <Link to="/upload" onClick={closeMenu} style={{
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            width: '40px', height: '40px', backgroundColor: 'var(--color-accent)',
            borderRadius: '12px', border: '2px solid var(--color-border)',
            color: 'var(--color-text-primary)', textDecoration: 'none',
            boxShadow: '2px 2px 0 rgba(18,18,18,1)'
          }}>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <line x1="12" y1="5" x2="12" y2="19"></line>
              <line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
          </Link>

          {/* Notifications */}
          <button onClick={() => { closeMenu(); notificationState.toggleOpen(); }} className={notificationState.isOpen ? 'nav-link active' : 'nav-link'} style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', width: '100%', height: '100%', background: 'none', border: 'none', color: 'var(--color-text-primary)', position: 'relative' }}>
            <svg className="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
            </svg>
            {notificationState.unreadCount > 0 && (
              <span style={{ position: 'absolute', top: 'calc(50% - 16px)', right: 'calc(50% - 16px)', backgroundColor: '#ff4444', color: 'white', fontSize: '0.6rem', padding: '0.1rem 0.3rem', borderRadius: '10px', fontWeight: 'bold' }}>
                {notificationState.unreadCount > 99 ? '99+' : notificationState.unreadCount}
              </span>
            )}
          </button>

          {/* Menu */}
          <button onClick={() => setIsMoreMenuOpen(!isMoreMenuOpen)} className={isMoreMenuOpen ? 'nav-link active' : 'nav-link'} style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', width: '100%', height: '100%', background: 'none', border: 'none', color: 'var(--color-text-primary)' }}>
            <svg className="nav-icon" viewBox="0 0 24 24" fill="currentColor" stroke="none">
              <circle cx="12" cy="5" r="2"></circle>
              <circle cx="12" cy="12" r="2"></circle>
              <circle cx="12" cy="19" r="2"></circle>
            </svg>
          </button>
        </div>
      )}
    </>
  );
}
