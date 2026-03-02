import { useState } from 'react';
import { type Certificate } from '../services/certificateService';
import '../styles/ApplicationForm.css';

interface CertificateFormProps {
    defaultOwnerName?: string;
    onSubmit: (cert: Certificate) => void;
    onCancel: () => void;
    loading?: boolean;
}

export function CertificateForm({
    defaultOwnerName = '',
    onSubmit,
    onCancel,
    loading = false,
}: CertificateFormProps) {
    const [formData, setFormData] = useState<Certificate>({
        path: '',
        ownerName: defaultOwnerName,
    });

    const [errors, setErrors] = useState<Record<string, string>>({});

    const validateForm = (): boolean => {
        const newErrors: Record<string, string> = {};
        if (!formData.path.trim()) newErrors.path = 'Certificate path is required';
        if (!formData.ownerName.trim()) newErrors.ownerName = 'Owner name is required';
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
        if (errors[name]) setErrors((prev) => ({ ...prev, [name]: '' }));
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (validateForm()) onSubmit(formData);
    };

    return (
        <div className="form-overlay">
            <div className="form-container">
                <h2>Issue New Certificate</h2>

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="cert-path">Certificate Path *</label>
                        <input
                            type="text"
                            id="cert-path"
                            name="path"
                            value={formData.path}
                            onChange={handleChange}
                            placeholder="e.g. /certs/client.pem"
                            disabled={loading}
                        />
                        {errors.path && <span className="error-message">{errors.path}</span>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="cert-ownerName">Owner Name *</label>
                        <input
                            type="text"
                            id="cert-ownerName"
                            name="ownerName"
                            value={formData.ownerName}
                            onChange={handleChange}
                            placeholder="Enter owner name"
                            disabled={loading}
                        />
                        {errors.ownerName && (
                            <span className="error-message">{errors.ownerName}</span>
                        )}
                    </div>

                    <div className="form-actions">
                        <button type="submit" className="btn btn-primary" disabled={loading}>
                            {loading ? 'Issuing...' : 'Issue Certificate'}
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
