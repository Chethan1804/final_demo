import React, { useState } from 'react';
import api from '../api/axios';
import { toast } from 'react-toastify';
import { Wand2, Upload, AlertCircle } from 'lucide-react';

const AiFeature = () => {
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [insights, setInsights] = useState(null);

  const handleFileChange = (e) => setFile(e.target.files[0]);

  const handleProcess = async (e) => {
    e.preventDefault();
    if (!file) { toast.warning('Please select a PDF file.'); return; }

    const formData = new FormData();
    formData.append('file', file);
    setLoading(true);
    setInsights(null);

    try {
      const response = await api.post('/ai/extract', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });

      // Backend: ApiResponse<Map> → data is at response.data.data
      // If parse failed backend returns { raw: "..." } — handle both
      const payload = response.data;

      let parsed = payload;
      if (payload?.raw && typeof payload.raw === 'string') {
        // Backend parse failed, got raw JSON string — parse it here
        try {
          const clean = payload.raw
            .replace(/```json/g, '').replace(/```/g, '').trim();
          const start = clean.indexOf('{');
          const end = clean.lastIndexOf('}');
          parsed = JSON.parse(clean.substring(start, end + 1));
        } catch {
          toast.error('Could not parse AI response.');
          setLoading(false);
          return;
        }
      }

      setInsights(parsed);
      toast.success('AI processing complete!');
    } catch (error) {
      if (error.response?.status === 403) {
        toast.warning('AI Insights require a Premium account. Unlock Premium (₹500) to access.');
      } else {
        toast.error('Failed to process document.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container" style={{ padding: '2rem 0' }}>
      <div className="flex items-center gap-3 mb-6">
        <div style={{ backgroundColor: 'rgba(99,102,241,0.1)', padding: '0.75rem', borderRadius: '0.5rem', color: 'var(--primary-color)' }}>
          <Wand2 size={32} />
        </div>
        <div>
          <h1 style={{ fontSize: '2rem', fontWeight: 'bold' }}>AI Resume Insights</h1>
          <p style={{ color: 'var(--text-muted)' }}>Upload your resume to get instant, AI-driven feedback.</p>
        </div>
      </div>

      <div className="card mb-6" style={{ maxWidth: '600px' }}>
        <form onSubmit={handleProcess}>
          <div className="form-group">
            <label className="form-label flex items-center gap-2">
              <Upload size={18} /> Upload PDF Resume
            </label>
            <input type="file" accept=".pdf" onChange={handleFileChange}
              className="form-input" style={{ padding: '1rem' }} />
          </div>
          <button type="submit" className="btn btn-primary w-full mt-4" disabled={loading || !file}>
            {loading ? 'Processing…' : 'Generate Insights'}
          </button>
        </form>
      </div>

      {insights && (
        <div className="card" style={{ borderLeft: '4px solid var(--primary-color)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1.5rem' }}>
            <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>Your Resume Analysis</h2>
            {insights.score != null && (
              <span style={{
                background: 'var(--primary-color)', color: '#fff',
                borderRadius: '999px', padding: '0.2rem 0.8rem',
                fontWeight: 700, fontSize: '1rem'
              }}>
                Score: {insights.score}/100
              </span>
            )}
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1.5rem', marginBottom: '1.5rem' }}>
            <div style={{ padding: '1rem', backgroundColor: 'var(--bg-color)', borderRadius: '0.5rem' }}>
              <h3 style={{ fontWeight: 'bold', color: 'var(--success-color)', marginBottom: '0.5rem' }}>Strengths</h3>
              <ul style={{ paddingLeft: '1.5rem', color: 'var(--text-main)' }}>
                {insights.strengths?.map((s, i) => <li key={i} style={{ marginBottom: '0.25rem' }}>{s}</li>)}
              </ul>
            </div>
            <div style={{ padding: '1rem', backgroundColor: 'var(--bg-color)', borderRadius: '0.5rem' }}>
              <h3 style={{ fontWeight: 'bold', color: 'var(--error-color)', marginBottom: '0.5rem' }}>Areas to Improve</h3>
              <ul style={{ paddingLeft: '1.5rem', color: 'var(--text-main)' }}>
                {insights.weaknesses?.map((w, i) => <li key={i} style={{ marginBottom: '0.25rem' }}>{w}</li>)}
              </ul>
            </div>
          </div>

          {insights.suggestions && (
            <div style={{ padding: '1rem', backgroundColor: 'rgba(99,102,241,0.05)', borderRadius: '0.5rem', display: 'flex', gap: '1rem' }}>
              <AlertCircle color="var(--primary-color)" style={{ flexShrink: 0 }} />
              <div>
                <h3 style={{ fontWeight: 'bold', color: 'var(--primary-color)', marginBottom: '0.25rem' }}>AI Suggestion</h3>
                <p style={{ color: 'var(--text-main)', lineHeight: '1.6' }}>{insights.suggestions}</p>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default AiFeature;