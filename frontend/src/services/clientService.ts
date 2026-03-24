/**
 * Client API Service
 * Handles all CRUD operations for clients under an application
 */

import { API_BASE } from '../config';
import { type PageResponse } from './applicationService';

export interface Client {
    id?: number;
    applicationId?: number;
    uniqueId?: string;
    name: string;
    email?: string;
    mobileNumber?: string;
    status: string;
    assignedUserCount?: number;
    certificateCount?: number;
}

export interface AssignedUser {
    id: number;
    userId?: string;
    name?: string;
    email?: string;
    mobileNumber?: string;
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
     * Get a client by unique ID (for auto-fill)
     */
    getClientByUniqueId: async (
        applicationId: number,
        uniqueId: string
    ): Promise<Client | null> => {
        const response = await fetch(`${baseUrl(applicationId)}/unique/${encodeURIComponent(uniqueId)}`);
        if (response.status === 404) return null;
        if (!response.ok) throw new Error('Failed to fetch client by unique ID');
        return response.json();
    },

    /**
     * List clients with pagination
     */
    listClients: async (
        applicationId: number,
        pageNo: number = 0,
        pageSize: number = 10,
        status: string = 'ALL'
    ): Promise<PageResponse<Client>> => {
        const params = new URLSearchParams({
            page: pageNo.toString(),
            size: pageSize.toString(),
            status: status,
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

    listAssignees: async (applicationId: number, clientId: number): Promise<AssignedUser[]> => {
        const response = await fetch(`${baseUrl(applicationId)}/${clientId}/assignees`);
        if (!response.ok) throw new Error('Failed to fetch assignees');
        return response.json();
    },

    assignUser: async (
        applicationId: number,
        clientId: number,
        payload: { email: string; name?: string; mobileNumber?: string; userId?: string }
    ): Promise<AssignedUser> => {
        const response = await fetch(`${baseUrl(applicationId)}/${clientId}/assignees`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload),
        });
        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            throw new Error(error.message || 'Failed to assign user');
        }
        return response.json();
    },

    updateAssignee: async (
        applicationId: number,
        clientId: number,
        userId: number,
        payload: { email: string; name?: string; mobileNumber?: string; userId?: string }
    ): Promise<AssignedUser> => {
        const response = await fetch(`${baseUrl(applicationId)}/${clientId}/assignees/${userId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload),
        });
        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            throw new Error(error.message || 'Failed to update assignee');
        }
        return response.json();
    },

    removeAssignee: async (applicationId: number, clientId: number, userId: number): Promise<boolean> => {
        const response = await fetch(`${baseUrl(applicationId)}/${clientId}/assignees/${userId}`, {
            method: 'DELETE',
        });
        if (!response.ok) throw new Error('Failed to remove assignee');
        return response.json();
    },

    /**
     * Notify client about a certificate
     */
    notifyClient: async (clientId: number, certificateId: number): Promise<number> => {
        const response = await fetch(`${API_BASE}/notifications/notify/client/${clientId}/certificate/${certificateId}`, {
            method: 'POST',
        });
        if (!response.ok) throw new Error('Failed to send notification');
        return response.json();
    },
};
