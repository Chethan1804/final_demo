import React, { useState } from 'react';
import { toast } from 'react-toastify';
import { Crown, X, Check, Loader } from 'lucide-react';
import api from '../api/axios';
import { loadRazorpayScript } from '../utils/razorpay';
import { useAuth } from '../context/AuthContext';

const PLAN_AMOUNT = 499; // INR
const RAZORPAY_KEY_ID = import.meta.env.VITE_RAZORPAY_KEY_ID || 'rzp_test_REPLACE_ME';

const PremiumUpgradeModal = ({ onClose, onSuccess }) => {
  const { user, updateUserRole } = useAuth();
  const [loading, setLoading] = useState(false);

  const handleUpgrade = async () => {
    setLoading(true);
    try {
      // 1. Load Razorpay SDK
      const loaded = await loadRazorpayScript();
      if (!loaded) {
        toast.error('Failed to load Razorpay. Check your internet connection.');
        setLoading(false);
        return;
      }

      // 2. Create order on backend
      const orderRes = await api.post('/payments/create-order', {
        amount: PLAN_AMOUNT,
        currency: 'INR',
        templateId: 'premium-monthly'
      });

      const { razorpayOrderId, amount, currency } = orderRes.data;

      // 3. Open Razorpay checkout modal
      const options = {
        key: RAZORPAY_KEY_ID,
        amount: amount * 100, // paise
        currency: currency,
        name: 'Smart Resume Builder',
        description: 'Premium Plan - 1 Month',
        order_id: razorpayOrderId,
        prefill: {
          email: user?.email || '',
        },
        theme: { color: '#2563eb' },
        handler: async (response) => {
          // 4. Verify payment on backend
          try {
            await api.post('/payments/verify', {
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            });

            // 5. Update local auth context — no need to re-login
            updateUserRole('ROLE_PREMIUM_USER');
            toast.success('🎉 You are now a Premium member!');
            onSuccess && onSuccess();
            onClose();
          } catch (err) {
            toast.error('Payment verification failed. Contact support.');
          }
        },
        modal: {
          ondismiss: () => {
            setLoading(false);
          }
        }
      };

      const rzp = new window.Razorpay(options);
      rzp.on('payment.failed', (response) => {
        toast.error(`Payment failed: ${response.error.description}`);
        setLoading(false);
      });
      rzp.open();

    } catch (err) {
      toast.error(err.response?.data?.error || 'Failed to initiate payment.');
      setLoading(false);
    }
  };

  return (
    <div style={{
      position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      zIndex: 1000, padding: '1rem'
    }}>
      <div className="card" style={{ maxWidth: '440px', width: '100%', position: 'relative', padding: '2rem' }}>
        {/* Close button */}
        <button
          onClick={onClose}
          style={{ position: 'absolute', top: '1rem', right: '1rem', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }}
        >
          <X size={20} />
        </button>

        {/* Header */}
        <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '0.75rem' }}>
            <div style={{ background: 'rgba(234,179,8,0.1)', padding: '1rem', borderRadius: '50%', color: '#ca8a04' }}>
              <Crown size={36} />
            </div>
          </div>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '0.25rem' }}>Upgrade to Premium</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Unlock AI resume analysis and more</p>
        </div>

        {/* Price */}
        <div style={{ textAlign: 'center', marginBottom: '1.5rem', padding: '1rem', background: 'rgba(37,99,235,0.05)', borderRadius: '8px', border: '1px solid rgba(37,99,235,0.1)' }}>
          <span style={{ fontSize: '2.5rem', fontWeight: 'bold', color: 'var(--primary-color)' }}>₹{PLAN_AMOUNT}</span>
          <span style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}> / month</span>
        </div>

        {/* Features */}
        <ul style={{ listStyle: 'none', padding: 0, marginBottom: '1.5rem', display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
          {[
            'AI-powered resume analysis',
            'Unlimited resume improvements',
            'Smart suggestions via Gemini AI',
            'PDF generation from AI output',
            'Priority support',
          ].map(f => (
            <li key={f} style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', fontSize: '0.9rem' }}>
              <Check size={16} style={{ color: '#16a34a', flexShrink: 0 }} />
              {f}
            </li>
          ))}
        </ul>

        {/* CTA */}
        <button
          onClick={handleUpgrade}
          disabled={loading}
          className="btn btn-primary"
          style={{ width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}
        >
          {loading ? (
            <><Loader size={16} style={{ animation: 'spin 1s linear infinite' }} /> Processing...</>
          ) : (
            <><Crown size={16} /> Pay ₹{PLAN_AMOUNT} & Upgrade</>
          )}
        </button>

        <p style={{ textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.75rem', marginTop: '0.75rem' }}>
          Secured by Razorpay · Cancel anytime
        </p>
      </div>
    </div>
  );
};

export default PremiumUpgradeModal;
