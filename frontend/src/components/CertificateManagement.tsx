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
    const [formLoading, setFormLoading] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');

    // Load client info for the heading
    useEffect(() => {
        clientService.getClient(appId, cId).then((c) => {
            setClientName(c?.name ?? `Client #${cId}`);
        }).catch(() => {
            setClientName(`Client #${cId}`);
        });
    }, [appId, cId]);

    // Fetch certificate detail for this client using ownerName lookup
    const loadCertificates = async () => {
        if (!clientName) return;
        try {
            setLoading(true);
            setError(null);
            const cert = await certificateService.getCertificateDetail({ ownerName: clientName });
            setCertificates(cert ? [cert] : []);
        } catch {
            // No certificate found — show empty table
            setCertificates([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (clientName) loadCertificates();
    }, [clientName]);

    const handleFormSubmit = async (certData: Certificate) => {
        try {
            setFormLoading(true);
            setError(null);
            const saved = await certificateService.saveCertificate({
                ...certData,
                ownerName: certData.ownerName || clientName,
                clientId: cId,
            });
            setCertificates((prev) => {
                const idx = prev.findIndex((c) => c.serialNumber === saved.serialNumber);
                if (idx >= 0) {
                    const updated = [...prev];
                    updated[idx] = saved;
                    return updated;
                }
                return [...prev, saved];
            });
            setShowForm(false);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to save certificate');
        } finally {
            setFormLoading(false);
        }
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
                                        <th>Owner Name</th>
                                        <th>Path</th>
                                        <th>Issued On</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {filtered.length > 0 ? (
                                        filtered.map((cert, idx) => {
                                            const isRevoked = !!cert.revokedOn;
                                            return (
                                                <tr
                                                    key={cert.serialNumber ?? idx}
                                                    className={isRevoked ? 'cert-revoked' : 'cert-active'}
                                                >
                                                    <td>{cert.serialNumber ?? '—'}</td>
                                                    <td>{cert.ownerName}</td>
                                                    <td>{cert.path}</td>
                                                    <td>
                                                        {cert.issuedOn
                                                            ? new Date(cert.issuedOn).toLocaleDateString()
                                                            : '—'}
                                                    </td>
                                                    <td>
                                                        <span
                                                            className={`cert-badge ${isRevoked ? 'cert-revoked' : 'cert-active'}`}
                                                        >
                                                            {isRevoked ? 'Revoked' : 'Active'}
                                                        </span>
                                                    </td>
                                                </tr>
                                            );
                                        })
                                    ) : (
                                        <tr>
                                            <td colSpan={5} className="no-data">
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
                            defaultOwnerName={clientName}
                            onSubmit={handleFormSubmit}
                            onCancel={() => {
                                setShowForm(false);
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
