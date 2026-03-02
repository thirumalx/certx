/**
 * Client API Service
 * Handles all CRUD operations for clients under an application
 */

import { API_BASE } from '../config';
import { type PageResponse } from './applicationService';

export interface Client {
    id?: number;
    applicationId?: number;
    name: string;
    email?: string;
    mobileNumber?: string;
    status: string;
}

const baseUrl = (applicationId: number) =>
    `${API_BASE}/application/${applicationId}/client`;

export const clientService = {
    /**
     * Create a new client under an application
     */
    createClient: async (applicationId: number, client: Client): Promise<Client> => {
        const response = await fetch(baseUrl(applicationId), {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(client),
        });
        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            throw new Error(error.message || 'Failed to create client');
        }
        return response.json();
    },

    /**
     * Get a client by ID
     */
    getClient: async (applicationId: number, id: number): Promise<Client> => {
        const response = await fetch(`${baseUrl(applicationId)}/${id}`);
        if (!response.ok) throw new Error('Failed to fetch client');
        return response.json();
    },

    /**
     * List clients with pagination
     */
    listClients: async (
        applicationId: number,
        pageNo: number = 0,
        pageSize: number = 10
    ): Promise<PageResponse<Client>> => {
        const params = new URLSearchParams({
            page: pageNo.toString(),
            size: pageSize.toString(),
        });
        const response = await fetch(`${baseUrl(applicationId)}?${params}`);
        if (!response.ok) throw new Error('Failed to fetch clients');
        return response.json();
    },

    /**
     * Update a client
     */
    updateClient: async (
        applicationId: number,
        id: number,
        client: Client
    ): Promise<Client> => {
        const response = await fetch(`${baseUrl(applicationId)}/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(client),
        });
        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            throw new Error(error.message || 'Failed to update client');
        }
        return response.json();
    },

    /**
     * Delete (soft-delete) a client
     */
    deleteClient: async (applicationId: number, id: number): Promise<boolean> => {
        const response = await fetch(`${baseUrl(applicationId)}/${id}`, {
            method: 'DELETE',
        });
        if (!response.ok) throw new Error('Failed to delete client');
        return response.json();
    },
};
