import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { FileText, LogOut, LayoutDashboard, Wand2, ShieldCheck } from 'lucide-react';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!user) return null;

  const isAdmin = user.role === 'ROLE_ADMIN';

  return (
    <nav style={{ background: 'var(--card-bg)', borderBottom: '1px solid var(--border-color)', padding: '1rem 0' }}>
      <div className="container flex justify-between items-center">
        <Link
          to="/"
          style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 'bold', fontSize: '1.25rem', color: 'var(--primary-color)' }}
        >
          <FileText size={24} />
          <span>ResumeBuilder</span>
        </Link>

        <div style={{ display: 'flex', gap: '1.5rem', alignItems: 'center' }}>
          <Link to="/" className="flex items-center gap-2" style={{ color: 'var(--text-muted)' }}>
            <LayoutDashboard size={18} />
            <span>Dashboard</span>
          </Link>

          <Link to="/resumes" className="flex items-center gap-2" style={{ color: 'var(--text-muted)' }}>
            <FileText size={18} />
            <span>Resumes</span>
          </Link>

          <Link to="/ai-feature" className="flex items-center gap-2" style={{ color: 'var(--text-muted)' }}>
            <Wand2 size={18} />
            <span>AI Tools</span>
          </Link>

          {/* Admin-only link */}
          {isAdmin && (
            <Link
              to="/admin/dashboard"
              className="flex items-center gap-2"
              style={{
                color: '#dc2626',
                fontWeight: '600',
                display: 'flex',
                alignItems: 'center',
                gap: '0.4rem',
              }}
            >
              <ShieldCheck size={18} />
              <span>Admin</span>
            </Link>
          )}

          <button
            onClick={handleLogout}
            className="btn btn-outline flex items-center gap-2"
            style={{ padding: '0.4rem 0.8rem' }}
          >
            <LogOut size={16} />
            <span>Logout</span>
          </button>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;