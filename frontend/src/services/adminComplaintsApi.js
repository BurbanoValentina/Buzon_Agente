import { apiClient } from './apiClient';

function queryString(filters = {}) {
  const params = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') params.set(key, value);
  });
  return params.toString();
}

export function listComplaints(filters) {
  const query = queryString(filters);
  return apiClient(`/admin/complaints${query ? `?${query}` : ''}`);
}

export function getComplaint(id) {
  return apiClient(`/admin/complaints/${encodeURIComponent(id)}`);
}

export async function downloadComplaints(filters) {
  const query = queryString(filters);
  const response = await fetch(`/api/v1/admin/complaints/export.xlsx${query ? `?${query}` : ''}`, {
    credentials: 'same-origin'
  });
  if (response.status === 401 || response.status === 403) {
    const message = 'Tu sesión expiró o no está autenticada. Inicia sesión nuevamente.';
    if (typeof window !== 'undefined' && typeof window.dispatchEvent === 'function' && window.CustomEvent) {
      window.dispatchEvent(new window.CustomEvent('ucc:session-expired', { detail: message }));
    }
    const error = new Error(message);
    error.code = 'SESSION_EXPIRED';
    error.status = response.status;
    throw error;
  }
  if (!response.ok) throw new Error('No fue posible descargar el Excel.');
  return response.blob();
}
