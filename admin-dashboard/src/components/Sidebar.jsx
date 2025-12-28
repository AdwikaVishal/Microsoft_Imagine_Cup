import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Bell, ShieldAlert } from 'lucide-react';

const Sidebar = () => {
    return (
        <div className="sidebar">
            <h2>SenseSafe</h2>
            <div className="sidebar-nav">
                <NavLink to="/" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`} end>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <LayoutDashboard size={18} />
                        Dashboard
                    </div>
                </NavLink>
                <NavLink to="/alerts" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <Bell size={18} />
                        Alert Feed
                    </div>
                </NavLink>
            </div>

            <div style={{ marginTop: 'auto', borderTop: '1px solid rgba(255,255,255,0.1)', paddingTop: '24px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px', color: '#6B7280', fontSize: '0.8rem' }}>
                    <ShieldAlert size={14} />
                    <span>SENSESAFE CORE V1.0</span>
                </div>
            </div>
        </div>
    );
};

export default Sidebar;
