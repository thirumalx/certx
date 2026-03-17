import { useState, useEffect } from 'react';
import { type Client, clientService } from '../services/clientService';
import '../styles/ApplicationForm.css';

interface ClientFormProps {
    client?: Client;
    applicationId: number;
    onSubmit: (client: Client) => void;
    onCancel: () => void;
    loading?: boolean;
    errorMessage?: string | null;
}

export function ClientForm({
    client,
    applicationId,
    onSubmit,
    onCancel,
    loading = false,
    errorMessage = null,
}: ClientFormProps) {
    const [formData, setFormData] = useState<Client>({
        uniqueId: '',
        name: '',
        email: '',
        mobileNumber: '',
        status: 'ACTIVE',
    });

    const [errors, setErrors] = useState<Record<string, string>>({});

    useEffect(() => {
        if (client) {
            setFormData({
                uniqueId: client.uniqueId ?? '',
                name: client.name,
                email: client.email ?? '',
                mobileNumber: client.mobileNumber ?? '',
                status: client.status,
            });
        }
    }, [client]);

    const validateForm = (): boolean => {
        const newErrors: Record<string, string> = {};
        if (formData.uniqueId && formData.uniqueId.trim().length > 15) {
            newErrors.uniqueId = 'Unique ID must be at most 15 characters';
        }
        if (!formData.name.trim()) newErrors.name = 'Name is required';
        if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
            newErrors.email = 'Enter a valid email address';
        }
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
    ) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
        if (errors[name]) setErrors((prev) => ({ ...prev, [name]: '' }));
    };

    const handleUniqueIdBlur = async () => {
        const uniqueId = formData.uniqueId?.trim();
        if (!uniqueId || uniqueId.length > 15) return;
        try {
            const match = await clientService.getClientByUniqueId(applicationId, uniqueId);
            if (!match) return;
            setFormData((prev) => ({
                ...prev,
                name: prev.name.trim() ? prev.name : (match.name ?? ''),
                email: prev.email?.trim() ? prev.email : (match.email ?? ''),
                mobileNumber: prev.mobileNumber?.trim() ? prev.mobileNumber : (match.mobileNumber ?? ''),
            }));
        } catch {
            // Ignore lookup failures; user can still enter data manually.
        }
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (validateForm()) onSubmit(formData);
    };

    return (
        <div className="form-overlay">
            <div className="form-container">
                <h2>{client ? 'Edit Client' : 'Create New Client'}</h2>

                <form onSubmit={handleSubmit}>
                    {errorMessage && (
                        <div className="form-error-banner" role="alert">
                            {errorMessage}
                        </div>
                    )}
                    <div className="form-group">
                        <label htmlFor="uniqueId">Unique ID</label>
                        <input
                            type="text"
                            id="uniqueId"
                            name="uniqueId"
                            value={formData.uniqueId ?? ''}
                            onChange={handleChange}
                            onBlur={handleUniqueIdBlur}
                            placeholder="Enter unique ID"
                            maxLength={15}
                            disabled={loading}
                        />
                        {errors.uniqueId && <span className="error-message">{errors.uniqueId}</span>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="name">Name *</label>
                        <input
                            type="text"
                            id="name"
                            name="name"
                            value={formData.name}
                            onChange={handleChange}
                            placeholder="Enter client name"
                            disabled={loading}
                        />
                        {errors.name && <span className="error-message">{errors.name}</span>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="email">Email</label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            placeholder="Enter email address"
                            disabled={loading}
                        />
                        {errors.email && <span className="error-message">{errors.email}</span>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="mobileNumber">Mobile Number</label>
                        <input
                            type="text"
                            id="mobileNumber"
                            name="mobileNumber"
                            value={formData.mobileNumber}
                            onChange={handleChange}
                            placeholder="Enter mobile number"
                            disabled={loading}
                        />
                    </div>

                    {/* <div className="form-group">
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
                        </select>
                        {errors.status && <span className="error-message">{errors.status}</span>}
                    </div> */}

                    <div className="form-actions">
                        <button type="submit" className="btn btn-primary" disabled={loading}>
                            {loading ? 'Saving...' : client ? 'Update' : 'Create'}
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
