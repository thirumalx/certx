import { API_BASE } from '../config';

export interface CrlCheckRunResponse {
  startedAt: string;
  finishedAt: string;
  totalActive: number;
  processed: number;
  revoked: number;
  skipped: number;
  failed: number;
  logs: CertificateLog[];
}

export interface CertificateLog {
  serialNumber: string;
  status: string;
  message: string;
}

export interface CertificateReportItem {
  category: string;
  serialNumber: string;
  clientName: string | null;
  applicationName: string | null;
  clientEmail: string | null;
  clientPhone: string | null;
  expiryDate: string | null;
  revokedOn: string | null;
}

export interface CertificateReportResponse {
  message: string;
  items: CertificateReportItem[];
}

export const schedulerService = {
  runCrlCheck: async (): Promise<CrlCheckRunResponse> => {
    const response = await fetch(`${API_BASE}/scheduler/crl-check/run`, {
      method: 'POST'
    });
    if (!response.ok) {
      throw new Error('Failed to run CRL check');
    }
    return response.json();
  },
  generateReport: async (): Promise<CertificateReportResponse> => {
    const response = await fetch(`${API_BASE}/scheduler/generate-report`, {
      method: 'POST'
    });
    if (!response.ok) {
      throw new Error('Failed to generate report');
    }
    return response.json();
  }
};
