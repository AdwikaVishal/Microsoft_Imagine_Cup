import React from 'react';

const StatusBadge = ({ status }) => {
    const getStatusClass = () => {
        switch (status.toLowerCase()) {
            case 'active': return 'status-active';
            case 'pending': return 'status-pending';
            case 'resolved': return 'status-resolved';
            default: return '';
        }
    };

    return (
        <span className={`badge status-badge ${getStatusClass()}`}>
            {status}
        </span>
    );
};

export default StatusBadge;
