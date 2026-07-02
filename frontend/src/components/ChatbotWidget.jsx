import React, { useState, useRef, useEffect } from 'react';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { MessageCircle, X, Send, Bot, User, Loader2, Minimize2 } from 'lucide-react';

const ChatbotWidget = () => {
  const { user } = useAuth();
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([
    { role: 'bot', text: "Hi! I'm your AI Resume Assistant. Ask me anything — how to improve your resume, tailor it for a job, or write a better summary." },
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const bottomRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    if (open) {
      bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
      inputRef.current?.focus();
    }
  }, [messages, open]);

  const send = async () => {
    const text = input.trim();
    if (!text || loading) return;

    setMessages((prev) => [...prev, { role: 'user', text }]);
    setInput('');
    setLoading(true);

    try {
      // baseURL is '/api' so path must be '/ai/generate' not '/api/ai/generate'
      const res = await api.post('/ai/generate', {
        userId: user?.id ?? 0,
        prompt: text,
      });
      // axios interceptor already unwraps ApiResponse → res.data = the inner data object
      const reply = res.data?.response || res.data?.data?.response || 'Sorry, no response generated.';
      setMessages((prev) => [...prev, { role: 'bot', text: reply }]);
    } catch (err) {
      const errMsg = err.response?.status === 403
        ? 'AI features require a Premium account. Upgrade to access the chatbot.'
        : 'Something went wrong. Please try again.';
      setMessages((prev) => [...prev, { role: 'bot', text: errMsg, error: true }]);
    } finally {
      setLoading(false);
    }
  };

  const handleKey = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send(); }
  };

  if (!user) return null;

  return (
    <>
      {!open && (
        <button onClick={() => setOpen(true)} aria-label="Open AI Chat"
          style={{ position:'fixed', bottom:'1.75rem', right:'1.75rem', zIndex:1000, width:'56px', height:'56px', borderRadius:'50%', background:'linear-gradient(135deg,var(--primary-color,#6366f1),#7c3aed)', border:'none', cursor:'pointer', display:'flex', alignItems:'center', justifyContent:'center', boxShadow:'0 4px 20px rgba(99,102,241,0.45)', color:'#fff', transition:'transform 0.2s,box-shadow 0.2s' }}
          onMouseEnter={(e) => { e.currentTarget.style.transform='scale(1.1)'; }}
          onMouseLeave={(e) => { e.currentTarget.style.transform='scale(1)'; }}
        >
          <MessageCircle size={24} />
        </button>
      )}

      {open && (
        <div style={{ position:'fixed', bottom:'1.75rem', right:'1.75rem', zIndex:1000, width:'360px', height:'520px', borderRadius:'16px', background:'var(--card-bg,#1e293b)', border:'1px solid var(--border-color)', boxShadow:'0 16px 48px rgba(0,0,0,0.4)', display:'flex', flexDirection:'column', overflow:'hidden', animation:'chatOpen 0.2s ease' }}>
          <style>{`
            @keyframes chatOpen { from{opacity:0;transform:scale(0.92) translateY(12px)} to{opacity:1;transform:scale(1) translateY(0)} }
            .chat-msg-bot{background:var(--bg-secondary,#0f172a);color:var(--text-main,#f1f5f9);}
            .chat-msg-user{background:linear-gradient(135deg,#6366f1,#7c3aed);color:#fff;}
            .chat-msg-error{background:rgba(248,113,113,0.1);color:var(--error-color,#f87171);}
            .chat-input{background:var(--bg-secondary,#0f172a)!important;color:var(--text-main,#f1f5f9)!important;border-color:var(--border-color)!important;}
            .chat-input:focus{outline:none;}
            .chat-send-btn:disabled{opacity:0.5;cursor:not-allowed;}
          `}</style>

          {/* Header */}
          <div style={{ padding:'1rem 1.25rem', background:'linear-gradient(135deg,#6366f1,#7c3aed)', color:'#fff', display:'flex', alignItems:'center', justifyContent:'space-between', flexShrink:0 }}>
            <div style={{ display:'flex', alignItems:'center', gap:'0.6rem' }}>
              <Bot size={20} />
              <div>
                <div style={{ fontWeight:700, fontSize:'0.95rem', lineHeight:1.2 }}>Resume AI</div>
                <div style={{ fontSize:'0.72rem', opacity:0.8 }}>Powered by Gemini</div>
              </div>
            </div>
            <div style={{ display:'flex', gap:'0.25rem' }}>
              <button onClick={() => setOpen(false)} style={{ background:'none', border:'none', color:'#fff', cursor:'pointer', padding:'0.25rem', borderRadius:'6px', opacity:0.8 }}><Minimize2 size={16} /></button>
              <button onClick={() => setOpen(false)} style={{ background:'none', border:'none', color:'#fff', cursor:'pointer', padding:'0.25rem', borderRadius:'6px', opacity:0.8 }}><X size={16} /></button>
            </div>
          </div>

          {/* Messages */}
          <div style={{ flex:1, overflowY:'auto', padding:'1rem', display:'flex', flexDirection:'column', gap:'0.75rem' }}>
            {messages.map((msg, i) => (
              <div key={i} style={{ display:'flex', alignItems:'flex-end', gap:'0.5rem', flexDirection: msg.role==='user' ? 'row-reverse' : 'row' }}>
                <div style={{ width:'28px', height:'28px', borderRadius:'50%', background: msg.role==='user' ? 'linear-gradient(135deg,#6366f1,#7c3aed)' : 'var(--bg-color,#0f172a)', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0, border:'1px solid var(--border-color)' }}>
                  {msg.role==='user' ? <User size={14} color="#fff" /> : <Bot size={14} color="var(--primary-color,#6366f1)" />}
                </div>
                <div className={`chat-msg-${msg.error ? 'error' : msg.role}`}
                  style={{ maxWidth:'78%', padding:'0.6rem 0.85rem', borderRadius: msg.role==='user' ? '14px 14px 4px 14px' : '14px 14px 14px 4px', fontSize:'0.875rem', lineHeight:1.55, whiteSpace:'pre-wrap', wordBreak:'break-word' }}>
                  {msg.text}
                </div>
              </div>
            ))}
            {loading && (
              <div style={{ display:'flex', alignItems:'flex-end', gap:'0.5rem' }}>
                <div style={{ width:'28px', height:'28px', borderRadius:'50%', background:'var(--bg-color)', display:'flex', alignItems:'center', justifyContent:'center', border:'1px solid var(--border-color)' }}>
                  <Bot size={14} color="var(--primary-color,#6366f1)" />
                </div>
                <div className="chat-msg-bot" style={{ padding:'0.6rem 0.85rem', borderRadius:'14px 14px 14px 4px', display:'flex', alignItems:'center', gap:'0.4rem', fontSize:'0.875rem' }}>
                  <Loader2 size={14} style={{ animation:'spin 1s linear infinite' }} /> Thinking…
                </div>
              </div>
            )}
            <div ref={bottomRef} />
          </div>

          {/* Input */}
          <div style={{ padding:'0.75rem 1rem', borderTop:'1px solid var(--border-color)', display:'flex', gap:'0.5rem', alignItems:'flex-end', flexShrink:0 }}>
            <textarea ref={inputRef} className="chat-input" value={input} onChange={(e) => setInput(e.target.value)} onKeyDown={handleKey}
              placeholder="Ask about your resume…" rows={1}
              style={{ flex:1, resize:'none', border:'1px solid var(--border-color)', borderRadius:'10px', padding:'0.55rem 0.75rem', fontSize:'0.875rem', fontFamily:'inherit', lineHeight:1.5, maxHeight:'96px', overflowY:'auto' }}
              onInput={(e) => { e.target.style.height='auto'; e.target.style.height=Math.min(e.target.scrollHeight,96)+'px'; }}
            />
            <button className="chat-send-btn" onClick={send} disabled={!input.trim()||loading} aria-label="Send"
              style={{ width:'38px', height:'38px', borderRadius:'10px', background:'linear-gradient(135deg,#6366f1,#7c3aed)', border:'none', cursor:'pointer', display:'flex', alignItems:'center', justifyContent:'center', color:'#fff', flexShrink:0 }}>
              <Send size={16} />
            </button>
          </div>
        </div>
      )}
    </>
  );
};

export default ChatbotWidget;