import { useEffect } from 'react';

export function useSessionExpiry(onExpired, timeoutMs = 10 * 60 * 60 * 1000) {
  useEffect(() => {
    const timer = window.setTimeout(onExpired, timeoutMs);
    return () => window.clearTimeout(timer);
  }, [onExpired, timeoutMs]);
}
