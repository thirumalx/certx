import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    type Certificate,
    certificateService,
} from '../services/certificateService';
import { clientService, type Client } from '../services/clientService';
import { CertificateForm } from './CertificateForm';
import '../styles/CertificateManagement.css';

export function CertificateManagement() {
    const { applicationId, clientId } = useParams<{
        applicationId: string;
        clientId: string;
    }>();
    const navigate = useNavigate();
    const appId = Number(applicationId);
    const cId = Number(clientId);

    const [clientName, setClientName] = useState<string>('');
    const [certificates, setCertificates] = useState<Certificate[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [formError, setFormError] = useState<string | null>(null);
    const [showForm, setShowForm] = useState(false);
    const [editingCert, setEditingCert] = useState<Certificate | null>(null);
    const [formLoading, setFormLoading] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const [paging, setPaging] = useState({ page: 0, size: 10, total: 0 });
    const [activeFilter, setActiveFilter] = useState('ALL');
    const [expandedNotifications, setExpandedNotifications] = useState<Record<number, any[]>>({});
    const [loadingNotifications, setLoadingNotifications] = useState<Record<number, boolean>>({});

    // Load client info for the heading
    useEffect(() => {
        clientService.getClient(appId, cId).then((c) => {
            setClientName(c?.name ?? `Client #${cId}`);
        }).catch(() => {
            setClientName(`Client #${cId}`);
        });
    }, [appId, cId]);

    // Fetch certificate detail for this client using ownerName lookup
    const loadCertificates = async (page = 0, status = activeFilter) => {
        try {
            setLoading(true);
            setError(null);
            const data = await certificateService.listCertificates(appId, cId, page, paging.size, status);
            setCertificates(data.content);
            setPaging((prev) => ({ ...prev, page, total: data.totalElements }));
        } catch (err) {
            setError('Failed to load certificates');
            setCertificates([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadCertificates(0, activeFilter);
    }, [appId, cId, activeFilter]);

    const handleFormSubmit = async (certData: Certificate) => {
        try {
            setFormLoading(true);
            setError(null);
            setFormError(null);
            if (certData.id) {
                await certificateService.updateCertificate(appId, cId, certData.id, certData);
            } else {
                await certificateService.saveCertificate(appId, cId, {
                    ...certData,
                    clientId: cId,
                });
            }
            setShowForm(false);
            setEditingCert(null);
            loadCertificates(paging.page, activeFilter);
        } catch (err) {
            setFormError(err instanceof Error ? err.message : 'Failed to save certificate');
        } finally {
            setFormLoading(false);
        }
    };

    const handleDelete = async (id: number) => {
        if (!window.confirm('Are you sure you want to delete this certificate?')) return;
        try {
            setLoading(true);
            await certificateService.deleteCertificate(appId, cId, id);
            loadCertificates(paging.page, activeFilter);
        } catch (err) {
            setError('Failed to delete certificate');
        } finally {
            setLoading(false);
        }
    };

    const handleEdit = (cert: Certificate) => {
        setEditingCert(cert);
        setShowForm(true);
        setFormError(null);
    };

    const handleNotify = async (certId: number) => {
        try {
            setLoading(true);
            await clientService.notifyClient(cId, certId);
            alert('Notification sent successfully');
            loadCertificates(paging.page, activeFilter);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to send notification');
        } finally {
            setLoading(false);
        }
    };

    const toggleNotifications = async (certId: number) => {
        if (expandedNotifications[certId]) {
            setExpandedNotifications((prev) => {
                const next = { ...prev };
                delete next[certId];
                return next;
            });
            return;
        }

        try {
            setLoadingNotifications((prev) => ({ ...prev, [certId]: true }));
            const notifications = await certificateService.getNotifications(certId);
            setExpandedNotifications((prev) => ({ ...prev, [certId]: notifications }));
        } catch (err) {
            console.error('Failed to load notifications', err);
        } finally {
            setLoadingNotifications((prev) => ({ ...prev, [certId]: false }));
        }
    };

    const filtered = certificates.filter((c) => {
        const q = searchTerm.toLowerCase();
        return (
            (c.serialNumber ?? '').toLowerCase().includes(q) ||
            (c.path ?? '').toLowerCase().includes(q)
        );
    });

    const formatPath = (path: string) => {
        if (!path) return '—';
        const parts = path.split(/[\\/]/);
        if (parts.length <= 2) return path;
        const root = parts[0] || (path.startsWith('/') ? '/' : '');
        const fileName = parts[parts.length - 1];
        return `${root}${root.endsWith('\\') || root.endsWith('/') ? '' : (path.includes('\\') ? '\\' : '/')}...${path.includes('\\') ? '\\' : '/'}${fileName}`;
    };

    return (
        <div className="certificate-management">
            <div className="management-container">
                <aside className="left-nav">
                    <nav className="filter-nav">
                        <h3 className="filter-title">Filters</h3>
                        <ul className="filter-list">
                            <li key="ALL">
                                <button
                                    className={`filter-btn ${activeFilter === 'ALL' ? 'active' : ''}`}
                                    onClick={() => setActiveFilter('ALL')}
                                >
                                    All Certificates
                                </button>
                            </li>
                            <li key="ACTIVE">
                                <button
                                    className={`filter-btn ${activeFilter === 'ACTIVE' ? 'active' : ''}`}
                                    onClick={() => setActiveFilter('ACTIVE')}
                                >
                                    Active
                                </button>
                            </li>
                            <li key="EXPIRED">
                                <button
                                    className={`filter-btn ${activeFilter === 'EXPIRED' ? 'active' : ''}`}
                                    onClick={() => setActiveFilter('EXPIRED')}
                                >
                                    Expired
                                </button>
                            </li>
                            <li key="REVOKED">
                                <button
                                    className={`filter-btn ${activeFilter === 'REVOKED' ? 'active' : ''}`}
                                    onClick={() => setActiveFilter('REVOKED')}
                                >
                                    Revoked
                                </button>
                            </li>
                            <li key="DELETED">
                                <button
                                    className={`filter-btn ${activeFilter === 'DELETED' ? 'active' : ''}`}
                                    onClick={() => setActiveFilter('DELETED')}
                                >
                                    Deleted
                                </button>
                            </li>
                        </ul>

                    </nav>
                </aside>

                <main className="management-content">
                    <div className="management-header">
                        <div className="header-left">
                            <button
                                className="back-btn"
                                onClick={() => navigate(`/applications/${appId}/clients`)}
                            >
                                ← Clients
                            </button>
                            <div className="page-title-group">
                                <h2>Certificates</h2>
                                {clientName && <p className="page-subtitle">{clientName}</p>}
                            </div>
                        </div>

                        <div className="search-bar">
                            <input
                                type="text"
                                placeholder="Search by serial, path..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                className="search-input"
                            />
                            <span className="result-count">{filtered.length} certificate(s)</span>
                        </div>

                        <button
                            className="btn btn-primary"
                            onClick={() => {
                                setFormError(null);
                                setShowForm(true);
                            }}
                            disabled={loading}
                        >
                            + Add New Certificate
                        </button>
                    </div>

                    {!showForm && error && <div className="alert alert-error">{error}</div>}

                    {loading && !showForm ? (
                        <div className="loading">Loading certificates...</div>
                    ) : (
                        <div className="table-container">
                            <table className="certificates-table">
                                <thead>
                                    <tr>
                                        <th>Serial Number</th>
                                        <th>Path</th>
                                        <th>Issued On</th>
                                        <th>Not After</th>
                                        <th>Status</th>
                                        <th>Notifications</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {filtered.length > 0 ? (
                                        filtered.map((cert) => {
                                            const statusValue = (cert.status ?? '').toString().toUpperCase();
                                            const isDeleted = statusValue === '0' || statusValue === 'DELETED';
                                            const isRevoked = !isDeleted && (!!cert.revokedOn || statusValue === '2' || statusValue === 'REVOKED');
                                            const isExpired = !isDeleted && !isRevoked && (cert.notAfter ? new Date(cert.notAfter) < new Date() : false);
                                            const statusClass = isDeleted
                                                ? 'cert-deleted'
                                                : (isRevoked ? 'cert-revoked' : (isExpired ? 'cert-expired' : 'cert-active'));

                                            return (
                                                <React.Fragment key={cert.id}>
                                                    <tr
                                                        className={statusClass}
                                                    >
                                                        <td>{cert.serialNumber ?? '—'}</td>
                                                        <td className="mono-text" title={cert.path}>
                                                            {formatPath(cert.path)}
                                                        </td>
                                                        <td>
                                                            {cert.issuedOn
                                                                ? new Date(cert.issuedOn).toLocaleDateString()
                                                                : '—'}
                                                        </td>
                                                        <td className={isExpired ? 'expired-text' : ''}>
                                                            {cert.notAfter
                                                                ? new Date(cert.notAfter).toLocaleDateString()
                                                                : '—'}
                                                        </td>
                                                        <td>
                                                            <span
                                                                className={`cert-badge ${statusClass}`}
                                                            >
                                                                {isDeleted ? 'Deleted' : (isRevoked ? 'Revoked' : (isExpired ? 'Expired' : 'Active'))}
                                                            </span>
                                                        </td>
                                                        <td>
                                                            <button
                                                                className="btn btn-sm btn-link"
                                                                onClick={() => toggleNotifications(cert.id!)}
                                                            >
                                                                {loadingNotifications[cert.id!] ? '...' : (expandedNotifications[cert.id!] ? 'Hide' : 'View History')}
                                                            </button>
                                                        </td>
                                                        <td className="actions-cell">
                                                            <button
                                                                className="btn btn-sm btn-outline"
                                                                onClick={() => handleEdit(cert)}
                                                                disabled={loading}
                                                            >
                                                                View
                                                            </button>
                                                            {!isRevoked && !isDeleted && (
                                                                <>
                                                                    <button
                                                                        className="btn btn-sm btn-info"
                                                                        onClick={() => handleNotify(cert.id!)}
                                                                        disabled={loading}
                                                                        title="Send email notification"
                                                                    >
                                                                        Notify
                                                                    </button>
                                                                    <button
                                                                        className="btn btn-sm btn-danger"
                                                                        onClick={() => handleDelete(cert.id!)}
                                                                        disabled={loading}
                                                                    >
                                                                        Delete
                                                                    </button>
                                                                </>
                                                            )}
                                                        </td>
                                                    </tr>
                                                    {expandedNotifications[cert.id!] && (
                                                        <tr className="notifications-row">
                                                            <td colSpan={7}>
                                                                <div className="notifications-history">
                                                                    <h4>Notification History</h4>
                                                                    {expandedNotifications[cert.id!].length > 0 ? (
                                                                        <table className="mini-table">
                                                                            <thead>
                                                                                <tr>
                                                                                    <th>Sent At</th>
                                                                                    <th>Type</th>
                                                                                </tr>
                                                                            </thead>
                                                                            <tbody>
                                                                                {expandedNotifications[cert.id!].map((n: any) => (
                                                                                    <tr key={n.id}>
                                                                                        <td>{new Date(n.sentAt).toLocaleString()}</td>
                                                                                        <td>{n.remainderCount === 0 ? 'Standard Expiry Reminder' : `Reminder #${n.remainderCount}`}</td>
                                                                                    </tr>
                                                                                ))}
                                                                            </tbody>
                                                                        </table>
                                                                    ) : (
                                                                        <p className="no-notifications">No notifications sent yet.</p>
                                                                    )}
                                                                </div>
                                                            </td>
                                                        </tr>
                                                    )}
                                                </React.Fragment>
                                            );
                                        })
                                    ) : (
                                        <tr>
                                            <td colSpan={6} className="no-data">
                                                {searchTerm
                                                    ? 'No certificates match your search'
                                                    : 'No certificates yet — click "+ Issue Certificate" to create one'}
                                            </td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    )}

                    {!showForm && (
                        <div className="pagination">
                            <button
                                className="btn btn-sm btn-outline"
                                onClick={() => loadCertificates(paging.page - 1)}
                                disabled={paging.page === 0 || loading}
                            >
                                Previous
                            </button>

                            <div className="page-numbers">
                                {Array.from({ length: Math.min(5, Math.ceil(paging.total / paging.size)) }, (_, i) => {
                                    const totalPages = Math.ceil(paging.total / paging.size);
                                    let pageNum = i;
                                    if (totalPages > 5) {
                                        if (paging.page > 2) pageNum = paging.page - 2 + i;
                                        if (pageNum >= totalPages) pageNum = totalPages - 5 + i;
                                        if (pageNum < 0) pageNum = i;
                                    }

                                    if (pageNum >= totalPages) return null;

                                    return (
                                        <button
                                            key={pageNum}
                                            className={`page-btn ${paging.page === pageNum ? 'active' : ''}`}
                                            onClick={() => loadCertificates(pageNum)}
                                            disabled={loading}
                                        >
                                            {pageNum + 1}
                                        </button>
                                    );
                                })}
                            </div>

                            <button
                                className="btn btn-sm btn-outline"
                                onClick={() => loadCertificates(paging.page + 1)}
                                disabled={paging.page >= Math.ceil(paging.total / paging.size) - 1 || loading}
                            >
                                Next
                            </button>

                            <span className="page-info">
                                Page {paging.page + 1} of {Math.max(1, Math.ceil(paging.total / paging.size))} ({paging.total} items)
                            </span>
                        </div>
                    )}

                    {showForm && (
                        <CertificateForm
                            certificate={editingCert || undefined}
                            applicationId={appId}
                            clientId={cId}
                            defaultOwnerName={clientName}
                            onSubmit={handleFormSubmit}
                            onCancel={() => {
                                setShowForm(false);
                                setEditingCert(null);
                                setFormError(null);
                            }}
                            loading={formLoading}
                            readOnly={!!editingCert
                                && (!!editingCert.revokedOn
                                    || editingCert.status === '0'
                                    || editingCert.status === '2'
                                    || (editingCert.status ?? '').toString().toUpperCase() === 'DELETED'
                                    || (editingCert.status ?? '').toString().toUpperCase() === 'REVOKED')}
                            errorMessage={formError}
                        />
                    )}
                </main>
            </div>
        </div>
    );
}
