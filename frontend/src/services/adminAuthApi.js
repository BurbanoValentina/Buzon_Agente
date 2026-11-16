import { apiClient } from './apiClient';

export async function login(username, password) {
  const result = await apiClient('/admin/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password })
  });
  if (result?.token && typeof window !== 'undefined') {
    window.sessionStorage?.setItem('ucc_admin_token', result.token);
    window.localStorage?.setItem('ucc_admin_token', result.token);
  }
  return result;
}

export async function verifyTelegramOtp(code) {
  const result = await apiClient('/admin/auth/otp/verify', {
    method: 'POST',
    body: JSON.stringify({ code })
  });
  if (result?.token && typeof window !== 'undefined') {
    window.sessionStorage?.setItem('ucc_admin_token', result.token);
    window.localStorage?.setItem('ucc_admin_token', result.token);
  }
  return result;
}

export function resendTelegramOtp() {
  return apiClient('/admin/auth/otp/resend', {
    method: 'POST'
  });
}

export async function recoverWithSecurityQuestion(username, answer) {
  const result = await apiClient('/admin/auth/security-question', {
    method: 'POST',
    body: JSON.stringify({ username, answer })
  });
  if (result?.token && typeof window !== 'undefined') {
    window.sessionStorage?.setItem('ucc_admin_token', result.token);
    window.localStorage?.setItem('ucc_admin_token', result.token);
  }
  return result;
}

export async function logout() {
  try {
    return await apiClient('/admin/auth/logout', { method: 'POST' });
  } finally {
    if (typeof window !== 'undefined') {
      window.sessionStorage?.removeItem('ucc_admin_token');
      window.localStorage?.removeItem('ucc_admin_token');
    }
  }
}

export function getAdminSession() {
  return apiClient('/admin/auth/session');
}

export function requestCredentialsOtp() {
  return apiClient('/admin/account/credentials/otp', { method: 'POST' });
}

export function changeCredentials(credentials) {
  return apiClient('/admin/account/credentials', {
    method: 'PATCH',
    body: JSON.stringify(credentials)
  });
}

export function getSecurityQuestion() {
  return apiClient('/admin/account/security-question');
}
