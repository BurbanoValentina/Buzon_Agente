import { apiClient } from './apiClient';

export function login(username, password) {
  return apiClient('/admin/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password })
  });
}

export function verifyTelegramOtp(code) {
  return apiClient('/admin/auth/otp/verify', {
    method: 'POST',
    body: JSON.stringify({ code })
  });
}

export function resendTelegramOtp() {
  return apiClient('/admin/auth/otp/resend', {
    method: 'POST'
  });
}

export function recoverWithSecurityQuestion(username, answer) {
  return apiClient('/admin/auth/security-question', {
    method: 'POST',
    body: JSON.stringify({ username, answer })
  });
}

export function logout() {
  return apiClient('/admin/auth/logout', { method: 'POST' });
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
