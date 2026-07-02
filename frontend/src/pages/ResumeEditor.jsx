import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { toast } from 'react-toastify';
import { Save, ArrowLeft, Download } from 'lucide-react';

/* ── Helpers ── */
function parseJsonField(val) {
  if (!val) return '';
  if (typeof val !== 'string') return String(val);
  try {
    const parsed = JSON.parse(val);
    if (typeof parsed === 'string') return parsed;
    return JSON.stringify(parsed, null, 2);
  } catch { return val; }
}

function parseSummaryField(raw) {
  if (!raw) return { fullName: '', email: '', phone: '', summary: '' };
  const lines = raw.split('\n');
  let fullName = '', email = '', phone = '', summaryLines = [];
  let pastHeader = false, blankCount = 0;
  for (const line of lines) {
    if (!pastHeader) {
      if (line.startsWith('Name: ')) { fullName = line.slice(6).trim(); continue; }
      if (line.startsWith('Email: ')) { email = line.slice(7).trim(); continue; }
      if (line.startsWith('Phone: ')) { phone = line.slice(7).trim(); continue; }
      if (line.trim() === '') { blankCount++; if (blankCount >= 1) pastHeader = true; continue; }
    } else { summaryLines.push(line); }
  }
  return { fullName, email, phone, summary: summaryLines.join('\n').trim() };
}

/* ── Field ── */
const Field = ({ label, name, type = 'text', rows, placeholder, value, onChange, error }) => (
  <div className="form-group" style={{ marginBottom: '1.25rem' }}>
    <label className="form-label" style={{
      display: 'block', marginBottom: '0.4rem',
      color: 'var(--text-muted)', fontSize: '0.85rem', fontWeight: 600,
    }}>
      {label}
    </label>
    {rows ? (
      <textarea
        name={name} value={value} onChange={onChange}
        className="form-input" rows={rows} placeholder={placeholder}
        style={{ width: '100%', resize: 'vertical', borderColor: error ? 'var(--error-color)' : undefined }}
      />
    ) : (
      <input
        type={type} name={name} value={value} onChange={onChange}
        className="form-input" placeholder={placeholder}
        style={{ width: '100%', borderColor: error ? 'var(--error-color)' : undefined }}
      />
    )}
    {error && (
      <span style={{ color: 'var(--error-color)', fontSize: '0.78rem', marginTop: '0.25rem', display: 'block' }}>
        {error}
      </span>
    )}
  </div>
);

/* ── Validation ── */
function validate(f) {
  const e = {};
  if (!f.title.trim()) e.title = 'Title is required.';
  else if (f.title.trim().length < 3) e.title = 'Title must be at least 3 characters.';
  if (!f.fullName.trim()) e.fullName = 'Full name is required.';
  else if (f.fullName.trim().length < 3) e.fullName = 'Name must be at least 3 characters.';
  if (!f.email.trim()) e.email = 'Email is required.';
  else if (!/\S+@\S+\.\S+/.test(f.email)) e.email = 'Enter a valid email address.';
  if (f.phone) {
    const digits = f.phone.replace(/\D/g, '');
    if (digits.length < 10) e.phone = 'Phone must have at least 10 digits.';
    else if (digits.length > 13) e.phone = 'Phone number too long.';
  }
  if (!f.summary.trim()) e.summary = 'Professional summary is required.';
  else if (f.summary.trim().length < 20) e.summary = 'Summary must be at least 20 characters.';
  if (!f.skills.trim()) e.skills = 'Skills are required.';
  if (!f.experience.trim()) e.experience = 'Work experience is required.';
  if (!f.education.trim()) e.education = 'Education is required.';
  return e;
}

/* ── PDF builder ── */
function buildResumeHTML(f) {
  const esc = (s) => (s || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/\n/g,'<br/>');
  return `
    <div style="font-family:Georgia,serif;font-size:11pt;line-height:1.6;color:#111;padding:0;margin:0;">
      <h1 style="font-size:22pt;margin:0 0 4pt;">${esc(f.fullName)}</h1>
      <p style="color:#444;font-size:10pt;margin:0 0 16pt;">${[f.email, f.phone].filter(Boolean).map(esc).join(' · ')}</p>
      ${f.summary ? `<h2 style="font-size:12pt;text-transform:uppercase;letter-spacing:1px;border-bottom:1.5px solid #111;padding-bottom:3pt;margin:14pt 0 6pt;">Summary</h2><p style="margin:0;white-space:pre-wrap;">${esc(f.summary)}</p>` : ''}
      ${f.skills ? `<h2 style="font-size:12pt;text-transform:uppercase;letter-spacing:1px;border-bottom:1.5px solid #111;padding-bottom:3pt;margin:14pt 0 6pt;">Skills</h2><p style="margin:0;white-space:pre-wrap;">${esc(f.skills)}</p>` : ''}
      ${f.experience ? `<h2 style="font-size:12pt;text-transform:uppercase;letter-spacing:1px;border-bottom:1.5px solid #111;padding-bottom:3pt;margin:14pt 0 6pt;">Experience</h2><p style="margin:0;white-space:pre-wrap;">${esc(f.experience)}</p>` : ''}
      ${f.education ? `<h2 style="font-size:12pt;text-transform:uppercase;letter-spacing:1px;border-bottom:1.5px solid #111;padding-bottom:3pt;margin:14pt 0 6pt;">Education</h2><p style="margin:0;white-space:pre-wrap;">${esc(f.education)}</p>` : ''}
    </div>`;
}

/* ── Main component ── */
const ResumeEditor = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isNew = id === 'new';

  const [formData, setFormData] = useState({
    title: '', fullName: '', email: '', phone: '',
    summary: '', skills: '', experience: '', education: '',
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(!isNew);
  const [downloading, setDownloading] = useState(false);

  useEffect(() => {
    if (!isNew) fetchResume();
  }, [id]);

  const fetchResume = async () => {
    try {
      const response = await api.get(`/resumes/${id}`);
      const data = response.data.data || response.data;
      const { fullName, email, phone, summary } = parseSummaryField(data.summary);
      setFormData({
        title: data.title || '',
        fullName, email, phone, summary,
        skills: parseJsonField(data.skills),
        experience: parseJsonField(data.experience),
        education: parseJsonField(data.education),
      });
    } catch {
      toast.error('Failed to load resume. Showing empty form.');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = useCallback((e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    setErrors(prev => ({ ...prev, [name]: undefined }));
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = validate(formData);
    if (Object.keys(errs).length > 0) {
      setErrors(errs);
      toast.error('Please fix the errors before saving.');
      return;
    }
    setLoading(true);
    try {
      const toJsonStr = (val) => val ? JSON.stringify(val) : null;
      const payload = {
        title: formData.title,
        summary: `Name: ${formData.fullName}\nEmail: ${formData.email}\nPhone: ${formData.phone || ''}\n\n${formData.summary}`,
        skills: toJsonStr(formData.skills),
        experience: toJsonStr(formData.experience),
        education: toJsonStr(formData.education),
      };
      if (isNew) {
        await api.post('/resumes', payload);
        toast.success('Resume created!');
      } else {
        await api.put(`/resumes/${id}`, payload);
        toast.success('Resume updated!');
      }
      navigate('/resumes');
    } catch {
      toast.error('Failed to save resume.');
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadPDF = async () => {
    setDownloading(true);
    try {
      const html2pdf = (await import('html2pdf.js')).default;
      const el = document.createElement('div');
      el.innerHTML = buildResumeHTML(formData);
      await html2pdf().set({
        margin: [15, 15, 15, 15],
        filename: `${formData.title || 'resume'}.pdf`,
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: { scale: 2, useCORS: true },
        jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' },
      }).from(el).save();
      toast.success('PDF downloaded!');
    } catch (err) {
      toast.error('PDF generation failed.');
      console.error(err);
    } finally {
      setDownloading(false);
    }
  };

  if (loading && !isNew) return <div className="container mt-4">Loading editor...</div>;

  return (
    <div className="container" style={{ padding: '2rem 0', maxWidth: '800px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <button onClick={() => navigate('/resumes')} className="btn btn-outline"
          style={{ border: 'none', padding: '0.5rem 0', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <ArrowLeft size={18} /> Back to Resumes
        </button>
        {!isNew && (
          <button onClick={handleDownloadPDF} className="btn btn-outline" disabled={downloading}
            style={{ borderColor: 'var(--primary-color)', color: 'var(--primary-color)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Download size={18} /> {downloading ? 'Generating…' : 'Download PDF'}
          </button>
        )}
      </div>

      <div className="card">
        <h1 style={{ fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '1.75rem' }}>
          {isNew ? 'Create New Resume' : 'Edit Resume'}
        </h1>

        <form onSubmit={handleSubmit} noValidate>
          <Field label="Resume Title (e.g. Software Engineer)" name="title"
            placeholder="Software Engineer" value={formData.title}
            onChange={handleChange} error={errors.title} />

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <Field label="Full Name" name="fullName" placeholder="Jane Doe"
              value={formData.fullName} onChange={handleChange} error={errors.fullName} />
            <Field label="Email" name="email" type="email" placeholder="jane@example.com"
              value={formData.email} onChange={handleChange} error={errors.email} />
          </div>

          <Field label="Phone" name="phone" placeholder="+91 98765 43210"
            value={formData.phone} onChange={handleChange} error={errors.phone} />

          <div style={{ borderTop: '1px solid var(--border-color)', margin: '1.25rem 0' }} />

          <Field label="Professional Summary" name="summary" rows={4}
            placeholder="Brief intro — who you are and what you bring (min 20 chars)..."
            value={formData.summary} onChange={handleChange} error={errors.summary} />
          <Field label="Skills" name="skills" rows={3}
            placeholder="e.g. React, Node.js, PostgreSQL, Docker…"
            value={formData.skills} onChange={handleChange} error={errors.skills} />
          <Field label="Work Experience" name="experience" rows={7}
            placeholder={"Company · Role · Dates\n• Achievement one\n• Achievement two"}
            value={formData.experience} onChange={handleChange} error={errors.experience} />
          <Field label="Education" name="education" rows={3}
            placeholder={"B.Tech Computer Science · BITS Pilani · 2019–2023"}
            value={formData.education} onChange={handleChange} error={errors.education} />

          <div style={{ display: 'flex', gap: '1rem', marginTop: '0.5rem' }}>
            <button type="submit" className="btn btn-primary" disabled={loading}
              style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Save size={18} /> {loading ? 'Saving…' : 'Save Resume'}
            </button>
            {!isNew && (
              <button type="button" onClick={handleDownloadPDF} disabled={downloading}
                className="btn btn-outline"
                style={{ borderColor: 'var(--primary-color)', color: 'var(--primary-color)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Download size={18} /> {downloading ? 'Generating…' : 'Download PDF'}
              </button>
            )}
          </div>
        </form>
      </div>
    </div>
  );
};

export default ResumeEditor;