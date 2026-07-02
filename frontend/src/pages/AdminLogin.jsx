import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { toast } from 'react-toastify';
import { ShieldCheck, Mail, Lock, KeyRound, ArrowLeft, Loader2 } from 'lucide-react';

const AdminLogin = () => {
  const [formData, setFormData] = useState({ email: '', password: '', otp: '' });
  const [loading, setLoading] = useState(false);
  const [showOtp, setShowOtp] = useState(false);
  const { login, verifyOtp } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    if (e?.preventDefault) e.preventDefault();
    setLoading(true);
    try {
      if (showOtp) {
        await verifyOtp({ email: formData.email, otp: formData.otp, password: formData.password });
        const token = localStorage.getItem('token');
        const payload = JSON.parse(atob(token.split('.')[1]));
        if (payload.role === 'ROLE_ADMIN') {
          toast.success('Welcome, Admin!');
          navigate('/admin/dashboard');
        } else {
          toast.error('Access denied: not an admin account.');
          localStorage.removeItem('token');
          navigate('/');
        }
      } else {
        const response = await login({ email: formData.email, password: formData.password });
        if (response?.requiresOtp) {
          setShowOtp(true);
          toast.info('OTP sent to your email.');
        } else {
          const token = localStorage.getItem('token');
          const payload = JSON.parse(atob(token.split('.')[1]));
          if (payload.role === 'ROLE_ADMIN') {
            toast.success('Welcome, Admin!');
            navigate('/admin/dashboard');
          } else {
            toast.error('Access denied: not an admin account.');
            localStorage.removeItem('token');
            navigate('/');
          }
        }
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Login failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container" style={{ background: 'linear-gradient(135deg, #0f172a 0%, #2d0a0a 50%, #0f172a 100%)' }}>
      <Link to="/" style={{
        position: 'absolute', top: '1.5rem', left: '1.5rem',
        display: 'flex', alignItems: 'center', gap: '0.4rem',
        color: 'rgba(255,255,255,0.4)', fontSize: '0.85rem',
        transition: 'color 0.2s',
      }}
        onMouseEnter={e => e.currentTarget.style.color = 'rgba(255,255,255,0.8)'}
        onMouseLeave={e => e.currentTarget.style.color = 'rgba(255,255,255,0.4)'}
      >
        <ArrowLeft size={16} /> Back
      </Link>

      <div className="card auth-card" style={{
        animation: 'fadeUp 0.4s ease',
        borderColor: 'rgba(220,38,38,0.2)',
      }}>
        <style>{`
          @keyframes fadeUp {
            from { opacity:0; transform:translateY(20px); }
            to   { opacity:1; transform:translateY(0); }
          }
          .admin-input:focus {
            border-color: #dc2626 !important;
            box-shadow: 0 0 0 3px rgba(220,38,38,0.2) !important;
          }
          .admin-btn {
            background: linear-gradient(135deg, #dc2626, #b91c1c) !important;
            box-shadow: 0 4px 14px rgba(220,38,38,0.35) !important;
          }
          .admin-btn:hover {
            box-shadow: 0 6px 20px rgba(220,38,38,0.5) !important;
          }
        `}</style>

        {/* Header */}
        <div className="text-center mb-6">
          <div style={{
            width: 56, height: 56, borderRadius: 16,
            background: 'linear-gradient(135deg,#dc2626,#b91c1c)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            margin: '0 auto 1rem',
            boxShadow: '0 8px 24px rgba(220,38,38,0.35)',
          }}>
            <ShieldCheck size={26} color="#fff" />
          </div>
          <h1 style={{ fontSize: '1.6rem', fontWeight: 800, marginBottom: '0.25rem' }}>
            {showOtp ? 'Verify OTP' : 'Admin Portal'}
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
            {showOtp ? `OTP sent to ${formData.email}` : 'Restricted access — admins only'}
          </p>
        </div>

        <form onSubmit={handleSubmit} noValidate>
          {!showOtp ? (
            <>
              <div className="form-group">
                <label className="form-label">Admin Email</label>
                <div style={{ position: 'relative' }}>
                  <Mail size={16} style={{ position: 'absolute', left: '0.85rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                  <input
                    type="email" name="email" value={formData.email}
                    onChange={handleChange} className="form-input admin-input"
                    style={{ paddingLeft: '2.4rem' }}
                    placeholder="admin@example.com" required
                  />
                </div>
              </div>
              <div className="form-group mb-6">
                <label className="form-label">Password</label>
                <div style={{ position: 'relative' }}>
                  <Lock size={16} style={{ position: 'absolute', left: '0.85rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                  <input
                    type="password" name="password" value={formData.password}
                    onChange={handleChange} className="form-input admin-input"
                    style={{ paddingLeft: '2.4rem' }}
                    placeholder="••••••••" required
                  />
                </div>
              </div>
            </>
          ) : (
            <div className="form-group mb-6">
              <label className="form-label">One-Time Password</label>
              <div style={{ position: 'relative' }}>
                <KeyRound size={16} style={{ position: 'absolute', left: '0.85rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                <input
                  type="text" name="otp" value={formData.otp}
                  onChange={handleChange} className="form-input admin-input"
                  style={{ paddingLeft: '2.4rem', letterSpacing: '0.2em', fontSize: '1.1rem' }}
                  placeholder="· · · · · ·" maxLength="6" required
                />
              </div>
            </div>
          )}

          <button type="submit" className="btn btn-primary admin-btn" style={{ width: '100%', padding: '0.75rem', fontSize: '0.95rem' }} disabled={loading}>
            {loading
              ? <><Loader2 size={16} style={{ animation: 'spin 1s linear infinite', marginRight: '0.5rem' }} /> Processing…</>
              : showOtp ? 'Verify & Enter' : 'Admin Sign In'
            }
          </button>
        </form>

        {showOtp && (
          <button
            onClick={() => setShowOtp(false)}
            style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', width: '100%', marginTop: '0.75rem', fontSize: '0.85rem' }}
          >
            ← Back to login
          </button>
        )}

        <p className="text-center mt-4" style={{ fontSize: '0.8rem', color: 'rgba(255,255,255,0.2)' }}>
          Not an admin? <Link to="/login" style={{ color: 'rgba(255,255,255,0.35)' }}>User login</Link>
        </p>
      </div>
    </div>
  );
};

export default AdminLogin;