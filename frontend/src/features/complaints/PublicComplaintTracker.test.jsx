import { describe, expect, it } from 'vitest';
import { PublicComplaintTracker } from './PublicComplaintTracker';
import { getComplaintStatus } from '../../services/complaintsApi';

describe('PublicComplaintTracker contract', () => {
  it('exports the PublicComplaintTracker component', () => {
    expect(PublicComplaintTracker).toBeDefined();
    expect(typeof PublicComplaintTracker).toBe('function');
  });

  it('exports the getComplaintStatus api function', () => {
    expect(getComplaintStatus).toBeDefined();
    expect(typeof getComplaintStatus).toBe('function');
  });
});
