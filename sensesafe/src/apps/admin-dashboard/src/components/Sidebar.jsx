import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Home, AlertTriangle, Settings, Users, BarChart3 } from 'lucide-react';

function Sidebar() {
    const location = useLocation();

    const menuItems = [
        { path: '/', icon: Home, label: 'Dashboard' },
        { path: '/alerts', icon: AlertTriangle, label: 'Alerts' },
        { path: '/users', icon: Users, label: 'Users' },
        { path: '/analytics', icon: BarChart3, label: 'Analytics' },
        { path: '/settings', icon: Settings, label: 'Settings' },
    ];

    return (
        <div className="bg-gray-800 text-white w-64 min-h-screen">
            <div className="p-4">
                <h2 className="text-xl font-bold">SenseSafe Admin</h2>
            </div>
            <nav className="mt-8">
                <ul>
                    {menuItems.map((item) => {
                        const Icon = item.icon;
                        const isActive = location.pathname === item.path;
                        return (
                            <li key={item.path} className="mb-2">
                                <Link
                                    to={item.path}
                                    className={`flex items-center px-4 py-2 text-sm font-medium rounded-lg mx-2 ${
                                        isActive
                                            ? 'bg-gray-700 text-white'
                                            : 'text-gray-300 hover:bg-gray-700 hover:text-white'
                                    }`}
                                >
                                    <Icon className="h-5 w-5 mr-3" />
                                    {item.label}
                                </Link>
                            </li>
                        );
                    })}
                </ul>
            </nav>
        </div>
    );
}

export default Sidebar;
