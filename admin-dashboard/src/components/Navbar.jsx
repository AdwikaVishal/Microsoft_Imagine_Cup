import React from 'react';
import { NavLink } from 'react-router-dom';

const Navbar = () => {
    return (
        <nav className="navbar">
            <div style={{ display: 'flex', alignItems: 'center', gap: '48px' }}>
                <div style={{
                    fontSize: '1.25rem',
                    fontWeight: '800',
                    color: '#1F2933',
                    letterSpacing: '-0.025em'
                }}>
                    SenseSafe Admin
                </div>

                <div className="nav-links">
                    <NavLink to="/" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`} end>
                        Dashboard
                    </NavLink>
                    <NavLink to="/alerts" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                        Alert Feed
                    </NavLink>
                </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '24px' }}>
                <div className="live-indicator">
                    <div className="dot-live"></div>
                    <span>Live Updates</span>
                </div>

                <div className="nav-user" style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <span style={{ fontSize: '0.85rem', color: '#6B7280', fontWeight: '500' }}>Responder #42</span>
                    <div style={{
                        width: '32px',
                        height: '32px',
                        backgroundColor: '#F3F4F6',
                        borderRadius: '50%',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '0.75rem',
                        fontWeight: '700',
                        color: '#4B5563',
                        border: '1px solid #E5E7EB'
                    }}>R1</div>
                </div>
            </div>
        </nav>
    );
};

export default Navbar;
