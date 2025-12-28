import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import StatusBadge from '../components/StatusBadge';
import VulnerableBadge from '../components/VulnerableBadge';
import { ChevronLeft, CheckCircle, Clock, MapPin, User, Info, MessageSquare } from 'lucide-react';

const formatRelativeTime = (timestamp) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);

    if (diffInSeconds < 60) return 'Updated just now';

    const diffInMinutes = Math.floor(diffInSeconds / 60);
    if (diffInMinutes < 60) {
        return `${diffInMinutes} ${diffInMinutes === 1 ? 'min' : 'mins'} ago`;
    }

    const diffInHours = Math.floor(diffInMinutes / 60);
    if (diffInHours < 24) {
        return `${diffInHours} ${diffInHours === 1 ? 'hr' : 'hrs'} ago`;
    }

    return date.toLocaleString();
};

const AlertDetail = ({ alerts }) => {
    const { id } = useParams();
    const navigate = useNavigate();
    const alert = alerts.find(a => a.id === id);
    const [relativeTime, setRelativeTime] = useState(alert ? formatRelativeTime(alert.timestamp) : '');

    useEffect(() => {
        if (!alert) return;

        const timer = setInterval(() => {
            setRelativeTime(formatRelativeTime(alert.timestamp));
        }, 60000);

        setRelativeTime(formatRelativeTime(alert.timestamp));

        return () => clearInterval(timer);
    }, [alert]);

    if (!alert) {
        return (
            <div style={{ textAlign: 'center', padding: '100px' }}>
                <h2>Alert Not Found</h2>
                <button className="btn btn-primary" onClick={() => navigate('/alerts')}>Back to Feed</button>
            </div>
        );
    }

    return (
        <div>
            <div
                onClick={() => navigate('/alerts')}
                style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    cursor: 'pointer',
                    color: '#6B7280',
                    marginBottom: '32px',
                    fontSize: '0.95rem',
                    fontWeight: '600',
                    transition: 'color 0.2s ease'
                }}
                onMouseEnter={(e) => e.currentTarget.style.color = '#1F2933'}
                onMouseLeave={(e) => e.currentTarget.style.color = '#6B7280'}
            >
                <ChevronLeft size={18} />
                Back to Emergency Feed
            </div>

            <div className="detail-container">
                <div className="detail-header">
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                        <div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '12px' }}>
                                <h1 style={{ margin: 0, fontSize: '1.85rem' }}>Incident Report: {alert.alertType}</h1>
                                <StatusBadge status={alert.status} />
                            </div>
                            <p style={{ color: '#6B7280', fontSize: '0.95rem', fontWeight: '500' }}>
                                System Reference: <span style={{ color: '#1F2933', fontWeight: '700' }}>#{alert.id}</span> •
                                {relativeTime}
                            </p>
                        </div>
                    </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '48px' }}>
                    <div>
                        <div className="detail-row">
                            <span className="detail-label"><User size={14} style={{ marginRight: '8px' }} /> User Information</span>
                            <span className="detail-value" style={{ fontSize: '1.25rem', fontWeight: '700' }}>{alert.userName}</span>
                        </div>
                        <div className="detail-row">
                            <span className="detail-label">Vulnerability Status</span>
                            <div style={{ marginTop: '12px' }}>
                                <span className="detail-value" style={{ display: 'flex', alignItems: 'center', gap: '12px', fontWeight: '600' }}>
                                    {alert.userCategory}
                                    {alert.isVulnerable && <VulnerableBadge category={alert.userCategory} />}
                                </span>
                            </div>
                        </div>
                        {alert.isVulnerable && (
                            <div className="detail-row">
                                <span className="detail-label"><MessageSquare size={14} style={{ marginRight: '8px' }} /> Communication Need</span>
                                <span className="detail-value" style={{ fontWeight: '700', color: '#92400E' }}>{alert.userCategory}</span>
                            </div>
                        )}
                    </div>
                    <div>
                        <div className="detail-row">
                            <span className="detail-label"><MapPin size={14} style={{ marginRight: '8px' }} /> Last Known Location</span>
                            <span className="detail-value" style={{ fontWeight: '600' }}>Sector A-12, Building 4 (Simulated GPS)</span>
                        </div>
                        <div className="detail-row">
                            <span className="detail-label"><Info size={14} style={{ marginRight: '8px' }} /> Classification</span>
                            <span className="detail-value" style={{ fontWeight: '600' }}>{alert.alertType} Emergency</span>
                        </div>
                    </div>
                </div>

                <div className="detail-row" style={{ marginTop: '32px' }}>
                    <span className="detail-label">Situation Description</span>
                    <div style={{
                        backgroundColor: '#F8FAFC',
                        padding: '24px',
                        borderRadius: '12px',
                        border: '1px solid #E2E8F0',
                        marginTop: '16px',
                        color: '#334155',
                        fontSize: '1.05rem',
                        lineHeight: '1.7'
                    }}>
                        {alert.description}
                    </div>
                </div>

                <div className="action-buttons">
                    <button className="btn btn-primary" onClick={() => window.alert('Incident Acknowledged. Dispatching team...')}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <Clock size={18} />
                            Acknowledge Dispatch
                        </div>
                    </button>
                    <button className="btn btn-success" onClick={() => window.alert('Incident Resolved. Logging report.')}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <CheckCircle size={18} />
                            Mark as Resolved
                        </div>
                    </button>
                    <button className="btn btn-outline" onClick={() => navigate('/alerts')}>Close Report</button>
                </div>
            </div>
        </div>
    );
};

export default AlertDetail;
