// Centralized frontend configuration
// Exposes `API_BASE` which reads `VITE_API_BASE_URL` or falls back to localhost:8080
const env = (import.meta as any).env || {};
export const API_BASE: string = env.API_BASE_URL ?? 'http://localhost:8080';

export default API_BASE;
