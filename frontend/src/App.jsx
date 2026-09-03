import { Routes, Route } from 'react-router-dom';
import AppShell from './components/layout/AppShell';
import ProtectedRoute from './components/layout/ProtectedRoute';

import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import FeedPage from './pages/FeedPage';
import UserProfilePage from './pages/UserProfilePage';
import ContentDetailPage from './pages/ContentDetailPage';
import UploadPage from './pages/UploadPage';
import MyContentPage from './pages/MyContentPage';
import CreatorDashboard from './pages/CreatorDashboard';
import BrandDashboard from './pages/BrandDashboard';

function App() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        {/* Public Routes */}
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/feed" element={<FeedPage />} />
        <Route path="/user/:id" element={<UserProfilePage />} />
        <Route path="/content/:id" element={<ContentDetailPage />} />
        
        {/* Placeholder Public Routes from Navbar */}
        <Route path="/creators" element={<div className="page-container"><h1 className="page-title">Creators</h1></div>} />
        <Route path="/campaigns" element={<div className="page-container"><h1 className="page-title">Campaigns</h1></div>} />

        {/* Protected Routes */}
        <Route element={<ProtectedRoute />}>
          <Route path="/upload" element={<UploadPage />} />
          <Route path="/my-content" element={<MyContentPage />} />
          <Route path="/dashboard/creator" element={<CreatorDashboard />} />
          <Route path="/dashboard/brand" element={<BrandDashboard />} />
        </Route>
      </Route>
    </Routes>
  );
}

export default App;
