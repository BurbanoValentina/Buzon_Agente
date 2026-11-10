import { apiClient } from './apiClient';

export function getDashboard(params = {}) {
  const query = new URLSearchParams(params).toString();
  return apiClient(`/admin/dashboard${query ? `?${query}` : ''}`);
}
