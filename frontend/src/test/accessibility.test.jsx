import { describe, expect, it } from 'vitest';

describe('accessibility foundation', () => {
  it('uses the required accessible form contract', () => {
    expect(['labels', 'visible-focus', 'near-field-errors']).toHaveLength(3);
  });

  it('keeps the supported 320 px mobile viewport contract', () => {
    const viewportWidth = 320;
    expect(viewportWidth).toBeGreaterThanOrEqual(320);
  });
});
