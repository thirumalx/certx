/**
 * Certificate API Service
 * Handles CRUD operations for certificates
 */

import { API_BASE } from '../config';

export interface Certificate {
    id?: number;
    serialNumber: string;
    path: string;
    ownerName: string;
    issuedOn?: string;
    revokedOn?: string;
    notAfter?: string;
    lastTimeVerifiedOn?: string;
    status?: string;
    clientId?: number;
}

const CERT_URL = `${API_BASE}/certificate`;

export const certificateService = {
    /**
     * Save (issue) a new certificate
     */
    saveCertificate: async (appId: number, clientId: number, certificate: Certificate): Promise<Certificate> => {
        const response = await fetch(`${API_BASE}/application/${appId}/client/${clientId}/certificate/`, {
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
     * Fetch certificate by ID
     */
    getCertificate: async (appId: number, clientId: number, id: number): Promise<Certificate> => {
        const response = await fetch(`${API_BASE}/application/${appId}/client/${clientId}/certificate/${id}`);
        if (!response.ok) {
            throw new Error('Failed to fetch certificate');
        }
        return response.json();
    },

    /**
     * Update an existing certificate
     */
    updateCertificate: async (appId: number, clientId: number, id: number, certificate: Certificate): Promise<Certificate> => {
        const response = await fetch(`${API_BASE}/application/${appId}/client/${clientId}/certificate/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(certificate),
        });
        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            throw new Error(error.message || 'Failed to update certificate');
        }
        return response.json();
    },

    /**
     * Revoke (delete) a certificate
     */
    deleteCertificate: async (appId: number, clientId: number, id: number): Promise<void> => {
        const response = await fetch(`${API_BASE}/application/${appId}/client/${clientId}/certificate/${id}`, {
            method: 'DELETE',
        });
        if (!response.ok) {
            throw new Error('Failed to revoke certificate');
        }
    },

    /**
     * List certificates for a client
     */
    listCertificates: async (appId: number, clientId: number, pageNum = 0, size = 10): Promise<{ content: Certificate[], totalElements: number, totalPages: number }> => {
        const response = await fetch(`${API_BASE}/application/${appId}/client/${clientId}/certificate?page=${pageNum}&size=${size}`);
        if (!response.ok) {
            throw new Error('Failed to list certificates');
        }
        const data = await response.json();
        // Adjust for PageResponse structure if matches Backend
        return {
            content: data.content || [],
            totalElements: data.totalElements || 0,
            totalPages: data.totalPages || 0
        };
    },

    /**
     * Validate certificate path and get details
     */
    validateCertificate: async (appId: number, clientId: number, path: string): Promise<Certificate> => {
        const response = await fetch(`${API_BASE}/application/${appId}/client/${clientId}/certificate/validate?path=${encodeURIComponent(path)}`, {
            method: 'POST',
        });
        if (!response.ok) {
            throw new Error('Failed to validate certificate path');
        }
        return response.json();
    },

    /**
     * Fetch certificate detail (legacy/lookup)
     */
    getCertificateDetail: async (certificate: Partial<Certificate>): Promise<Certificate> => {
        const response = await fetch(`${CERT_URL}/fetch-detail`, { // Keeping for fallback if needed, but standardizing on others
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
