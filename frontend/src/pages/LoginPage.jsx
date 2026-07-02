import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { toast } from 'react-toastify';
import { FileText, Mail, Lock, KeyRound, ArrowLeft, Loader2 } from 'lucide-react';

const LoginPage = () => {
  const [formData, setFormData] = useState({ email: '', password: '', otp: '' });
  const [loading, setLoading] = useState(false);
  const [showOtp, setShowOtp] = useState(false);
  const { login, verifyOtp } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const redirectByRole = () => {
    const token = localStorage.getItem('token');
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      navigate(payload.role === 'ROLE_ADMIN' ? '/admin/dashboard' : '/dashboard');
    } catch {
      navigate('/dashboard');
    }
  };

  const handleSubmit = async (e) => {
    if (e?.preventDefault) e.preventDefault();
    setLoading(true);
    try {
      if (showOtp) {
        await verifyOtp({ email: formData.email, otp: formData.otp, password: formData.password });
        toast.success('Login successful!');
        redirectByRole();
      } else {
        const response = await login({ email: formData.email, password: formData.password });
        if (response?.requiresOtp) {
          setShowOtp(true);
          toast.info('OTP sent to your email.');
        } else {
          toast.success('Login successful!');
          redirectByRole();
        }
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Login failed. Check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container" style={{ background: 'linear-gradient(135deg, #0f172a 0%, #1e1b4b 50%, #0f172a 100%)' }}>
      {/* Back to home */}
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

      <div className="card auth-card" style={{ animation: 'fadeUp 0.4s ease' }}>
        <style>{`
          @keyframes fadeUp {
            from { opacity:0; transform:translateY(20px); }
            to   { opacity:1; transform:translateY(0); }
          }
        `}</style>

        {/* Header */}
        <div className="text-center mb-6">
          <div style={{
            width: 56, height: 56, borderRadius: 16,
            background: 'linear-gradient(135deg,#6366f1,#7c3aed)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            margin: '0 auto 1rem',
            boxShadow: '0 8px 24px rgba(99,102,241,0.35)',
          }}>
            <FileText size={26} color="#fff" />
          </div>
          <h1 style={{ fontSize: '1.6rem', fontWeight: 800, marginBottom: '0.25rem' }}>
            {showOtp ? 'Verify OTP' : 'Welcome back'}
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
            {showOtp ? `OTP sent to ${formData.email}` : 'Sign in to your account'}
          </p>
        </div>

        <form onSubmit={handleSubmit} noValidate>
          {!showOtp ? (
            <>
              <div className="form-group">
                <label className="form-label">Email</label>
                <div style={{ position: 'relative' }}>
                  <Mail size={16} style={{ position: 'absolute', left: '0.85rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                  <input
                    type="email" name="email" value={formData.email}
                    onChange={handleChange} className="form-input"
                    style={{ paddingLeft: '2.4rem' }}
                    placeholder="you@example.com" required
                  />
                </div>
              </div>
              <div className="form-group mb-6">
                <label className="form-label">Password</label>
                <div style={{ position: 'relative' }}>
                  <Lock size={16} style={{ position: 'absolute', left: '0.85rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                  <input
                    type="password" name="password" value={formData.password}
                    onChange={handleChange} className="form-input"
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
                  onChange={handleChange} className="form-input"
                  style={{ paddingLeft: '2.4rem', letterSpacing: '0.2em', fontSize: '1.1rem' }}
                  placeholder="· · · · · ·" maxLength="6" required
                />
              </div>
            </div>
          )}

          <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '0.75rem', fontSize: '0.95rem' }} disabled={loading}>
            {loading
              ? <><Loader2 size={16} style={{ animation: 'spin 1s linear infinite', marginRight: '0.5rem' }} /> Processing…</>
              : showOtp ? 'Verify & Login' : 'Sign In'
            }
          </button>
        </form>

        {!showOtp && (
          <p className="text-center mt-4" style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>
            No account?{' '}
            <Link to="/register" style={{ color: '#818cf8', fontWeight: 600 }}>Register here</Link>
          </p>
        )}

        {showOtp && (
          <button
            onClick={() => setShowOtp(false)}
            style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', width: '100%', marginTop: '0.75rem', fontSize: '0.85rem' }}
          >
            ← Back to login
          </button>
        )}
      </div>
    </div>
  );
};

export default LoginPage;