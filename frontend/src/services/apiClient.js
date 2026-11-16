const RAW_API_URL = (typeof import.meta !== 'undefined' && import.meta.env?.VITE_API_URL) || '';
const API_BASE_URL = RAW_API_URL.replace(/\/+$/, '') + '/api/v1';

export async function apiClient(path, options = {}) {
  const headers = { ...options.headers };
  if (!(options.body instanceof FormData)) headers['Content-Type'] = 'application/json';
  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: RAW_API_URL ? 'include' : 'same-origin',
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
  if (response.status === 204) return undefined;
  try {
    return await response.json();
  } catch {
    throw new Error(
      'El servidor no devolvió una respuesta JSON válida. Verifica que el backend esté en ejecución y que VITE_API_URL apunte al servidor correcto.'
    );
  }
}
