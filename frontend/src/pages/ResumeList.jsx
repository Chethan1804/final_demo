import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { FileText, PlusCircle, Trash2, Edit, Lock, Unlock, CheckCircle, X } from 'lucide-react';
import { toast } from 'react-toastify';
import { useAuth } from '../context/AuthContext';
import { loadRazorpayScript } from '../utils/razorpay';

/* ── Custom Delete Confirm Modal ── */
const DeleteModal = ({ onConfirm, onCancel }) => (
  <div style={{
    position: 'fixed', inset: 0, zIndex: 1000,
    background: 'rgba(0,0,0,0.55)', backdropFilter: 'blur(3px)',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
  }}>
    <div style={{
      background: 'var(--card-bg, #1e1e2e)',
      border: '1px solid var(--border-color, #333)',
      borderRadius: '1rem',
      padding: '2rem',
      maxWidth: '380px', width: '90%',
      boxShadow: '0 20px 60px rgba(0,0,0,0.4)',
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
        <h3 style={{ fontWeight: 700, fontSize: '1.1rem' }}>Delete Resume?</h3>
        <button onClick={onCancel} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }}>
          <X size={20} />
        </button>
      </div>
      <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem', fontSize: '0.9rem' }}>
        This action cannot be undone. The resume will be permanently deleted.
      </p>
      <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end' }}>
        <button
          onClick={onCancel}
          className="btn btn-outline"
          style={{ padding: '0.5rem 1.25rem' }}
        >
          Cancel
        </button>
        <button
          onClick={onConfirm}
          className="btn btn-primary"
          style={{ padding: '0.5rem 1.25rem', background: 'var(--error-color)', borderColor: 'var(--error-color)' }}
        >
          Delete
        </button>
      </div>
    </div>
  </div>
);

const ResumeList = () => {
  const [resumes, setResumes]                 = useState([]);
  const [loading, setLoading]                 = useState(true);
  const [processingPayment, setProcessingPayment] = useState(false);
  const [deleteTargetId, setDeleteTargetId]   = useState(null); // null = modal closed
  const { user, updateUserRole }              = useAuth();
  const navigate                              = useNavigate();

  useEffect(() => { fetchResumes(); }, []);

  const fetchResumes = async () => {
    try {
      const response = await api.get('/resumes');
      const data = response.data;
      const list = Array.isArray(data) ? data
        : Array.isArray(data?.content) ? data.content : [];
      setResumes(list);
    } catch {
      setResumes([]);
    } finally {
      setLoading(false);
    }
  };

  const confirmDelete = (id) => setDeleteTargetId(id);

  const handleDelete = async () => {
    const id = deleteTargetId;
    setDeleteTargetId(null);
    try {
      await api.delete(`/resumes/${id}`);
      toast.success('Resume deleted successfully');
      setResumes(prev => prev.filter(r => r.id !== id));
    } catch {
      toast.error('Failed to delete resume');
    }
  };

  const handleUnlockPremium = async () => {
    const res = await loadRazorpayScript();
    if (!res) { toast.error('Razorpay SDK failed to load. Are you online?'); return; }
    setProcessingPayment(true);
    try {
      const orderResponse = await api.post('/payments/create-order', {
        amount: 500.0, currency: 'INR', templateId: 'executive-template'
      });
      const orderData = orderResponse.data;
      const options = {
        key: import.meta.env.VITE_RAZORPAY_KEY_ID,
        amount: orderData.amount * 100,
        currency: orderData.currency,
        name: 'Resume Builder',
        description: 'Premium Template Unlock',
        order_id: orderData.razorpayOrderId,
        handler: async function (response) {
          try {
            await api.post('/payments/verify', {
              razorpayOrderId:   response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            });
            toast.success('Payment Successful! Premium features unlocked.');
            updateUserRole('PREMIUM_USER');
          } catch {
            toast.error('Payment verification failed. Please contact support.');
          }
        },
        prefill: { name: 'User', email: 'user@example.com', contact: '9999999999' },
        theme: { color: '#2563eb' },
      };
      const paymentObject = new window.Razorpay(options);
      paymentObject.on('payment.failed', () => toast.error('Payment failed or was cancelled.'));
      paymentObject.open();
    } catch {
      toast.error('Failed to initiate payment.');
    } finally {
      setProcessingPayment(false);
    }
  };

  const handleCreatePremium = () => {
    if (user?.role !== 'PREMIUM_USER') {
      toast.warning('You must unlock premium to use this template.');
      return;
    }
    navigate('/resumes/new?type=PREMIUM');
  };

  if (loading) return <div className="container mt-4">Loading resumes...</div>;

  return (
    <div className="container" style={{ padding: '2rem 0' }}>

      {/* Delete confirmation modal */}
      {deleteTargetId !== null && (
        <DeleteModal
          onConfirm={handleDelete}
          onCancel={() => setDeleteTargetId(null)}
        />
      )}

      <div className="flex justify-between items-center mb-6">
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold' }}>My Resumes</h1>
        <Link to="/resumes/new" className="btn btn-primary flex items-center gap-2">
          <PlusCircle size={18} /> Create Basic Resume
        </Link>
      </div>

      {/* Premium Templates */}
      <div style={{ marginBottom: '3rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '1rem' }}>Premium Templates</h2>
        <div className="card" style={{
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          background: 'linear-gradient(135deg, rgba(37,99,235,0.05), rgba(37,99,235,0.1))',
          border: '1px solid var(--primary-color)',
        }}>
          <div>
            <h3 style={{ fontWeight: 'bold', fontSize: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              Executive Template
              {user?.role === 'PREMIUM_USER'
                ? <Unlock size={18} color="var(--success-color)" />
                : <Lock size={18} color="var(--text-muted)" />}
            </h3>
            <p style={{ color: 'var(--text-muted)', marginTop: '0.5rem' }}>
              A highly professional template designed for senior roles. ATS friendly.
            </p>
          </div>
          <div>
            {user?.role === 'PREMIUM_USER' ? (
              <button onClick={handleCreatePremium} className="btn btn-primary flex items-center gap-2"
                style={{ backgroundColor: 'var(--success-color)' }}>
                <CheckCircle size={18} /> Use Template
              </button>
            ) : (
              <button onClick={handleUnlockPremium} disabled={processingPayment}
                className="btn btn-primary flex items-center gap-2">
                <Lock size={18} /> {processingPayment ? 'Processing...' : 'Unlock Premium (₹500)'}
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Resume list */}
      <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '1rem' }}>Your Created Resumes</h2>
      {resumes.length === 0 ? (
        <div className="card text-center" style={{ padding: '4rem 2rem' }}>
          <FileText size={48} style={{ margin: '0 auto 1rem', color: 'var(--text-muted)' }} />
          <h3 style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>No resumes found</h3>
          <p style={{ color: 'var(--text-muted)', marginBottom: '1rem' }}>Create your first resume to get started!</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1.5rem' }}>
          {resumes.map(resume => (
            <div key={resume.id} className="card" style={{ display: 'flex', flexDirection: 'column' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
                <div style={{ backgroundColor: 'var(--bg-color)', padding: '0.75rem', borderRadius: '0.5rem' }}>
                  <FileText size={24} color="var(--primary-color)" />
                </div>
                <div>
                  <h3 style={{ fontWeight: 'bold', fontSize: '1.1rem' }}>{resume.title}</h3>
                  <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Updated: {resume.lastUpdated}</p>
                </div>
              </div>
              <div style={{ marginTop: 'auto', display: 'flex', gap: '0.5rem', borderTop: '1px solid var(--border-color)', paddingTop: '1rem' }}>
                <Link to={`/resumes/${resume.id}`} className="btn btn-outline flex items-center gap-2" style={{ flex: 1 }}>
                  <Edit size={16} /> Edit
                </Link>
                <button
                  onClick={() => confirmDelete(resume.id)}
                  className="btn btn-outline"
                  style={{ color: 'var(--error-color)', borderColor: 'var(--error-color)' }}
                >
                  <Trash2 size={16} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ResumeList;