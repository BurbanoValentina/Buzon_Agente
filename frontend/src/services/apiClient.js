const API_BASE_URL = '/api/v1';

export async function apiClient(path, options = {}) {
  const headers = { ...options.headers };
  if (!(options.body instanceof FormData)) headers['Content-Type'] = 'application/json';
  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: 'same-origin',
    headers,
    ...options
  });
  if (response.status === 401 || response.status === 403) {
    const payload = await response.json().catch(() => ({}));
    const message =
      payload.message || 'Tu sesión expiró o no está autenticada. Inicia sesión nuevamente.';
    if (typeof window !== 'undefined' && typeof window.dispatchEvent === 'function' && window.CustomEvent) {
      window.dispatchEvent(new window.CustomEvent('ucc:session-expired', { detail: message }));
    }
    const error = new Error(message);
    error.code = 'SESSION_EXPIRED';
    error.payload = payload;
    error.status = response.status;
    throw error;
  }
  if (!response.ok) {
    const payload = await response.json().catch(() => ({}));
    const error = new Error(payload.message || 'No fue posible completar la solicitud.');
    error.payload = payload;
    error.status = response.status;
    throw error;
  }
  return response.status === 204 ? undefined : response.json();
}
