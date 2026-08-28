import { Outlet } from 'react-router-dom';
import Navbar from '../navigation/Navbar';
import '../../styles/layout.css';

export default function AppShell() {
  return (
    <div className="app-shell">
      <Navbar />
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}
