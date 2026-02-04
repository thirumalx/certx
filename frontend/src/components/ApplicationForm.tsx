import { useState, useEffect } from 'react';
import { Application } from '../services/applicationService';
import '../styles/ApplicationForm.css';

interface ApplicationFormProps {
  application?: Application;
  onSubmit: (application: Application) => void;
  onCancel: () => void;
  loading?: boolean;
}

export function ApplicationForm({
  application,
  onSubmit,
  onCancel,
  loading = false,
}: ApplicationFormProps) {
  const [formData, setFormData] = useState<Application>({
    applicationName: '',
    uniqueId: '',
    status: 'ACTIVE',
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (application) {
      setFormData(application);
    }
  }, [application]);

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.applicationName.trim()) {
      newErrors.applicationName = 'Application Name is required';
    }

    if (!formData.uniqueId.trim()) {
      newErrors.uniqueId = 'Unique ID is required';
    }

    if (!formData.status.trim()) {
      newErrors.status = 'Status is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // Clear error for this field when user starts typing
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: '',
      }));
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (validateForm()) {
      onSubmit(formData);
    }
  };

  return (
    <div className="form-overlay">
      <div className="form-container">
        <h2>{application ? 'Edit Application' : 'Create New Application'}</h2>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="applicationName">Application Name *</label>
            <input
              type="text"
              id="applicationName"
              name="applicationName"
              value={formData.applicationName}
              onChange={handleChange}
              placeholder="Enter application name"
              disabled={loading}
            />
            {errors.applicationName && (
              <span className="error-message">{errors.applicationName}</span>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="uniqueId">Unique ID *</label>
            <input
              type="text"
              id="uniqueId"
              name="uniqueId"
              value={formData.uniqueId}
              onChange={handleChange}
              placeholder="Enter unique identifier"
              disabled={loading}
            />
            {errors.uniqueId && (
              <span className="error-message">{errors.uniqueId}</span>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="status">Status *</label>
            <select
              id="status"
              name="status"
              value={formData.status}
              onChange={handleChange}
              disabled={loading}
            >
              <option value="">-- Select Status --</option>
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
              <option value="SUSPENDED">SUSPENDED</option>
              <option value="ARCHIVED">ARCHIVED</option>
            </select>
            {errors.status && (
              <span className="error-message">{errors.status}</span>
            )}
          </div>

          <div className="form-actions">
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? 'Saving...' : application ? 'Update' : 'Create'}
            </button>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={onCancel}
              disabled={loading}
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
