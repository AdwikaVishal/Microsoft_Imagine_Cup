import React from 'react';

const VulnerableBadge = ({ category }) => {
    return (
        <span className="badge badge-vulnerable">
            ⚠️ Vulnerable {category ? `· ${category}` : ''}
        </span>
    );
};

export default VulnerableBadge;
