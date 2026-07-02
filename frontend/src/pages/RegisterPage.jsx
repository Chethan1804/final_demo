import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { toast } from 'react-toastify';
import { FileText, User, Mail, Lock, ArrowLeft, Loader2 } from 'lucide-react';

const RegisterPage = () => {
  const [formData, setFormData] = useState({ name: '', email: '', password: '' });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const validate = () => {
    const e = {};
    if (!formData.name || formData.name.trim().length < 3) e.name = 'Min 3 chars';
    if (!/\S+@\S+\.\S+/.test(formData.email)) e.email = 'Invalid email';
    if (!formData.password || formData.password.length < 6) e.password = 'Min 6 chars';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async (e) => {
    if (e?.preventDefault) e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    try {
      await register(formData);
      toast.success('Registered! Please log in.');
      navigate('/login');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  const field = (label, name, type, icon, placeholder, extra = {}) => (
    <div className="form-group">
      <label className="form-label">{label}</label>
      <div style={{ position: 'relative' }}>
        {React.cloneElement(icon, {
          style: { position: 'absolute', left: '0.85rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }
        })}
        <input
          type={type} name={name} value={formData[name]}
          onChange={handleChange} className="form-input"
          style={{ paddingLeft: '2.4rem' }}
          placeholder={placeholder} required
          {...extra}
        />
      </div>
      {errors[name] && <p style={{ color: '#f87171', fontSize: '0.75rem', marginTop: '0.25rem' }}>{errors[name]}</p>}
    </div>
  );

  return (
    <div className="auth-container" style={{ background: 'linear-gradient(135deg, #0f172a 0%, #1e1b4b 50%, #0f172a 100%)' }}>
      <Link to="/" style={{
        position: 'absolute', top: '1.5rem', left: '1.5rem',
        display: 'flex', alignItems: 'center', gap: '0.4rem',
        color: 'rgba(255,255,255,0.4)', fontSize: '0.85rem', transition: 'color 0.2s',
      }}
        onMouseEnter={e => e.currentTarget.style.color = 'rgba(255,255,255,0.8)'}
        onMouseLeave={e => e.currentTarget.style.color = 'rgba(255,255,255,0.4)'}
      >
        <ArrowLeft size={16} /> Back
      </Link>

      <div className="card auth-card" style={{ animation: 'fadeUp 0.4s ease' }}>
        <style>{`@keyframes fadeUp { from{opacity:0;transform:translateY(20px)} to{opacity:1;transform:translateY(0)} }`}</style>

        <div className="text-center mb-6">
          <div style={{
            width: 56, height: 56, borderRadius: 16,
            background: 'linear-gradient(135deg,#6366f1,#7c3aed)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            margin: '0 auto 1rem', boxShadow: '0 8px 24px rgba(99,102,241,0.35)',
          }}>
            <FileText size={26} color="#fff" />
          </div>
          <h1 style={{ fontSize: '1.6rem', fontWeight: 800, marginBottom: '0.25rem' }}>Create account</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Start building your resume</p>
        </div>

        <form onSubmit={handleSubmit} noValidate>
          {field('Username', 'name', 'text', <User size={16} />, 'John Doe')}
          {field('Email', 'email', 'email', <Mail size={16} />, 'you@example.com')}
          <div className="mb-6">
            {field('Password', 'password', 'password', <Lock size={16} />, '••••••••')}
          </div>
          <button type="submit" className="btn btn-primary"
            style={{ width: '100%', padding: '0.75rem', fontSize: '0.95rem' }} disabled={loading}>
            {loading
              ? <><Loader2 size={16} style={{ animation: 'spin 1s linear infinite', marginRight: '0.5rem' }} /> Registering…</>
              : 'Create Account'}
          </button>
        </form>

        <p className="text-center mt-4" style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>
          Have account?{' '}
          <Link to="/login" style={{ color: '#818cf8', fontWeight: 600 }}>Log in</Link>
        </p>
      </div>
    </div>
  );
};

export default RegisterPage;