import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { clientService, type AssignedUser } from '../services/clientService';
import '../styles/ClientManagement.css';
import '../styles/ApplicationForm.css';

export function ClientAssignees() {
    const { applicationId, clientId } = useParams<{
        applicationId: string;
        clientId: string;
    }>();
    const navigate = useNavigate();
    const appId = Number(applicationId);
    const cId = Number(clientId);

    const [clientName, setClientName] = useState<string>('');
    const [assignees, setAssignees] = useState<AssignedUser[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const [showForm, setShowForm] = useState(false);
    const [formEmail, setFormEmail] = useState('');
    const [formName, setFormName] = useState('');
    const [formMobile, setFormMobile] = useState('');
    const [editing, setEditing] = useState<AssignedUser | null>(null);
    const [formError, setFormError] = useState<string | null>(null);

    useEffect(() => {
        clientService.getClient(appId, cId).then((c) => {
            setClientName(c?.name ?? `Client #${cId}`);
        }).catch(() => {
            setClientName(`Client #${cId}`);
        });
    }, [appId, cId]);

    const loadAssignees = async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await clientService.listAssignees(appId, cId);
            setAssignees(data);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to load assignees');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadAssignees();
    }, [appId, cId]);

    const openCreate = () => {
        setEditing(null);
        setFormEmail('');
        setFormName('');
        setFormMobile('');
        setFormError(null);
        setShowForm(true);
    };

    const openEdit = (assignee: AssignedUser) => {
        setEditing(assignee);
        setFormEmail(assignee.email ?? '');
        setFormName(assignee.name ?? '');
        setFormMobile(assignee.mobileNumber ?? '');
        setFormError(null);
        setShowForm(true);
    };

    const validateForm = () => {
        if (!formEmail.trim()) return 'Email is required';
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formEmail)) return 'Enter a valid email address';
        if (!editing) {
            if (!formName.trim()) return 'Name is required';
            if (!formMobile.trim()) return 'Mobile number is required';
        }
        return null;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        const errorMsg = validateForm();
        if (errorMsg) {
            setFormError(errorMsg);
            return;
        }

        try {
            setLoading(true);
            setFormError(null);
            if (editing?.id) {
                await clientService.updateAssignee(appId, cId, editing.id, {
                    email: formEmail,
                    name: formName,
                    mobileNumber: formMobile,
                });
            } else {
                await clientService.assignUser(appId, cId, {
                    email: formEmail,
                    name: formName,
                    mobileNumber: formMobile,
                });
            }
            setShowForm(false);
            setEditing(null);
            setFormEmail('');
            setFormName('');
            setFormMobile('');
            loadAssignees();
        } catch (err) {
            setFormError(err instanceof Error ? err.message : 'Operation failed');
        } finally {
            setLoading(false);
        }
    };

    const handleRemove = async (assignee: AssignedUser) => {
        if (!window.confirm('Remove this assignee?')) return;
        try {
            setLoading(true);
            await clientService.removeAssignee(appId, cId, assignee.id);
            loadAssignees();
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to remove assignee');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="client-management">
            <div className="management-container">
                <main className="management-content">
                    <div className="management-header">
                        <div className="header-left">
                            <button
                                className="back-btn"
                                onClick={() => navigate(`/applications/${appId}/clients`)}
                            >
                                â† Clients
                            </button>
                            <div className="page-title-group">
                                <h2>Assigned Users</h2>
                                {clientName && <p className="page-subtitle">{clientName}</p>}
                            </div>
                        </div>

                        <button className="btn btn-primary" onClick={openCreate} disabled={loading}>
                            + Add Assignee
                        </button>
                    </div>

                    {error && <div className="alert alert-error">{error}</div>}

                    {loading && !showForm ? (
                        <div className="loading">Loading assignees...</div>
                    ) : (
                        <div className="table-container">
                            <table className="clients-table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Name</th>
                                        <th>Email</th>
                                        <th>Mobile</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {assignees.length > 0 ? (
                                        assignees.map((assignee) => (
                                            <tr key={assignee.id}>
                                                <td>{assignee.id}</td>
                                                <td>{assignee.name ?? 'â€”'}</td>
                                                <td>{assignee.email ?? 'â€”'}</td>
                                                <td>{assignee.mobileNumber ?? 'â€”'}</td>
                                                <td className="actions-cell">
                                                    <button
                                                        className="btn btn-sm btn-info"
                                                        onClick={() => openEdit(assignee)}
                                                        disabled={loading}
                                                    >
                                                        Edit
                                                    </button>
                                                    <button
                                                        className="btn btn-sm btn-danger"
                                                        onClick={() => handleRemove(assignee)}
                                                        disabled={loading}
                                                    >
                                                        Remove
                                                    </button>
                                                </td>
                                            </tr>
                                        ))
                                    ) : (
                                        <tr>
                                            <td colSpan={5} className="no-data">
                                                No assignees yet — click "+ Add Assignee" to add one
                                            </td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    )}

                    {showForm && (
                        <div className="form-overlay">
                            <div className="form-container">
                                <h2>{editing ? 'Update Assignee' : 'Add Assignee'}</h2>
                                <form onSubmit={handleSubmit}>
                                    <div className="form-group">
                                        <label htmlFor="name">Name {!editing && '*'}</label>
                                        <input
                                            type="text"
                                            id="name"
                                            value={formName}
                                            onChange={(e) => setFormName(e.target.value)}
                                            placeholder="Enter user name"
                                            disabled={loading}
                                        />
                                    </div>
                                    <div className="form-group">
                                        <label htmlFor="email">User Email *</label>
                                        <input
                                            type="email"
                                            id="email"
                                            value={formEmail}
                                            onChange={(e) => setFormEmail(e.target.value)}
                                            placeholder="Enter user email"
                                            disabled={loading}
                                        />
                                        {formError && <span className="error-message">{formError}</span>}
                                    </div>
                                    
                                    <div className="form-group">
                                        <label htmlFor="mobile">Mobile Number {!editing && '*'}</label>
                                        <input
                                            type="text"
                                            id="mobile"
                                            value={formMobile}
                                            onChange={(e) => setFormMobile(e.target.value)}
                                            placeholder="Enter mobile number"
                                            disabled={loading}
                                        />
                                    </div>
                                    <div className="form-actions">
                                        <button type="submit" className="btn btn-primary" disabled={loading}>
                                            {loading ? 'Saving...' : (editing ? 'Update' : 'Assign')}
                                        </button>
                                        <button
                                            type="button"
                                            className="btn btn-secondary"
                                            onClick={() => {
                                                setShowForm(false);
                                                setEditing(null);
                                                setFormError(null);
                                            }}
                                            disabled={loading}
                                        >
                                            Cancel
                                        </button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    )}
                </main>
            </div>
        </div>
    );
}
