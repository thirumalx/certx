import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    type Client,
    clientService,
} from '../services/clientService';
import { type PageResponse } from '../services/applicationService';
import { applicationService } from '../services/applicationService';
import { ClientForm } from './ClientForm';
import '../styles/ClientManagement.css';

type FilterStatus = 'All' | 'Active' | 'Inactive' | 'Suspended' | 'Deleted';

export function ClientManagement() {
    const { applicationId } = useParams<{ applicationId: string }>();
    const navigate = useNavigate();
    const appId = Number(applicationId);

    const [applicationName, setApplicationName] = useState<string>('');
    const [clients, setClients] = useState<Client[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [formError, setFormError] = useState<string | null>(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [pageSize] = useState(10);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    // Form state
    const [showForm, setShowForm] = useState(false);
    const [selectedClient, setSelectedClient] = useState<Client | undefined>();
    const [formLoading, setFormLoading] = useState(false);

    // Search & filter state
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedFilter, setSelectedFilter] = useState<FilterStatus>('All');

    // Load application name for heading
    useEffect(() => {
        applicationService.getApplication(appId).then((app) => {
            setApplicationName(app.applicationName);
        }).catch(() => {
            setApplicationName(`Application #${appId}`);
        });
    }, [appId]);

    const loadClients = async (page: number = 0) => {
        try {
            setLoading(true);
            setError(null);
            const response: PageResponse<Client> = await clientService.listClients(appId, page, pageSize);
            setClients(response.content);
            setTotalPages(response.totalPages);
            setTotalElements(response.totalElements);
            setCurrentPage(response.page);
        } catch (err) {
            let errorMessage = 'Failed to load clients';
            if (err instanceof TypeError && err.message.includes('Failed to fetch')) {
                errorMessage = 'Cannot connect to backend API. Make sure the server is running on http://localhost:8080';
            } else if (err instanceof Error) {
                errorMessage = err.message;
            }
            setError(errorMessage);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadClients(0);
    }, [appId, pageSize]);

    const handleFormSubmit = async (formData: Client) => {
        try {
            setFormLoading(true);
            setError(null);
            setFormError(null);
            if (selectedClient?.id) {
                await clientService.updateClient(appId, selectedClient.id, formData);
            } else {
                await clientService.createClient(appId, formData);
            }
            loadClients(0);
            setShowForm(false);
            setSelectedClient(undefined);
        } catch (err) {
            setFormError(err instanceof Error ? err.message : 'Operation failed');
        } finally {
            setFormLoading(false);
        }
    };

    const handleEdit = async (client: Client) => {
        try {
            const full = await clientService.getClient(appId, client.id!);
            setSelectedClient(full);
            setShowForm(true);
            setFormError(null);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to load client');
        }
    };

    const handleDelete = async (id: number) => {
        if (!confirm('Are you sure you want to delete this client?')) return;
        try {
            setLoading(true);
            setError(null);
            await clientService.deleteClient(appId, id);
            loadClients(currentPage);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to delete client');
        } finally {
            setLoading(false);
        }
    };

    const handleFormCancel = () => {
        setShowForm(false);
        setSelectedClient(undefined);
        setFormError(null);
    };

    const navigateToCertificates = (clientId: number) => {
        navigate(`/applications/${appId}/clients/${clientId}/certificates`);
    };

    const filteredClients = clients.filter((c) => {
        const matchesSearch =
            c.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            (c.uniqueId ?? '').toLowerCase().includes(searchTerm.toLowerCase()) ||
            (c.email ?? '').toLowerCase().includes(searchTerm.toLowerCase()) ||
            (c.mobileNumber ?? '').toLowerCase().includes(searchTerm.toLowerCase());

        if (selectedFilter === 'All') return matchesSearch;
        return matchesSearch && c.status.toLowerCase() === selectedFilter.toLowerCase();
    });

    const filterOptions: FilterStatus[] = ['All', 'Active', 'Inactive', 'Suspended', 'Deleted'];

    return (
        <div className="client-management">
            <div className="management-container">
                <aside className="left-nav">
                    <nav className="filter-nav">
                        <h3 className="filter-title">Filter</h3>
                        <ul className="filter-list">
                            {filterOptions.map((f) => (
                                <li key={f}>
                                    <button
                                        className={`filter-btn ${selectedFilter === f ? 'active' : ''}`}
                                        onClick={() => {
                                            setSelectedFilter(f);
                                            setCurrentPage(0);
                                        }}
                                    >
                                        {f}
                                    </button>
                                </li>
                            ))}
                        </ul>
                    </nav>
                </aside>

                <main className="management-content">
                    <div className="management-header">
                        <div className="header-left">
                            <button className="back-btn" onClick={() => navigate('/applications')}>
                                ← Applications
                            </button>
                            <div className="page-title-group">
                                <h2 className="page-title">
                                    Client Management
                                    {applicationName && (
                                        <span className="page-title-app"> - {applicationName}</span>
                                    )}
                                </h2>
                            </div>
                        </div>

                        <div className="search-bar">
                            <input
                                type="text"
                                placeholder="Search by name, unique ID, email..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                className="search-input"
                            />
                            <span className="result-count">
                                {filteredClients.length} of {totalElements} clients
                            </span>
                        </div>

                        <button
                            className="btn btn-primary"
                            onClick={() => {
                                setSelectedClient(undefined);
                                setFormError(null);
                                setShowForm(true);
                            }}
                            disabled={loading}
                        >
                            + New Client
                        </button>
                    </div>

                    {!showForm && error && <div className="alert alert-error">{error}</div>}

                    {loading && !showForm ? (
                        <div className="loading">Loading clients...</div>
                    ) : (
                        <>
                            <div className="table-container">
                                <table className="clients-table">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>UID</th>
                                            <th>Name</th>
                                            <th>Email</th>
                                            <th>Mobile</th>
                                            <th>Assignees</th>
                                            <th>Status</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {filteredClients.length > 0 ? (
                                            filteredClients.map((client) => (
                                                <tr
                                                    key={client.id}
                                                    className={`status-${client.status.toLowerCase()}`}
                                                    onClick={() => navigateToCertificates(client.id!)}
                                                    onKeyDown={(event) => {
                                                        if (event.key === 'Enter' || event.key === ' ') {
                                                            event.preventDefault();
                                                            navigateToCertificates(client.id!);
                                                        }
                                                    }}
                                                    role="button"
                                                    tabIndex={0}
                                                >
                                                    <td>{client.id}</td>
                                                    <td>{client.uniqueId ?? '—'}</td>
                                                    <td>
                                                        <span className="clickable-name">{client.name}</span>
                                                    </td>
                                                    <td>{client.email ?? '—'}</td>
                                                    <td>{client.mobileNumber ?? '—'}</td>
                                                    <td>{client.assignedUserCount ?? 0}</td>
                                                    <td>
                                                        <span className={`status-badge status-${client.status.toLowerCase()}`}>
                                                            {client.status}
                                                        </span>
                                                    </td>
                                                    <td className="actions-cell">
                                                        <button
                                                            className="btn btn-sm btn-info"
                                                            onClick={(event) => {
                                                                event.stopPropagation();
                                                                handleEdit(client);
                                                            }}
                                                            disabled={loading}
                                                        >
                                                            Edit
                                                        </button>
                                                        <button
                                                            className="btn btn-sm btn-success"
                                                            onClick={(event) => {
                                                                event.stopPropagation();
                                                                navigateToCertificates(client.id!);
                                                            }}
                                                        >
                                                            Certificates →
                                                        </button>
                                                        <button
                                                            className="btn btn-sm btn-outline"
                                                            onClick={(event) => {
                                                                event.stopPropagation();
                                                                navigate(
                                                                    `/applications/${appId}/clients/${client.id}/assignees`
                                                                );
                                                            }}
                                                        >
                                                            Assignees
                                                        </button>
                                                        <button
                                                            className="btn btn-sm btn-danger"
                                                            onClick={(event) => {
                                                                event.stopPropagation();
                                                                handleDelete(client.id!);
                                                            }}
                                                            disabled={loading}
                                                        >
                                                            Delete
                                                        </button>
                                                    </td>
                                                </tr>
                                            ))
                                        ) : (
                                            <tr>
                                                <td colSpan={7} className="no-data">
                                                    {searchTerm
                                                        ? 'No clients found matching your search'
                                                        : 'No clients yet — click "+ New Client" to add one'}
                                                </td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            </div>

                            <div className="pagination">
                                <button
                                    className="btn btn-sm btn-outline"
                                    onClick={() => loadClients(currentPage - 1)}
                                    disabled={currentPage === 0 || loading}
                                >
                                    Previous
                                </button>

                                <div className="page-numbers">
                                    {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                                        let pageNum = i;
                                        if (totalPages > 5) {
                                            if (currentPage > 2) pageNum = currentPage - 2 + i;
                                            if (pageNum >= totalPages) pageNum = totalPages - 5 + i;
                                            if (pageNum < 0) pageNum = i;
                                        }
                                        if (pageNum >= totalPages) return null;

                                        return (
                                            <button
                                                key={pageNum}
                                                className={`page-btn ${currentPage === pageNum ? 'active' : ''}`}
                                                onClick={() => loadClients(pageNum)}
                                                disabled={loading}
                                            >
                                                {pageNum + 1}
                                            </button>
                                        );
                                    })}
                                </div>

                                <button
                                    className="btn btn-sm btn-outline"
                                    onClick={() => loadClients(currentPage + 1)}
                                    disabled={currentPage >= totalPages - 1 || loading}
                                >
                                    Next
                                </button>

                                <span className="page-info">
                                    Page {currentPage + 1} of {Math.max(1, totalPages)} ({totalElements} items)
                                </span>
                            </div>
                        </>
                    )}

                    {showForm && (
                        <ClientForm
                            client={selectedClient}
                            applicationId={appId}
                            onSubmit={handleFormSubmit}
                            onCancel={handleFormCancel}
                            loading={formLoading}
                            errorMessage={formError}
                        />
                    )}
                </main>
            </div>
        </div>
    );
}
