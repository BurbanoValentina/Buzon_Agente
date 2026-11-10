import { apiClient } from './apiClient';

export function updateComplaintStatus(id, status, comment) {
  return apiClient(`/admin/complaints/${encodeURIComponent(id)}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status, comment }),
    headers: { 'Content-Type': 'application/json' }
  });
}

export function getStatusHistory(id) {
  return apiClient(`/admin/complaints/${encodeURIComponent(id)}/status/history`);
}

export function getMonthlyFollowUp(params = {}) {
  const query = new URLSearchParams(params).toString();
  return apiClient(`/admin/follow-up/months${query ? `?${query}` : ''}`);
}