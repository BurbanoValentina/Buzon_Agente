export function createErrorState(error) {
  return { message: error?.message || 'Ocurrió un error.', code: error?.code || 'UNKNOWN' };
}
