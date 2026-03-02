/**
 * Certificate API Service
 * Handles CRUD operations for certificates
 */

import { API_BASE } from '../config';

export interface Certificate {
    serialNumber?: string;
    path: string;
    ownerName: string;
    issuedOn?: string;
    revokedOn?: string;
    clientId?: number;
}

const CERT_URL = `${API_BASE}/certificate`;

export const certificateService = {
    /**
     * Save (issue) a new certificate
     */
    saveCertificate: async (certificate: Certificate): Promise<Certificate> => {
        const response = await fetch(`${CERT_URL}/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(certificate),
        });
        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            throw new Error(error.message || 'Failed to save certificate');
        }
        return response.json();
    },

    /**
     * Fetch certificate detail by serial number / identifier
     */
    getCertificateDetail: async (certificate: Partial<Certificate>): Promise<Certificate> => {
        const response = await fetch(`${CERT_URL}/fetch-detail`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(certificate),
        });
        if (!response.ok) {
            throw new Error('Failed to fetch certificate detail');
        }
        return response.json();
    },
};
