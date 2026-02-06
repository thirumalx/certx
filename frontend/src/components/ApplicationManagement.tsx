import { useState, useEffect } from 'react';
import {
  type Application,
  applicationService,
  type PageResponse,
} from '../services/applicationService';
import { ApplicationForm } from './ApplicationForm';
import '../styles/ApplicationManagement.css';

export function ApplicationManagement() {
  const [applications, setApplications] = useState<Application[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Form state
  const [showForm, setShowForm] = useState(false);
  const [selectedApplication, setSelectedApplication] = useState<Application | undefined>();
  const [formLoading, setFormLoading] = useState(false);

  // Search state
  const [searchTerm, setSearchTerm] = useState('');

  // Load applications
  const loadApplications = async (page: number = 0) => {
    try {
      setLoading(true);
      setError(null);
      const response: PageResponse<Application> = await applicationService.listApplications(
        page,
        pageSize
      );
      setApplications(response.content);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
      setCurrentPage(response.currentPage);
    } catch (err) {
      let errorMessage = 'Failed to load applications';
      if (err instanceof TypeError && err.message.includes('Failed to fetch')) {
        errorMessage = 'Cannot connect to backend API. Make sure the server is running on http://localhost:8080';
      } else if (err instanceof Error) {
        errorMessage = err.message;
      }
      setError(errorMessage);
      console.error('Error loading applications:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadApplications(0);
  }, [pageSize]);

  // Handle create/update
  const handleFormSubmit = async (formData: Application) => {
    try {
      setFormLoading(true);
      setError(null);

      if (selectedApplication?.id) {
        // Update
        await applicationService.updateApplication(selectedApplication.id, formData);
      } else {
        // Create
        await applicationService.createApplication(formData);
      }

      // Reload the list
      loadApplications(0);
      setShowForm(false);
      setSelectedApplication(undefined);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Operation failed';
      setError(errorMessage);
    } finally {
      setFormLoading(false);
    }
  };

  // Handle edit
  const handleEdit = async (application: Application) => {
    try {
      const fullApplication = await applicationService.getApplication(
        application.id!
      );
      setSelectedApplication(fullApplication);
      setShowForm(true);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to load application';
      setError(errorMessage);
    }
  };

  // Handle delete
  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this application?')) {
      return;
    }

    try {
      setLoading(true);
      setError(null);
      await applicationService.deleteApplication(id);
      loadApplications(currentPage);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to delete application';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // Handle form cancel
  const handleFormCancel = () => {
    setShowForm(false);
    setSelectedApplication(undefined);
    setError(null);
  };

  // Filter applications based on search term
  const filteredApplications = applications.filter((app) =>
    app.applicationName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    app.uniqueId.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="application-management">
      <div className="management-header">
        <h1>Application Management</h1>
        <button
          className="btn btn-primary"
          onClick={() => {
            setSelectedApplication(undefined);
            setShowForm(true);
          }}
          disabled={loading}
        >
          + New Application
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="search-bar">
        <input
          type="text"
          placeholder="Search by name or unique ID..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="search-input"
        />
        <span className="result-count">
          {filteredApplications.length} of {totalElements} applications
        </span>
      </div>

      {loading && !showForm ? (
        <div className="loading">Loading applications...</div>
      ) : (
        <>
          <div className="table-container">
            <table className="applications-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Application Name</th>
                  <th>Unique ID</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredApplications.length > 0 ? (
                  filteredApplications.map((app) => (
                    <tr key={app.id} className={`status-${app.status.toLowerCase()}`}>
                      <td>{app.id}</td>
                      <td>{app.applicationName}</td>
                      <td>{app.uniqueId}</td>
                      <td>
                        <span className={`status-badge status-${app.status.toLowerCase()}`}>
                          {app.status}
                        </span>
                      </td>
                      <td className="actions-cell">
                        <button
                          className="btn btn-sm btn-info"
                          onClick={() => handleEdit(app)}
                          disabled={loading}
                        >
                          Edit
                        </button>
                        <button
                          className="btn btn-sm btn-danger"
                          onClick={() => handleDelete(app.id!)}
                          disabled={loading}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={5} className="no-data">
                      {searchTerm ? 'No applications found matching your search' : 'No applications yet'}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          <div className="pagination">
            <button
              className="btn btn-sm"
              onClick={() => loadApplications(currentPage - 1)}
              disabled={currentPage === 0 || loading}
            >
              Previous
            </button>
            <span className="page-info">
              Page {currentPage + 1} of {Math.max(1, totalPages)}
            </span>
            <button
              className="btn btn-sm"
              onClick={() => loadApplications(currentPage + 1)}
              disabled={currentPage >= totalPages - 1 || loading}
            >
              Next
            </button>
          </div>
        </>
      )}

      {showForm && (
        <ApplicationForm
          application={selectedApplication}
          onSubmit={handleFormSubmit}
          onCancel={handleFormCancel}
          loading={formLoading}
        />
      )}
    </div>
  );
}
