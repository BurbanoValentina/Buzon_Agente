import { useCallback, useState } from 'react';

export function useAsyncState() {
  const [state, setState] = useState({ loading: false, error: null });
  const run = useCallback(async (operation) => {
    setState({ loading: true, error: null });
    try {
      const value = await operation();
      setState({ loading: false, error: null });
      return value;
    } catch (error) {
      setState({ loading: false, error });
      throw error;
    }
  }, []);
  return { ...state, run };
}
