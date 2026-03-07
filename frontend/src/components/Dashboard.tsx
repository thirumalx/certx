import { useState, useEffect } from 'react';
import {
    PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip,
    BarChart, Bar, XAxis, YAxis, CartesianGrid
} from 'recharts';
import { dashboardService, type DashboardStats } from '../services/dashboardService';
import '../styles/Dashboard.css';

const COLORS = ['#3498db', '#e74c3c', '#2ecc71', '#f1c40f'];

export function Dashboard() {
    const [stats, setStats] = useState<DashboardStats | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const loadStats = async () => {
            try {
                setLoading(true);
                const data = await dashboardService.getStats();
                setStats(data);
            } catch (err) {
                setError('Failed to load dashboard statistics');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        loadStats();
    }, []);

    if (loading) return <div className="loading">Loading dashboard statistics...</div>;
    if (error) return <div className="alert alert-error">{error}</div>;
    if (!stats) return null;

    return (
        <div className="dashboard-container">
            <header className="dashboard-header">
                <h1>Dashboard</h1>
                <p>Real-time overview of your certificate ecosystem</p>
            </header>

            <div className="stats-grid">
                <div className="stat-card">
                    <span className="stat-value">{stats.totalApplications}</span>
                    <span className="stat-label">Applications</span>
                </div>
                <div className="stat-card">
                    <span className="stat-value">{stats.totalClients}</span>
                    <span className="stat-label">Clients</span>
                </div>
                <div className="stat-card">
                    <span className="stat-value">{stats.totalCertificates}</span>
                    <span className="stat-label">Certificates</span>
                </div>
            </div>

            <div className="charts-grid">
                <div className="chart-card">
                    <h3 className="chart-title">Certificate Status Distribution</h3>
                    <div className="chart-wrapper">
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie
                                    data={stats.statusDistribution}
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={60}
                                    outerRadius={80}
                                    paddingAngle={5}
                                    dataKey="count"
                                    nameKey="status"
                                    label
                                >
                                    {stats.statusDistribution.map((_, index) => (
                                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                    ))}
                                </Pie>
                                <Tooltip />
                                <Legend verticalAlign="bottom" height={36} />
                            </PieChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                <div className="chart-card">
                    <h3 className="chart-title">Upcoming Expirations (Timeline)</h3>
                    <div className="chart-wrapper">
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={stats.expiryStats}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                                <XAxis dataKey="month" fontSize={12} />
                                <YAxis allowDecimals={false} fontSize={12} />
                                <Tooltip />
                                <Bar dataKey="count" fill="#3498db" radius={[4, 4, 0, 0]} />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                <div className="chart-card" style={{ gridColumn: 'span 2' }}>
                    <h3 className="chart-title">Top Applications by Certificate Count</h3>
                    <div className="chart-wrapper">
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart
                                layout="vertical"
                                data={stats.topApplications}
                                margin={{ top: 5, right: 30, left: 40, bottom: 5 }}
                            >
                                <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                                <XAxis type="number" fontSize={12} />
                                <YAxis dataKey="name" type="category" width={100} fontSize={12} />
                                <Tooltip />
                                <Bar dataKey="count" fill="#2ecc71" radius={[0, 4, 4, 0]} />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            </div>
        </div>
    );
}
