import { apiClient } from './apiClient';

export function getFormOptions() {
  return apiClient('/public/form-options');
}

export function submitComplaint(values, files = []) {
  const form = new FormData();
  form.append('complaint', new Blob([JSON.stringify(values)], { type: 'application/json' }));
  files.forEach((file) => form.append('evidence', file));
  return apiClient('/public/complaints', { method: 'POST', body: form, headers: {} });
}
