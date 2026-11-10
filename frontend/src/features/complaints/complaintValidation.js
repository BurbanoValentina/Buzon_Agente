export const CATEGORIES = [
  'Infraestructura',
  'Bienestar',
  'Laboratorio',
  'Profesores',
  'Estudiantes',
  'Horarios',
  'Tecnología',
  'Administrativos',
  'Convivencia',
  'Aulas',
  'Otras situaciones'
];

export function numberIsRequired(category) {
  return category === 'Laboratorio' || category === 'Aulas';
}

export function validateComplaint(values) {
  const errors = {};
  const identified = values.userType === 'identified';
  if (!values.userType) errors.userType = 'Selecciona el tipo de usuario.';
  if (identified && !values.name?.trim()) errors.name = 'El nombre es obligatorio.';
  if (identified && !values.semester?.trim()) errors.semester = 'El semestre es obligatorio.';
  if (!CATEGORIES.includes(values.category)) errors.category = 'Selecciona una categoría válida.';
  if (numberIsRequired(values.category) && !values.locationNumber?.trim()) {
    errors.locationNumber = 'El número es obligatorio para esta categoría.';
  }
  if (values.locationNumber?.length > 50) errors.locationNumber = 'El número no puede superar 50 caracteres.';
  if (values.subject?.length < 5 || values.subject?.length > 200) errors.subject = 'El asunto debe tener entre 5 y 200 caracteres.';
  if (values.description?.length < 20 || values.description?.length > 500) errors.description = 'La descripción debe tener entre 20 y 500 caracteres.';
  if (!values.privacyAccepted) errors.privacyAccepted = 'Debes aceptar el aviso de privacidad.';
  if (values.files?.length > 5) errors.files = 'Puedes adjuntar máximo 5 imágenes.';
  if (values.files?.some((file) => !['image/jpeg', 'image/png'].includes(file.type))) errors.files = 'Solo se permiten imágenes JPG, JPEG o PNG.';
  if (values.files?.some((file) => file.size > 100 * 1024 * 1024)) errors.files = 'Cada imagen puede pesar máximo 100 MB.';
  return errors;
}

export function counters(values) {
  return {
    subject: `${values.subject?.length || 0}/200`,
    description: `${values.description?.length || 0}/500`,
    locationNumber: `${values.locationNumber?.length || 0}/50`
  };
}
