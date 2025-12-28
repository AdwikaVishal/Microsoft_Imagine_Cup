import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import StatusBadge from './StatusBadge';
import VulnerableBadge from './VulnerableBadge';

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

    return date.toLocaleDateString();
};

const AlertCard = ({ alert, isNew }) => {
    const navigate = useNavigate();
    const [relativeTime, setRelativeTime] = useState(formatRelativeTime(alert.timestamp));

    // Update relative time every minute
    useEffect(() => {
        const timer = setInterval(() => {
            setRelativeTime(formatRelativeTime(alert.timestamp));
        }, 60000); // 60 seconds

        // Also update immediately if alert object reference changes (e.g. status update)
        setRelativeTime(formatRelativeTime(alert.timestamp));

        return () => clearInterval(timer);
    }, [alert.timestamp]);

    const getAlertTypeIcon = (type) => {
        switch (type) {
            case 'SOS': return '🚨';
            case 'Injured': return '🩹';
            case 'Trapped': return '⛓️';
            case 'Safe': return '✅';
            default: return '❓';
        }
    };

    const getStatusClass = (status) => {
        const s = status.toLowerCase();
        if (s === 'active') return 'active-sos';
        return s;
    };

    // Pulse effect logic: Only for SOS and only when it's new
    const isSOSPulse = isNew && alert.alertType === 'SOS';

    return (
        <div
            className={`alert-card ${getStatusClass(alert.status)} ${isNew ? 'alert-new-highlight' : ''} ${isSOSPulse ? 'alert-sos-pulse' : ''}`}
            onClick={() => navigate(`/alerts/${alert.id}`)}
        >
            <div className="alert-info">
                <div className="alert-header">
                    <span style={{ fontSize: '1.25rem' }}>{getAlertTypeIcon(alert.alertType)}</span>
                    <div>
                        <div className="user-name">{alert.userName}</div>
                        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                            <span className="timestamp">{relativeTime}</span>
                            <span style={{ fontSize: '0.75rem', color: '#CBD5E0' }}>•</span>
                            <span style={{ fontSize: '0.75rem', color: '#6B7280' }}>{alert.alertType}</span>
                        </div>
                    </div>
                </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                {alert.isVulnerable && <VulnerableBadge category={alert.userCategory} />}
                <StatusBadge status={alert.status} />
            </div>
        </div>
    );
};

export default AlertCard;
