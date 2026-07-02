import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { FileText, Wand2, PlusCircle, Activity, Crown } from 'lucide-react';
import PremiumUpgradeModal from '../components/PremiumUpgradeModal';

const Dashboard = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState({ resumes: 0 });
  const [loading, setLoading] = useState(true);
  const [showUpgradeModal, setShowUpgradeModal] = useState(false);

  const isPremium = user?.role === 'ROLE_PREMIUM_USER' || user?.role === 'ROLE_ADMIN';

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const res = await api.get('/resumes');
        const data = res.data;
        const count = data?.totalElements ?? (Array.isArray(data) ? data.length : 0);
        setStats({ resumes: count });
      } catch {
        setStats({ resumes: 0 });
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, []);

  if (loading) {
    return <div className="container mt-4">Loading dashboard...</div>;
  }

  return (
    <div className="container" style={{ padding: '2rem 0' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '2rem', fontWeight: 'bold', marginBottom: '0.5rem' }}>Dashboard</h1>
          <p style={{ color: 'var(--text-muted)' }}>Welcome back, {user?.email}</p>
        </div>

        {/* Premium badge or upgrade button */}
        {isPremium ? (
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', padding: '0.4rem 0.9rem', background: 'rgba(234,179,8,0.1)', borderRadius: '9999px', color: '#ca8a04', fontWeight: '600', fontSize: '0.875rem' }}>
            <Crown size={16} /> Premium Member
          </div>
        ) : (
          <button
            onClick={() => setShowUpgradeModal(true)}
            className="btn"
            style={{ background: 'linear-gradient(135deg,#ca8a04,#eab308)', color: '#fff', border: 'none', display: 'flex', alignItems: 'center', gap: '0.4rem' }}
          >
            <Crown size={16} /> Upgrade to Premium
          </button>
        )}
      </div>

      {/* Stats cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
        <div className="card" style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
          <div style={{ backgroundColor: 'rgba(37,99,235,0.1)', padding: '1rem', borderRadius: '50%', color: 'var(--primary-color)' }}>
            <FileText size={32} />
          </div>
          <div>
            <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>{stats.resumes}</h2>
            <p style={{ color: 'var(--text-muted)' }}>Total Resumes</p>
          </div>
        </div>

        <div className="card" style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
          <div style={{ backgroundColor: isPremium ? 'rgba(234,179,8,0.1)' : 'rgba(100,116,139,0.1)', padding: '1rem', borderRadius: '50%', color: isPremium ? '#ca8a04' : 'var(--text-muted)' }}>
            <Crown size={32} />
          </div>
          <div>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>{isPremium ? 'Active' : 'Free Tier'}</h2>
            <p style={{ color: 'var(--text-muted)' }}>Plan Status</p>
          </div>
        </div>
      </div>

      {/* Quick actions */}
      <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1rem' }}>Quick Actions</h2>
      <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
        <Link to="/resumes/new" className="btn btn-primary" style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
          <PlusCircle size={18} /> Create New Resume
        </Link>

        {isPremium ? (
          <Link to="/ai-feature" className="btn btn-outline" style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <Wand2 size={18} /> AI Resume Analysis
          </Link>
        ) : (
          <button
            onClick={() => setShowUpgradeModal(true)}
            className="btn btn-outline"
            style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}
          >
            <Wand2 size={18} /> AI Analysis (Premium)
          </button>
        )}
      </div>

      {/* Upgrade modal */}
      {showUpgradeModal && (
        <PremiumUpgradeModal
          onClose={() => setShowUpgradeModal(false)}
          onSuccess={() => setShowUpgradeModal(false)}
        />
      )}
    </div>
  );
};

export default Dashboard;
