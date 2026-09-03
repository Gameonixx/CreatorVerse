import { useState } from 'react';
import { NavLink, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function Navbar() {
  const { user, logout, hasCreatorMode, hasBrandMode } = useAuth();
  const navigate = useNavigate();
  const [isMoreMenuOpen, setIsMoreMenuOpen] = useState(false);
  const [isContextDropdownOpen, setIsContextDropdownOpen] = useState(false);

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
        
        <div className="navbar-links">
          <NavLink to="/" onClick={closeMenu} className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')} aria-label="Home">
            <NavIcon path="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" polyline="9 22 9 12 15 12 15 22" />
            <span className="nav-text">Home</span>
          </NavLink>
          <NavLink to="/feed" onClick={closeMenu} className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')} aria-label="Explore">
            <NavIcon circle={{cx: "12", cy: "12", r: "10"}} polygon="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76" />
            <span className="nav-text">Explore</span>
          </NavLink>

          
          <NavLink to="/creators" onClick={closeMenu} className={({ isActive }) => (isActive ? 'nav-link active desktop-only' : 'nav-link desktop-only')} aria-label="Creators">
            <span className="nav-text">Creators</span>
          </NavLink>
          <NavLink to="/campaigns" onClick={closeMenu} className={({ isActive }) => (isActive ? 'nav-link active desktop-only' : 'nav-link desktop-only')} aria-label="Campaigns">
            <span className="nav-text">Campaigns</span>
          </NavLink>
          
          <button className={`nav-link mobile-only-nav ${isMoreMenuOpen ? 'active' : ''}`} onClick={() => setIsMoreMenuOpen(!isMoreMenuOpen)} aria-label="More" style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}>
            <svg className="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="12" cy="12" r="1.5"/><circle cx="19" cy="12" r="1.5"/><circle cx="5" cy="12" r="1.5"/>
            </svg>
            <span className="nav-text">More</span>
          </button>
        </div>

        <div className="navbar-actions desktop-only">
          {user ? (
            <>
              <Link to="/upload" className="btn primary">Upload</Link>
              <Link to="/my-content" className="btn">My Content</Link>
              
              {hasCreatorMode && !hasBrandMode && (
                <Link to="/dashboard/creator" className="btn" title="Creator Dashboard">Creator Mode</Link>
              )}
              {!hasCreatorMode && hasBrandMode && (
                <Link to="/dashboard/brand" className="btn" title="Brand Dashboard">Brand Mode</Link>
              )}
              {hasCreatorMode && hasBrandMode && (
                <div style={{ position: 'relative' }}>
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
              
              <button onClick={handleLogout} className="btn">Log Out</button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn">Log In</Link>
              <Link to="/register" className="btn primary">Sign Up</Link>
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
    </>
  );
}
