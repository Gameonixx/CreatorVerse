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
import CreatorDiscoveryPage from './pages/CreatorDiscoveryPage';
import CampaignDiscoveryPage from './pages/CampaignDiscoveryPage';
import CampaignDetailPage from './pages/CampaignDetailPage';
import CollaborationDetailPage from './pages/CollaborationDetailPage';

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
        
        {/* Public Routes from Navbar */}
        <Route path="/creators" element={<CreatorDiscoveryPage />} />
        <Route path="/campaigns" element={<CampaignDiscoveryPage />} />
        <Route path="/campaigns/:id" element={<CampaignDetailPage />} />

        {/* Protected Routes */}
        <Route element={<ProtectedRoute />}>
          <Route path="/upload" element={<UploadPage />} />
          <Route path="/my-content" element={<MyContentPage />} />
          <Route path="/dashboard/creator" element={<CreatorDashboard />} />
          <Route path="/dashboard/brand" element={<BrandDashboard />} />
          <Route path="/collaborations/:id" element={<CollaborationDetailPage />} />
        </Route>
      </Route>
    </Routes>
  );
}

export default App;
