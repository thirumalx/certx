/**
 * Application API Service
 * Handles all CRUD operations for applications
 */

export interface Application {
  id?: number;
  applicationName: string;
  uniqueId: string;
  status: string;
}

export interface PageRequest {
  pageNo?: number;
  pageSize?: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
}


import { API_BASE } from '../config';

const API_BASE_URL = `${API_BASE}/application`;

export const applicationService = {
  /**
   * Create a new application
   */
  createApplication: async (application: Application): Promise<Application> => {
    const response = await fetch(API_BASE_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(application),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to create application');
    }

    return response.json();
  },

  /**
   * Get application by ID
   */
  getApplication: async (id: number): Promise<Application> => {
    const response = await fetch(`${API_BASE_URL}/${id}`);

    if (!response.ok) {
      throw new Error('Failed to fetch application');
    }

    return response.json();
  },

  /**
   * List all applications with pagination
   */
  listApplications: async (pageNo: number = 0, pageSize: number = 10): Promise<PageResponse<Application>> => {
    const params = new URLSearchParams({
      page: pageNo.toString(),
      size: pageSize.toString(),
    });

    const response = await fetch(`${API_BASE_URL}?${params}`);

    if (!response.ok) {
      throw new Error('Failed to fetch applications');
    }

    return response.json();
  },

  /**
   * Update an application
   */
  updateApplication: async (id: number, application: Application): Promise<Application> => {
    const response = await fetch(`${API_BASE_URL}/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(application),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to update application');
    }

    return response.json();
  },

  /**
   * Delete an application
   */
  deleteApplication: async (id: number): Promise<boolean> => {
    const response = await fetch(`${API_BASE_URL}/${id}`, {
      method: 'DELETE',
    });

    if (!response.ok) {
      throw new Error('Failed to delete application');
    }

    return response.json();
  },
};
