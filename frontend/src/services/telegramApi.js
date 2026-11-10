import { apiClient } from './apiClient';

export function getTelegramStatus() {
  return apiClient('/admin/telegram/status');
}

export function getTelegramBotInfo() {
  return apiClient('/admin/telegram/bot-info');
}

export function startTelegramLink(accountId) {
  return apiClient('/admin/telegram/link', {
    method: 'POST',
    body: JSON.stringify({ accountId })
  });
}

export function confirmTelegramLink(accountId, code) {
  return apiClient('/admin/telegram/confirm-link', {
    method: 'POST',
    body: JSON.stringify({ accountId, code })
  });
}

export function unlinkTelegram() {
  return apiClient('/admin/telegram/unlink', { method: 'POST' });
}

export function setTelegramNotifications(enabled) {
  return apiClient('/admin/telegram/notifications', {
    method: 'PATCH',
    body: JSON.stringify({ enabled })
  });
}

export function sendTelegramTest() {
  return apiClient('/admin/telegram/test', { method: 'POST' });
}
