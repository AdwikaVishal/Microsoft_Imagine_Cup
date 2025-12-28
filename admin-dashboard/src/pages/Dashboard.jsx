import React from 'react';
import AlertCard from '../components/AlertCard';

/**
 * Dashboard Overview - High-level operational metrics and recent activity.
 * Designed for immediate situational awareness (SA).
 */
const Dashboard = ({ alerts, newAlertId }) => {
    const totalAlerts = alerts.length;
    const activeSOS = alerts.filter(a => (a.alertType === 'SOS' || a.status === 'Active') && a.status !== 'Resolved').length;
    const vulnerableUsers = alerts.filter(a => a.isVulnerable).length;
    const systemStatus = 'Operational';

    // Show top 4 most recent alerts for the live feed
    const recentAlerts = alerts.slice(0, 4);

    return (
        <div>
            <div style={{ marginBottom: '40px', borderBottom: '1px solid #E4E7EB', paddingBottom: '24px' }}>
                <h1 style={{ marginBottom: '8px' }}>Operational Overview</h1>
                <p style={{ color: '#6B7280', fontSize: '0.95rem', fontWeight: '500' }}>
                    Real-time system health and critical event tracking
                </p>
            </div>

            <div className="stats-grid">
                <div className="stat-card">
                    <h3>Total System Alerts</h3>
                    <div className="value">{totalAlerts}</div>
                </div>
                <div className="stat-card">
                    <h3>Active Emergency Signals</h3>
                    <div className="value" style={{ color: 'var(--status-active)' }}>{activeSOS}</div>
                </div>
                <div className="stat-card">
                    <h3>Vulnerable Users</h3>
                    <div className="value" style={{ color: '#92400E' }}>{vulnerableUsers}</div>
                </div>
                <div className="stat-card">
                    <h3>Network Status</h3>
                    <div className="value" style={{ color: 'var(--status-resolved)' }}>{systemStatus}</div>
                </div>
            </div>

            <div style={{ marginTop: '64px' }}>
                <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'baseline',
                    marginBottom: '24px'
                }}>
                    <h2>Live Incident Feed</h2>
                    <div style={{
                        fontSize: '0.8rem',
                        color: 'var(--text-muted)',
                        fontWeight: '600',
                        textTransform: 'uppercase',
                        letterSpacing: '0.05em'
                    }}>
                        Auto-refreshing every 8s
                    </div>
                </div>
                <div className="alerts-list">
                    {recentAlerts.map(alert => (
                        <AlertCard
                            key={alert.id}
                            alert={alert}
                            isNew={alert.id === newAlertId}
                        />
                    ))}
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
