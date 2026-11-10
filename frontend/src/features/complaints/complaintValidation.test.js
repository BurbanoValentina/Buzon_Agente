import { describe, expect, it } from 'vitest';
import { counters, validateComplaint } from './complaintValidation';

describe('complaint validation limits', () => {
  const base = {
    userType: 'anonymous', category: 'Laboratorio', locationNumber: 'A-1',
    subject: 'Asunto', description: 'Descripción válida con más de veinte caracteres.',
    privacyAccepted: true
  };

  it('enforces number, subject and description limits', () => {
    expect(validateComplaint({ ...base, locationNumber: 'x'.repeat(51) }).locationNumber).toBeDefined();
    expect(validateComplaint({ ...base, subject: 'x'.repeat(4) }).subject).toBeDefined();
    expect(validateComplaint({ ...base, description: 'x'.repeat(19) }).description).toBeDefined();
    expect(validateComplaint({ ...base, subject: 'x'.repeat(201) }).subject).toBeDefined();
    expect(validateComplaint({ ...base, description: 'x'.repeat(501) }).description).toBeDefined();
  });

  it('returns visible counters', () => {
    expect(counters(base)).toEqual({ subject: '6/200', description: '48/500', locationNumber: '3/50' });
  });
});
