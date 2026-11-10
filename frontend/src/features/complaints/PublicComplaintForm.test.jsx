import { describe, expect, it } from 'vitest';
import { CATEGORIES, numberIsRequired, validateComplaint } from './complaintValidation';

describe('public complaint form rules', () => {
  it('exposes exactly the eleven categories and conditional number', () => {
    expect(CATEGORIES).toHaveLength(11);
    expect(numberIsRequired('Laboratorio')).toBe(true);
    expect(numberIsRequired('Aulas')).toBe(true);
    expect(numberIsRequired('Bienestar')).toBe(false);
  });

  it('requires identity only for identified submissions', () => {
    const anonymous = validateComplaint({
      userType: 'anonymous', category: 'Bienestar', subject: 'Asunto válido',
      description: 'Descripción suficientemente extensa para la queja.', privacyAccepted: true
    });
    expect(anonymous.name).toBeUndefined();
    expect(anonymous.semester).toBeUndefined();
    const identified = validateComplaint({
      userType: 'identified', category: 'Bienestar', subject: 'Asunto válido',
      description: 'Descripción suficientemente extensa para la queja.', privacyAccepted: true
    });
    expect(identified.name).toBeDefined();
    expect(identified.semester).toBeDefined();
  });
});
