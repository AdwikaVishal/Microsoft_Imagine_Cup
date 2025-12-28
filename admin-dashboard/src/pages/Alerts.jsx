import React, { useState } from 'react';
import AlertCard from '../components/AlertCard';

/**
 * Alerts Page - Displays the full feed of emergency incidents.
 * Includes specialized operational filters (All, Active, Vulnerable).
 * Designed for calm scanning during high-stress scenarios.
 */
const Alerts = ({ alerts, newAlertId }) => {
    const [filter, setFilter] = useState('All');

    // Client-side filtering logic
    const filteredAlerts = alerts.filter(alert => {
        if (filter === 'All') return true;
        if (filter === 'Active') return alert.status === 'Active';
        if (filter === 'Vulnerable') return alert.isVulnerable === true;
        return true;
    });

    return (
        <div>
            <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'baseline',
                marginBottom: '40px',
                paddingBottom: '24px',
                borderBottom: '1px solid #E4E7EB'
            }}>
                <div>
                    <h1 style={{ marginBottom: '8px' }}>Emergency Alert Feed</h1>
                    <p style={{ color: '#6B7280', fontSize: '0.95rem', fontWeight: '500' }}>
                        Live monitoring of all system-detected incidents
                    </p>
                </div>

                {/* Filter Selection Tabs - Operational emphasis */}
                <div style={{
                    display: 'flex',
                    backgroundColor: '#F1F5F9',
                    padding: '4px',
                    borderRadius: '10px',
                    gap: '2px',
                    border: '1px solid #E2E8F0'
                }}>
                    {['All', 'Active', 'Vulnerable'].map(f => (
                        <button
                            key={f}
                            onClick={() => setFilter(f)}
                            style={{
                                padding: '8px 20px',
                                fontSize: '0.85rem',
                                borderRadius: '8px',
                                border: 'none',
                                cursor: 'pointer',
                                fontWeight: filter === f ? '700' : '600',
                                backgroundColor: filter === f ? 'white' : 'transparent',
                                color: filter === f ? '#2563EB' : '#64748B',
                                boxShadow: filter === f ? '0 2px 4px rgba(0,0,0,0.06)' : 'none',
                                transition: 'all 0.2s ease',
                                minWidth: '100px'
                            }}
                        >
                            {f}
                        </button>
                    ))}
                </div>
            </div>

            <div className="alerts-list">
                {filteredAlerts.length > 0 ? (
                    filteredAlerts.map(alert => (
                        <AlertCard
                            key={alert.id}
                            alert={alert}
                            isNew={alert.id === newAlertId}
                        />
                    ))
                ) : (
                    <div style={{
                        textAlign: 'center',
                        padding: '80px 40px',
                        color: '#64748B',
                        backgroundColor: 'white',
                        borderRadius: '16px',
                        border: '2px dashed #E2E8F0',
                        fontSize: '1.1rem',
                        fontWeight: '500'
                    }}>
                        No alerts currently match the "{filter}" criteria.
                    </div>
                )}
            </div>
        </div>
    );
};

export default Alerts;
