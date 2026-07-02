import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FileText, ShieldCheck, User, Sparkles, ArrowRight } from 'lucide-react';

const LandingPage = () => {
  const navigate = useNavigate();
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const t = setTimeout(() => setVisible(true), 80);
    return () => clearTimeout(t);
  }, []);

  return (
    <div style={{
      minHeight: '100vh',
      background: 'linear-gradient(135deg, #0f172a 0%, #1e1b4b 50%, #0f172a 100%)',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '2rem',
      position: 'relative',
      overflow: 'hidden',
      fontFamily: "'Segoe UI', system-ui, sans-serif",
    }}>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Syne:wght@700;800&display=swap');

        .landing-fade {
          opacity: 0;
          transform: translateY(24px);
          transition: opacity 0.6s ease, transform 0.6s ease;
        }
        .landing-fade.visible {
          opacity: 1;
          transform: translateY(0);
        }
        .card-btn {
          background: rgba(255,255,255,0.04);
          border: 1px solid rgba(255,255,255,0.12);
          border-radius: 20px;
          padding: 2rem 1.75rem;
          cursor: pointer;
          transition: transform 0.22s ease, background 0.22s ease, border-color 0.22s ease, box-shadow 0.22s ease;
          text-align: left;
          color: #fff;
          width: 100%;
          max-width: 300px;
          display: flex;
          flex-direction: column;
          gap: 1rem;
          position: relative;
          overflow: hidden;
        }
        .card-btn::before {
          content: '';
          position: absolute;
          inset: 0;
          opacity: 0;
          transition: opacity 0.22s ease;
          border-radius: 20px;
        }
        .card-btn.user::before {
          background: linear-gradient(135deg, rgba(37,99,235,0.15), rgba(99,102,241,0.1));
        }
        .card-btn.admin::before {
          background: linear-gradient(135deg, rgba(220,38,38,0.15), rgba(239,68,68,0.1));
        }
        .card-btn:hover {
          transform: translateY(-4px);
          border-color: rgba(255,255,255,0.25);
          box-shadow: 0 20px 60px rgba(0,0,0,0.4);
        }
        .card-btn:hover::before { opacity: 1; }
        .card-btn.user:hover { box-shadow: 0 20px 60px rgba(37,99,235,0.25); }
        .card-btn.admin:hover { box-shadow: 0 20px 60px rgba(220,38,38,0.25); }

        .icon-wrap {
          width: 52px;
          height: 52px;
          border-radius: 14px;
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
        }
        .icon-wrap.user { background: rgba(37,99,235,0.2); color: #60a5fa; }
        .icon-wrap.admin { background: rgba(220,38,38,0.2); color: #f87171; }

        .arrow-icon {
          opacity: 0;
          transform: translateX(-6px);
          transition: opacity 0.2s, transform 0.2s;
          position: absolute;
          bottom: 1.5rem;
          right: 1.5rem;
          color: rgba(255,255,255,0.4);
        }
        .card-btn:hover .arrow-icon {
          opacity: 1;
          transform: translateX(0);
        }

        /* floating orbs */
        .orb {
          position: absolute;
          border-radius: 50%;
          filter: blur(80px);
          pointer-events: none;
          animation: drift 8s ease-in-out infinite alternate;
        }
        @keyframes drift {
          from { transform: translate(0, 0); }
          to   { transform: translate(20px, -20px); }
        }
        .badge {
          display: inline-flex;
          align-items: center;
          gap: 0.4rem;
          background: rgba(99,102,241,0.15);
          border: 1px solid rgba(99,102,241,0.3);
          border-radius: 9999px;
          padding: 0.3rem 0.8rem;
          font-size: 0.75rem;
          color: #a5b4fc;
          font-weight: 600;
          letter-spacing: 0.04em;
          text-transform: uppercase;
        }
      `}</style>

      {/* Background orbs */}
      <div className="orb" style={{ width: 400, height: 400, background: 'rgba(37,99,235,0.15)', top: '-10%', left: '-10%', animationDuration: '9s' }} />
      <div className="orb" style={{ width: 300, height: 300, background: 'rgba(139,92,246,0.12)', bottom: '5%', right: '-5%', animationDuration: '11s', animationDelay: '1s' }} />
      <div className="orb" style={{ width: 200, height: 200, background: 'rgba(220,38,38,0.08)', bottom: '30%', left: '10%', animationDuration: '7s', animationDelay: '0.5s' }} />

      {/* Content */}
      <div style={{ position: 'relative', zIndex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '3rem', width: '100%', maxWidth: '720px' }}>

        {/* Hero */}
        <div className={`landing-fade ${visible ? 'visible' : ''}`} style={{ textAlign: 'center', transitionDelay: '0s' }}>
          <div style={{ marginBottom: '1.25rem' }}>
            <span className="badge"><Sparkles size={12} /> AI-Powered</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.75rem', marginBottom: '1rem' }}>
            <div style={{ background: 'linear-gradient(135deg,#2563eb,#7c3aed)', padding: '0.6rem', borderRadius: '14px' }}>
              <FileText size={28} color="#fff" />
            </div>
            <h1 style={{
              fontFamily: "'Syne', sans-serif",
              fontSize: 'clamp(1.8rem, 5vw, 2.75rem)',
              fontWeight: 800,
              color: '#fff',
              margin: 0,
              letterSpacing: '-0.02em',
            }}>
              Resume<span style={{ color: '#818cf8' }}>Builder</span>
            </h1>
          </div>
          <p style={{ color: 'rgba(255,255,255,0.5)', fontSize: '1rem', maxWidth: '420px', margin: '0 auto', lineHeight: 1.6 }}>
            Build professional resumes with AI-powered suggestions, premium templates, and one-click PDF export.
          </p>
        </div>

        {/* Login cards */}
        <div
          className={`landing-fade ${visible ? 'visible' : ''}`}
          style={{
            display: 'flex',
            gap: '1.25rem',
            flexWrap: 'wrap',
            justifyContent: 'center',
            transitionDelay: '0.15s',
            width: '100%',
          }}
        >
          {/* User card */}
          <button className="card-btn user" onClick={() => navigate('/login')}>
            <div className="icon-wrap user"><User size={24} /></div>
            <div>
              <div style={{ fontWeight: '700', fontSize: '1.1rem', marginBottom: '0.35rem' }}>User Login</div>
              <div style={{ color: 'rgba(255,255,255,0.45)', fontSize: '0.85rem', lineHeight: 1.5 }}>
                Create & manage your resumes, access AI tools, export PDFs
              </div>
            </div>
            <ArrowRight size={18} className="arrow-icon" />
          </button>

          {/* Admin card */}
          <button className="card-btn admin" onClick={() => navigate('/admin/login')}>
            <div className="icon-wrap admin"><ShieldCheck size={24} /></div>
            <div>
              <div style={{ fontWeight: '700', fontSize: '1.1rem', marginBottom: '0.35rem' }}>Admin Login</div>
              <div style={{ color: 'rgba(255,255,255,0.45)', fontSize: '0.85rem', lineHeight: 1.5 }}>
                Manage all users, view resumes, control platform access
              </div>
            </div>
            <ArrowRight size={18} className="arrow-icon" />
          </button>
        </div>

        {/* Footer */}
        <div
          className={`landing-fade ${visible ? 'visible' : ''}`}
          style={{ color: 'rgba(255,255,255,0.2)', fontSize: '0.75rem', transitionDelay: '0.3s' }}
        >
          Smart Resume Builder • Powered by Gemini AI
        </div>
      </div>
    </div>
  );
};

export default LandingPage;