import { API_BASE } from '../config';

export interface StatusCount {
    status: string;
    count: number;
}

export interface ExpiryCount {
    month: string;
    count: number;
}

export interface AppCount {
    name: string;
    count: number;
}

export interface DashboardStats {
    totalApplications: number;
    totalClients: number;
    totalCertificates: number;
    statusDistribution: StatusCount[];
    expiryStats: ExpiryCount[];
    topApplications: AppCount[];
}

export const dashboardService = {
    getStats: async (): Promise<DashboardStats> => {
        const response = await fetch(`${API_BASE}/dashboard/stats`);
        if (!response.ok) {
            throw new Error('Failed to fetch dashboard stats');
        }
        return response.json();
    }
};
