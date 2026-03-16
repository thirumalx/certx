import { API_BASE } from '../config';

export interface CrlCheckRunResponse {
  startedAt: string;
  finishedAt: string;
  totalActive: number;
  processed: number;
  revoked: number;
  skipped: number;
  failed: number;
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
  }
};
