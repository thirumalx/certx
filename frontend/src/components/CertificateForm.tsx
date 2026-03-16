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
    readOnly?: boolean;
    errorMessage?: string | null;
}

export function CertificateForm({
    certificate,
    applicationId,
    clientId,
    defaultOwnerName = '',
    onSubmit,
    onCancel,
    loading = false,
    readOnly = false,
    errorMessage = null,
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
            password: '',
        }
    );

    const [isPfx, setIsPfx] = useState(formData.path.toLowerCase().endsWith('.pfx') || formData.path.toLowerCase().endsWith('.p12'));

    const [isValidating, setIsValidating] = useState(false);
    const [validationStatus, setValidationStatus] = useState<'success' | 'error' | null>(null);
    const [validationMessage, setValidationMessage] = useState<string | null>(null);

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
        if (name === 'path') {
            setValidationStatus(null);
            setValidationMessage(null);
            setIsPfx(value.toLowerCase().endsWith('.pfx') || value.toLowerCase().endsWith('.p12'));
        }
    };

    const handleValidate = async () => {
        if (!formData.path.trim()) {
            setErrors((prev) => ({ ...prev, path: 'Path is required for validation' }));
            return;
        }
        try {
            setIsValidating(true);
            setValidationMessage(null);
            const certDetails = await certificateService.validateCertificate(
                applicationId,
                clientId,
                formData.path,
                formData.password
            );

            if (certDetails.serialNumber === 'PASSWORD_PROTECTED_PFX') {
                setValidationStatus('error');
                setValidationMessage('Password required or incorrect for this PFX file');
                setErrors((prev) => ({ ...prev, password: 'Password required or incorrect for this PFX file' }));
                return;
            }

            // Auto-populate fields from the validated certificate
            setFormData((prev) => ({
                ...prev,
                serialNumber: certDetails.serialNumber ?? prev.serialNumber,
                issuedOn: certDetails.issuedOn
                    ? certDetails.issuedOn.split('T')[0] + "T00:00:00"
                    : prev.issuedOn,
                notAfter: certDetails.notAfter
                    ? certDetails.notAfter.split('T')[0] + "T00:00:00"
                    : prev.notAfter,
                lastTimeVerifiedOn: certDetails.lastTimeVerifiedOn
                    ? certDetails.lastTimeVerifiedOn.split('T')[0] + "T00:00:00"
                    : new Date().toISOString().slice(0, 19),
            }));

            setValidationStatus('success');
            setValidationMessage('Certificate details fetched successfully!');
            // Clear errors for fields that might have been populated
            setErrors((prev) => {
                const newErrors = { ...prev };
                if (certDetails.serialNumber) delete newErrors.serialNumber;
                if (certDetails.notAfter) delete newErrors.notAfter;
                delete newErrors.path;
                delete newErrors.password;
                return newErrors;
            });
        } catch (err) {
            setValidationStatus('error');
            setValidationMessage(err instanceof Error ? err.message : 'Validation failed');
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
                <h2>{certificate ? 'Edit Certificate' : 'Add New Certificate'}</h2>

                <form onSubmit={handleSubmit} className="two-column-form">
                    {errorMessage && (
                        <div className="form-error-banner" role="alert">
                            {errorMessage}
                        </div>
                    )}
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
                                    placeholder="e.g. C:\Certs\client.pem"
                                    disabled={loading || readOnly}
                                />
                                {!readOnly && (
                                    <button
                                        type="button"
                                        className={`btn-action btn-large ${validationStatus}`}
                                        onClick={handleValidate}
                                        disabled={isValidating || loading}
                                    >
                                        {isValidating ? '...' : 'Fetch Certificate Details'}
                                    </button>
                                )}
                            </div>
                            {validationMessage && (
                                <span className={validationStatus === 'success' ? 'success-message' : 'error-message'}>
                                    {validationMessage}
                                </span>
                            )}
                            {errors.path && <span className="error-message">{errors.path}</span>}
                        </div>

                        <div className="form-group">
                            <label htmlFor="cert-serialNumber">Serial Number *</label>
                            <input
                                type="text"
                                id="cert-serialNumber"
                                name="serialNumber"
                                value={formData.serialNumber}
                                onChange={handleChange}
                                placeholder="Auto-populated"
                                disabled={true}
                                className="readonly-input"
                            />
                            {errors.serialNumber && <span className="error-message">{errors.serialNumber}</span>}
                        </div>
                    </div>

                    {isPfx && !readOnly && (
                        <div className="form-row">
                            <div className="form-group">
                                <label htmlFor="pfx-password">PFX Password</label>
                                <input
                                    type="password"
                                    id="pfx-password"
                                    name="password"
                                    value={formData.password || ''}
                                    onChange={handleChange}
                                    placeholder="Enter password if required"
                                    disabled={loading || readOnly}
                                />
                                {errors.password && <span className="error-message">{errors.password}</span>}
                            </div>
                            <div className="form-group placeholder-group"></div>
                        </div>
                    )}

                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="cert-issuedOn">Issued On</label>
                            <input
                                type="date"
                                id="cert-issuedOn"
                                name="issuedOn"
                                value={formData.issuedOn?.split('T')[0] || ''}
                                onChange={handleChange}
                                disabled={true}
                                className="readonly-input"
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
                                disabled={true}
                                className="readonly-input"
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
                                disabled={true}
                                className="readonly-input"
                            />
                        </div>
                        <div className="form-group placeholder-group"></div>
                    </div>

                    <div className="form-actions">
                        {!readOnly && (
                            <button type="submit" className="btn btn-primary" disabled={loading}>
                                {loading ? 'Saving...' : certificate ? 'Update' : 'Add Certificate'}
                            </button>
                        )}
                        <button
                            type="button"
                            className="btn btn-secondary"
                            onClick={onCancel}
                            disabled={loading}
                        >
                            {readOnly ? 'Close' : 'Cancel'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
