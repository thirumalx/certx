import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    type Certificate,
    certificateService,
} from '../services/certificateService';
import { clientService } from '../services/clientService';
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
    const [showForm, setShowForm] = useState(false);
    const [editingCert, setEditingCert] = useState<Certificate | null>(null);
    const [formLoading, setFormLoading] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const [paging, setPaging] = useState({ page: 0, size: 10, total: 0 });

    // Load client info for the heading
    useEffect(() => {
        clientService.getClient(appId, cId).then((c) => {
            setClientName(c?.name ?? `Client #${cId}`);
        }).catch(() => {
            setClientName(`Client #${cId}`);
        });
    }, [appId, cId]);

    // Fetch certificate detail for this client using ownerName lookup
    const loadCertificates = async (page = 0) => {
        try {
            setLoading(true);
            setError(null);
            const data = await certificateService.listCertificates(appId, cId, page, paging.size);
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
        loadCertificates();
    }, [appId, cId]);

    const handleFormSubmit = async (certData: Certificate) => {
        try {
            setFormLoading(true);
            setError(null);
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
            loadCertificates(paging.page);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to save certificate');
        } finally {
            setFormLoading(false);
        }
    };

    const handleRevoke = async (id: number) => {
        if (!window.confirm('Are you sure you want to revoke this certificate?')) return;
        try {
            setLoading(true);
            await certificateService.deleteCertificate(appId, cId, id);
            loadCertificates(paging.page);
        } catch (err) {
            setError('Failed to revoke certificate');
        } finally {
            setLoading(false);
        }
    };

    const handleEdit = (cert: Certificate) => {
        setEditingCert(cert);
        setShowForm(true);
    };

    const filtered = certificates.filter((c) => {
        const q = searchTerm.toLowerCase();
        return (
            (c.serialNumber ?? '').toLowerCase().includes(q) ||
            (c.ownerName ?? '').toLowerCase().includes(q) ||
            (c.path ?? '').toLowerCase().includes(q)
        );
    });

    return (
        <div className="certificate-management">
            <div className="management-container">
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
                                placeholder="Search by serial, owner, path..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                className="search-input"
                            />
                            <span className="result-count">{filtered.length} certificate(s)</span>
                        </div>

                        <button
                            className="btn btn-primary"
                            onClick={() => setShowForm(true)}
                            disabled={loading}
                        >
                            + Issue Certificate
                        </button>
                    </div>

                    {error && <div className="alert alert-error">{error}</div>}

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
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {filtered.length > 0 ? (
                                        filtered.map((cert) => {
                                            const isRevoked = !!cert.revokedOn;
                                            return (
                                                <tr
                                                    key={cert.id}
                                                    className={isRevoked ? 'cert-revoked' : 'cert-active'}
                                                >
                                                    <td>{cert.serialNumber ?? '—'}</td>
                                                    <td className="mono-text">{cert.path}</td>
                                                    <td>
                                                        {cert.issuedOn
                                                            ? new Date(cert.issuedOn).toLocaleDateString()
                                                            : '—'}
                                                    </td>
                                                    <td>
                                                        {cert.notAfter
                                                            ? new Date(cert.notAfter).toLocaleDateString()
                                                            : '—'}
                                                    </td>
                                                    <td>
                                                        <span
                                                            className={`cert-badge ${isRevoked ? 'cert-revoked' : 'cert-active'}`}
                                                        >
                                                            {isRevoked ? 'Revoked' : 'Active'}
                                                        </span>
                                                    </td>
                                                    <td className="actions-cell">
                                                        <button
                                                            className="btn-icon"
                                                            title="Edit"
                                                            onClick={() => handleEdit(cert)}
                                                            disabled={isRevoked}
                                                        >
                                                            ✎
                                                        </button>
                                                        {!isRevoked && (
                                                            <button
                                                                className="btn-icon revoke-btn"
                                                                title="Revoke"
                                                                onClick={() => cert.id && handleRevoke(cert.id)}
                                                            >
                                                                ✕
                                                            </button>
                                                        )}
                                                    </td>
                                                </tr>
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
                                setError(null);
                            }}
                            loading={formLoading}
                        />
                    )}
                </main>
            </div>
        </div>
    );
}
