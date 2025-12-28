import React, { useState, useEffect } from 'react';

/**
 * SystemHealthStrip - A micro-UI component for the top of the dashboard.
 * Provides informational system status to emergency responders.
 * Operational only for demo purposes (static/mock sync timer).
 */
const SystemHealthStrip = () => {
    const [seconds, setSeconds] = useState(0);

    useEffect(() => {
        const timer = setInterval(() => {
            setSeconds(prev => (prev >= 59 ? 0 : prev + 1));
        }, 1000);
        return () => clearInterval(timer);
    }, []);

    return (
        <div style={{
            backgroundColor: '#F0F4F8',
            color: '#4A5568',
            fontSize: '0.7rem',
            padding: '4px 24px',
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            borderBottom: '1px solid #E2E8F0',
            letterSpacing: '0.025em',
            textTransform: 'uppercase',
            fontWeight: '600'
        }}>
            <span style={{ color: '#16A34A', display: 'flex', alignItems: 'center', gap: '4px' }}>
                <span style={{ fontSize: '10px' }}>●</span> System Operational
            </span>
            <span style={{ color: '#CBD5E0' }}>•</span>
            <span>Last sync {seconds}s ago</span>
        </div>
    );
};

export default SystemHealthStrip;
