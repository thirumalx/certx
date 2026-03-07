import { useState } from 'react';
import { type Certificate, certificateService } from '../services/certificateService';
import '../styles/ApplicationForm.css';

interface CertificateFormProps {
    certificate?: Certificate;
    applicationId: number;
    clientId: number;
    defaultOwnerName?: string;
    onSubmit: (cert: Certificate) => void;
    onCancel: () => void;
    loading?: boolean;
}

export function CertificateForm({
    certificate,
    applicationId,
    clientId,
    defaultOwnerName = '',
    onSubmit,
    onCancel,
    loading = false,
}: CertificateFormProps) {
    const [formData, setFormData] = useState<Certificate>(
        certificate || {
            serialNumber: '',
            path: '',
            ownerName: defaultOwnerName,
            status: 'ACTIVE',
            issuedOn: new Date().toISOString().split('T')[0],
            notAfter: '',
            lastTimeVerifiedOn: '',
        }
    );

    const [isValidating, setIsValidating] = useState(false);
    const [validationStatus, setValidationStatus] = useState<'success' | 'error' | null>(null);

    const [errors, setErrors] = useState<Record<string, string>>({});

    const validateForm = (): boolean => {
        const newErrors: Record<string, string> = {};
        if (!formData.serialNumber.trim()) newErrors.serialNumber = 'Serial number is required';
        if (!formData.path.trim()) newErrors.path = 'Certificate path is required';
        if (!formData.notAfter) newErrors.notAfter = 'Expiry date (Not After) is required';
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
    ) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
        if (errors[name]) setErrors((prev) => ({ ...prev, [name]: '' }));
        if (name === 'path') setValidationStatus(null);
    };

    const handleValidate = async () => {
        if (!formData.path.trim()) {
            setErrors((prev) => ({ ...prev, path: 'Path is required for validation' }));
            return;
        }
        try {
            setIsValidating(true);
            const isValid = await certificateService.validateCertificate(
                applicationId,
                clientId,
                formData.path
            );
            setValidationStatus(isValid ? 'success' : 'error');
        } catch {
            setValidationStatus('error');
        } finally {
            setIsValidating(false);
        }
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (validateForm()) onSubmit(formData);
    };

    return (
        <div className="form-overlay">
            <div className="form-container wide-form">
                <h2>{certificate ? 'Edit Certificate' : 'Issue New Certificate'}</h2>

                <form onSubmit={handleSubmit} className="two-column-form">
                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="cert-serialNumber">Serial Number *</label>
                            <input
                                type="text"
                                id="cert-serialNumber"
                                name="serialNumber"
                                value={formData.serialNumber}
                                onChange={handleChange}
                                placeholder="e.g. SN-12345"
                                disabled={loading}
                            />
                            {errors.serialNumber && <span className="error-message">{errors.serialNumber}</span>}
                        </div>

                        <div className="form-group">
                            <label htmlFor="cert-ownerName">Owner Name (Client)</label>
                            <input
                                type="text"
                                id="cert-ownerName"
                                name="ownerName"
                                value={formData.ownerName}
                                onChange={handleChange}
                                placeholder="Enter owner name"
                                disabled={true}
                                className="readonly-input"
                            />
                        </div>
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="cert-path">Certificate Path *</label>
                            <div className="input-with-action">
                                <input
                                    type="text"
                                    id="cert-path"
                                    name="path"
                                    value={formData.path}
                                    onChange={handleChange}
                                    placeholder="e.g. client.pem"
                                    disabled={loading}
                                />
                                <button
                                    type="button"
                                    className={`btn-action ${validationStatus}`}
                                    onClick={handleValidate}
                                    disabled={isValidating || loading}
                                >
                                    {isValidating ? '...' : 'Validate'}
                                </button>
                            </div>
                            {validationStatus === 'success' && (
                                <span className="success-message">File found in directory!</span>
                            )}
                            {validationStatus === 'error' && (
                                <span className="error-message">File NOT found in directory</span>
                            )}
                            {errors.path && <span className="error-message">{errors.path}</span>}
                        </div>

                        <div className="form-group">
                            <label htmlFor="cert-status">Status *</label>
                            <select
                                id="cert-status"
                                name="status"
                                value={formData.status}
                                onChange={handleChange}
                                disabled={loading}
                            >
                                <option value="ACTIVE">ACTIVE</option>
                                <option value="INACTIVE">INACTIVE</option>
                                <option value="REVOKED">REVOKED</option>
                            </select>
                        </div>
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="cert-issuedOn">Issued On</label>
                            <input
                                type="date"
                                id="cert-issuedOn"
                                name="issuedOn"
                                value={formData.issuedOn?.split('T')[0] || ''}
                                onChange={handleChange}
                                disabled={loading}
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="cert-notAfter">Not After (Expiry) *</label>
                            <input
                                type="date"
                                id="cert-notAfter"
                                name="notAfter"
                                value={formData.notAfter?.split('T')[0] || ''}
                                onChange={handleChange}
                                disabled={loading}
                            />
                            {errors.notAfter && <span className="error-message">{errors.notAfter}</span>}
                        </div>
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="cert-lastTimeVerifiedOn">Last Verified On</label>
                            <input
                                type="date"
                                id="cert-lastTimeVerifiedOn"
                                name="lastTimeVerifiedOn"
                                value={formData.lastTimeVerifiedOn?.split('T')[0] || ''}
                                onChange={handleChange}
                                disabled={loading}
                            />
                        </div>
                        <div className="form-group placeholder-group"></div>
                    </div>

                    <div className="form-actions">
                        <button type="submit" className="btn btn-primary" disabled={loading}>
                            {loading ? 'Saving...' : certificate ? 'Update' : 'Issue Certificate'}
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
