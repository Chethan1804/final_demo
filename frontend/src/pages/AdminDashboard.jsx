import React, { useEffect, useState } from 'react';
import api from '../api/axios';
import { toast } from 'react-toastify';
import { Users, FileText, Trash2, RefreshCw, ChevronDown, ChevronRight, AlertTriangle } from 'lucide-react';

const AdminDashboard = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandedUser, setExpandedUser] = useState(null);
  const [userResumes, setUserResumes] = useState({});
  const [resumeLoading, setResumeLoading] = useState({});
  const [deleteModal, setDeleteModal] = useState(null); // { id, email }

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await api.get('/admin/users');
      setUsers(res.data);
    } catch {
      toast.error('Failed to load users.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchUsers(); }, []);

  const confirmDelete = async () => {
    if (!deleteModal) return;
    try {
      await api.delete(`/admin/users/${deleteModal.id}`);
      toast.success('User deleted.');
      setUsers(prev => prev.filter(u => u.id !== deleteModal.id));
      if (expandedUser === deleteModal.id) setExpandedUser(null);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Delete failed.');
    } finally {
      setDeleteModal(null);
    }
  };

  const toggleResumes = async (userId) => {
    if (expandedUser === userId) { setExpandedUser(null); return; }
    setExpandedUser(userId);
    if (userResumes[userId]) return;
    setResumeLoading(prev => ({ ...prev, [userId]: true }));
    try {
      const res = await api.get(`/admin/users/${userId}/resumes`);
      setUserResumes(prev => ({ ...prev, [userId]: res.data }));
    } catch {
      toast.error('Failed to load resumes.');
      setUserResumes(prev => ({ ...prev, [userId]: [] }));
    } finally {
      setResumeLoading(prev => ({ ...prev, [userId]: false }));
    }
  };

  const totalResumes = Object.values(userResumes).reduce((sum, r) => sum + (r?.length || 0), 0);

  return (
    <div className="container" style={{ padding: '2rem 0' }}>

      {/* Delete Modal */}
      {deleteModal && (
        <div style={{
          position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.6)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000
        }}>
          <div className="card" style={{ width: '100%', maxWidth: 420, padding: '2rem', textAlign: 'center' }}>
            <div style={{
              width: 48, height: 48, borderRadius: '50%',
              background: 'rgba(220,38,38,0.1)', display: 'flex',
              alignItems: 'center', justifyContent: 'center', margin: '0 auto 1rem'
            }}>
              <AlertTriangle size={22} color="#dc2626" />
            </div>
            <h3 style={{ fontWeight: 700, marginBottom: '0.5rem' }}>Delete User?</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '1.5rem' }}>
              This action cannot be undone. <strong>{deleteModal.email}</strong> and all their data will be permanently deleted.
            </p>
            <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'center' }}>
              <button
                className="btn btn-outline"
                style={{ flex: 1 }}
                onClick={() => setDeleteModal(null)}
              >Cancel</button>
              <button
                className="btn"
                style={{ flex: 1, background: '#dc2626', color: '#fff', border: 'none' }}
                onClick={confirmDelete}
              >Delete</button>
            </div>
          </div>
        </div>
      )}

      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '2rem', fontWeight: 'bold', marginBottom: '0.25rem' }}>Admin Dashboard</h1>
          <p style={{ color: 'var(--text-muted)' }}>Manage users and resumes</p>
        </div>
        <button className="btn btn-outline" onClick={fetchUsers} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <RefreshCw size={16} /> Refresh
        </button>
      </div>

      {/* Stats */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
        <div className="card" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ background: 'rgba(37,99,235,0.1)', padding: '0.75rem', borderRadius: '50%', color: 'var(--primary-color)' }}>
            <Users size={28} />
          </div>
          <div>
            <div style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>{users.length}</div>
            <div style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Total Users</div>
          </div>
        </div>
        <div className="card" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ background: 'rgba(16,185,129,0.1)', padding: '0.75rem', borderRadius: '50%', color: 'var(--success-color)' }}>
            <FileText size={28} />
          </div>
          <div>
            <div style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>{totalResumes}</div>
            <div style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Resumes Fetched</div>
          </div>
        </div>
      </div>

      {/* Users Table */}
      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <div style={{ padding: '1.25rem 1.5rem', borderBottom: '1px solid var(--border-color)' }}>
          <h2 style={{ fontWeight: '600', fontSize: '1.1rem' }}>All Users</h2>
        </div>

        {loading ? (
          <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>Loading users...</div>
        ) : users.length === 0 ? (
          <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>No users found.</div>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ background: 'var(--background-secondary, #f9fafb)' }}>
                {['ID', 'Email', 'Role', 'Resumes', 'Actions'].map(h => (
                  <th key={h} style={{ padding: '0.75rem 1.5rem', textAlign: 'left', fontSize: '0.8rem', fontWeight: '600', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {users.map((user, idx) => (
                <React.Fragment key={user.id}>
                  <tr style={{ borderTop: idx > 0 ? '1px solid var(--border-color)' : 'none' }}>
                    <td style={{ padding: '1rem 1.5rem', fontSize: '0.875rem', color: 'var(--text-muted)' }}>#{user.id}</td>
                    <td style={{ padding: '1rem 1.5rem', fontWeight: '500' }}>{user.email}</td>
                    <td style={{ padding: '1rem 1.5rem' }}>
                      <span style={{
                        padding: '0.2rem 0.6rem', borderRadius: '9999px', fontSize: '0.75rem', fontWeight: '600',
                        background: user.role === 'ROLE_ADMIN' ? 'rgba(220,38,38,0.1)' : 'rgba(37,99,235,0.1)',
                        color: user.role === 'ROLE_ADMIN' ? '#dc2626' : 'var(--primary-color)'
                      }}>
                        {(user.role || 'USER').replace('ROLE_', '')}
                      </span>
                    </td>
                    <td style={{ padding: '1rem 1.5rem' }}>
                      <button
                        onClick={() => toggleResumes(user.id)}
                        style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--primary-color)', fontSize: '0.875rem', padding: 0 }}
                      >
                        {expandedUser === user.id ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
                        View Resumes
                      </button>
                    </td>
                    <td style={{ padding: '1rem 1.5rem' }}>
                      <button
                        onClick={() => setDeleteModal({ id: user.id, email: user.email })}
                        style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', background: 'none', border: 'none', cursor: 'pointer', color: '#dc2626', fontSize: '0.875rem', padding: 0 }}
                      >
                        <Trash2 size={14} /> Delete
                      </button>
                    </td>
                  </tr>

                  {expandedUser === user.id && (
                    <tr style={{ background: 'var(--background-secondary, #f9fafb)' }}>
                      <td colSpan={5} style={{ padding: '0.75rem 1.5rem 1rem 3rem' }}>
                        {resumeLoading[user.id] ? (
                          <span style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Loading resumes...</span>
                        ) : !userResumes[user.id]?.length ? (
                          <span style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>No resumes found.</span>
                        ) : (
                          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                            {userResumes[user.id].map(resume => (
                              <div key={resume.id} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem' }}>
                                <FileText size={14} style={{ color: 'var(--text-muted)' }} />
                                <span style={{ fontWeight: '500' }}>{resume.title || 'Untitled Resume'}</span>
                                <span style={{ color: 'var(--text-muted)' }}>— updated {new Date(resume.updatedAt || resume.createdAt).toLocaleDateString()}</span>
                              </div>
                            ))}
                          </div>
                        )}
                      </td>
                    </tr>
                  )}
                </React.Fragment>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default AdminDashboard;