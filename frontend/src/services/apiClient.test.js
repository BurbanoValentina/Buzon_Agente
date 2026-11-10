import { afterEach, describe, expect, it, vi } from 'vitest';
import { apiClient } from './apiClient';

describe('apiClient structured error and session handling', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('preserves JSON payload on 400 errors for Telegram OTP fallback', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: false,
        status: 400,
        json: async () => ({
          fallbackRequired: true,
          question: '¿Qué representante eres?',
          message: 'Se agotaron los 3 intentos OTP.'
        })
      })
    );

    await expect(apiClient('/admin/auth/otp/verify', { method: 'POST', body: '{}' })).rejects.toMatchObject({
      message: 'Se agotaron los 3 intentos OTP.',
      status: 400,
      payload: {
        fallbackRequired: true,
        question: '¿Qué representante eres?'
      }
    });
  });

  it('marks 401 and 403 responses as SESSION_EXPIRED', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: false,
        status: 401,
        json: async () => ({ message: 'La sesión expiró por inactividad.' })
      })
    );

    await expect(apiClient('/admin/complaints')).rejects.toMatchObject({
      code: 'SESSION_EXPIRED',
      message: 'La sesión expiró por inactividad.'
    });

    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: false,
        status: 403,
        json: async () => {
          throw new Error('empty body');
        }
      })
    );

    await expect(apiClient('/admin/telegram/link', { method: 'POST', body: '{}' })).rejects.toMatchObject({
      code: 'SESSION_EXPIRED',
      status: 403
    });
  });
});
