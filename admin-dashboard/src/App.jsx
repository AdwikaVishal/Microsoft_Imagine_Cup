import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import SystemHealthStrip from './components/SystemHealthStrip';
import Dashboard from './pages/Dashboard';
import Alerts from './pages/Alerts';
import AlertDetail from './pages/AlertDetail';
import { mockAlerts as initialAlerts } from './data/mockAlerts';

function App() {
    const [alerts, setAlerts] = useState(initialAlerts);
    const [newAlertId, setNewAlertId] = useState(null);

    // Fake Live Update Logic
    useEffect(() => {
        const interval = setInterval(() => {
            const chance = Math.random();

            if (chance > 0.7) {
                // Option 1: Add a NEW alert
                const id = Math.floor(Math.random() * 10000).toString();
                const names = ['Alice Cooper', 'David Miller', 'Sophie Chen', 'Marcus Thorne'];
                const types = ['SOS', 'Injured', 'Trapped', 'Safe'];
                const categories = ['Blind', 'Deaf', 'Elderly', 'Normal'];

                const newAlert = {
                    id,
                    userName: names[Math.floor(Math.random() * names.length)],
                    alertType: types[Math.floor(Math.random() * types.length)],
                    userCategory: categories[Math.floor(Math.random() * categories.length)],
                    isVulnerable: Math.random() > 0.5,
                    timestamp: new Date().toISOString(),
                    status: 'Pending',
                    description: 'Simulated live emergency update. System detected unusual activity in sector ' + Math.floor(Math.random() * 100)
                };

                setAlerts(prev => [newAlert, ...prev]);
                setNewAlertId(id);

                // Remove highlight after 3 seconds
                setTimeout(() => setNewAlertId(null), 3000);

            } else {
                // Option 2: Change an EXISTING alert status (Pending -> Active -> Resolved)
                setAlerts(prev => {
                    if (prev.length === 0) return prev;
                    const indexToChange = Math.floor(Math.random() * prev.length);
                    const newAlerts = [...prev];
                    const alert = { ...newAlerts[indexToChange] };

                    if (alert.status === 'Pending') alert.status = 'Active';
                    else if (alert.status === 'Active') alert.status = 'Resolved';
                    else return prev;

                    alert.timestamp = new Date().toISOString();
                    newAlerts[indexToChange] = alert;
                    return newAlerts;
                });
            }
        }, 8000);

        return () => clearInterval(interval);
    }, []);

    return (
        <Router>
            <div style={{ display: 'flex', flexDirection: 'column', width: '100%', minHeight: '100vh' }}>
                <SystemHealthStrip />
                <Navbar />
                <main className="main-content">
                    <Routes>
                        <Route path="/" element={<Dashboard alerts={alerts} newAlertId={newAlertId} />} />
                        <Route path="/alerts" element={<Alerts alerts={alerts} newAlertId={newAlertId} />} />
                        <Route path="/alerts/:id" element={<AlertDetail alerts={alerts} />} />
                    </Routes>
                </main>
            </div>
        </Router>
    );
}

export default App;
