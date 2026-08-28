import { Link } from 'react-router-dom';

export default function HomePage() {
  return (
    <div className="home-page">
      <div className="home-hero">
        <h1 className="home-hero-title">CreatorVerse</h1>
        <p className="home-hero-subtitle">
          A creator-first social platform where creators publish, audiences discover, and brands eventually collaborate.
        </p>
        <div className="home-hero-actions">
          <Link to="/register" className="btn primary">Join the Platform</Link>
          <Link to="/feed" className="btn">Explore Content</Link>
        </div>
      </div>
    </div>
  );
}
