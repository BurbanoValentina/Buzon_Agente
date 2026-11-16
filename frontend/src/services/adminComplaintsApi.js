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
  const RAW_API_URL = (typeof import.meta !== 'undefined' && import.meta.env && import.meta.env.VITE_API_URL) || '';
  const API_BASE_URL = RAW_API_URL.replace(/\/+$/, '') + '/api/v1';
  const token =
    typeof window !== 'undefined'
      ? window.sessionStorage?.getItem('ucc_admin_token') || window.localStorage?.getItem('ucc_admin_token')
      : null;
  const headers = {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
    headers['X-Admin-Token'] = token;
  }
  const response = await fetch(`${API_BASE_URL}/admin/complaints/export.xlsx${query ? `?${query}` : ''}`, {
    credentials: RAW_API_URL ? 'include' : 'same-origin',
    headers
  });
  if (response.status === 401 || response.status === 403) {
    if (typeof window !== 'undefined') {
      window.sessionStorage?.removeItem('ucc_admin_token');
      window.localStorage?.removeItem('ucc_admin_token');
    }
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
