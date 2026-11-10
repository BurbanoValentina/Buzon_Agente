import { useMemo, useState } from 'react';
import { Icon } from '../../components/Icon';
import { submitComplaint } from '../../services/complaintsApi';
import { CATEGORIES, counters, numberIsRequired, validateComplaint } from './complaintValidation';
import { EvidenceUploader } from './EvidenceUploader';
import { PrivacyNotice } from './PrivacyNotice';

const initialValues = {
  userType: 'anonymous',
  name: '',
  semester: '',
  category: '',
  locationNumber: '',
  subject: '',
  description: '',
  privacyAccepted: false
};

const CATEGORY_ICONS = {
  Infraestructura: 'domain',
  Bienestar: 'favorite',
  Laboratorio: 'science',
  Profesores: 'school',
  Estudiantes: 'groups',
  Horarios: 'schedule',
  Tecnología: 'computer',
  Administrativos: 'admin_panel_settings',
  Convivencia: 'diversity_3',
  Aulas: 'meeting_room',
  'Otras situaciones': 'more_horiz'
};

export function PublicComplaintForm({ onSubmitted = () => {} }) {
  const [values, setValues] = useState(initialValues);
  const [files, setFiles] = useState([]);
  const [errors, setErrors] = useState({});
  const [status, setStatus] = useState({ loading: false, message: '', error: '', receipt: null });
  const [copiedId, setCopiedId] = useState(false);

  const count = useMemo(() => counters(values), [values]);
  const update = (field, value) => setValues((current) => ({ ...current, [field]: value }));

  const progress = useMemo(() => {
    const checks = [
      Boolean(values.userType),
      values.userType === 'anonymous' || (values.name.trim().length > 0 && values.semester.trim().length > 0),
      CATEGORIES.includes(values.category),
      !numberIsRequired(values.category) || values.locationNumber.trim().length > 0,
      values.subject.length >= 5 && values.subject.length <= 200,
      values.description.length >= 20 && values.description.length <= 500,
      values.privacyAccepted
    ];
    const passed = checks.filter(Boolean).length;
    return Math.round((passed / checks.length) * 100);
  }, [values]);

  async function submit(event) {
    event.preventDefault();
    const nextErrors = validateComplaint({ ...values, files });
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length) return;
    setStatus({ loading: true, message: '', error: '', receipt: null });
    try {
      const response = await submitComplaint(values, files);
      setStatus({
        loading: false,
        message: response.message,
        error: '',
        receipt: response
      });
      setValues(initialValues);
      setFiles([]);
      setErrors({});
      onSubmitted(response);
    } catch (error) {
      setStatus({ loading: false, message: '', error: error.message, receipt: null });
    }
  }

  async function copyComplaintId(id) {
    try {
      await window.navigator?.clipboard?.writeText(id);
      setCopiedId(true);
      window.setTimeout(() => setCopiedId(false), 2000);
    } catch {
      // Clipboard API unavailable in restricted contexts
    }
  }

  const subjectLen = values.subject.length;
  const descLen = values.description.length;
  const subjectValid = subjectLen >= 5 && subjectLen <= 200;
  const descValid = descLen >= 20 && descLen <= 500;
  const selectedCategoryIcon = CATEGORY_ICONS[values.category] || 'category';

  return (
    <form onSubmit={submit} noValidate className="complaint-form-styled">
      {/* Barra de progreso interactiva */}
      <div className="form-progress-wrap">
        <div className="form-progress-head">
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem' }}>
            <Icon name="task_alt" size={17} style={{ color: progress === 100 ? 'var(--success)' : 'var(--primary)' }} />
            <span>Completitud del reporte</span>
          </span>
          <span className="font-mono" style={{ color: progress === 100 ? 'var(--success-text)' : 'var(--primary-hover)' }}>
            {progress}%
          </span>
        </div>
        <div className="progress-track">
          <div className="progress-fill" style={{ width: `${progress}%` }} />
        </div>
      </div>

      {/* Selector de anonimato / identificación */}
      <fieldset>
        <legend>Modalidad de presentación</legend>
        <div className="user-type-selector">
          <label className={`user-type-option ${values.userType === 'anonymous' ? 'selected' : ''}`}>
            <input
              type="radio"
              name="userType"
              value="anonymous"
              checked={values.userType === 'anonymous'}
              onChange={(event) => update('userType', event.target.value)}
            />
            <span className="option-icon-pill">
              <Icon name="visibility_off" size={18} />
            </span>
            <span>
              <span className="option-title">Anónimo</span>
              <span className="option-desc">Sin registro de nombre ni semestre</span>
            </span>
          </label>

          <label className={`user-type-option ${values.userType === 'identified' ? 'selected' : ''}`}>
            <input
              type="radio"
              name="userType"
              value="identified"
              checked={values.userType === 'identified'}
              onChange={(event) => update('userType', event.target.value)}
            />
            <span className="option-icon-pill">
              <Icon name="person" size={18} />
            </span>
            <span>
              <span className="option-title">Identificado</span>
              <span className="option-desc">Permite contacto o respuesta directa</span>
            </span>
          </label>
        </div>
      </fieldset>

      {/* Campos de identidad condicional */}
      {values.userType === 'identified' && (
        <div className="grid-2 animate-in">
          <div>
            <label htmlFor="name">Nombre completo *</label>
            <div className="input-with-icon">
              <Icon name="badge" size={18} className="input-leading-icon" />
              <input
                id="name"
                placeholder="Ej. Juan Sebastián Pérez"
                value={values.name}
                onChange={(event) => update('name', event.target.value)}
              />
            </div>
            {errors.name && (
              <p role="alert">
                <Icon name="error" size={17} />
                <span>{errors.name}</span>
              </p>
            )}
          </div>

          <div>
            <label htmlFor="semester">Semestre académico *</label>
            <div className="input-with-icon">
              <Icon name="school" size={18} className="input-leading-icon" />
              <input
                id="semester"
                placeholder="Ej. 6to Semestre"
                value={values.semester}
                onChange={(event) => update('semester', event.target.value)}
              />
            </div>
            {errors.semester && (
              <p role="alert">
                <Icon name="error" size={17} />
                <span>{errors.semester}</span>
              </p>
            )}
          </div>
        </div>
      )}

      {/* Categoría con ícono dinámico */}
      <div>
        <label htmlFor="category">Categoría de la situación *</label>
        <div className="input-with-icon">
          <Icon name={selectedCategoryIcon} size={19} className="input-leading-icon" style={{ color: 'var(--primary)' }} />
          <select
            id="category"
            value={values.category}
            onChange={(event) => update('category', event.target.value)}
          >
            <option value="">Selecciona una categoría de la facultad...</option>
            {CATEGORIES.map((category) => (
              <option key={category} value={category}>
                {category}
              </option>
            ))}
          </select>
        </div>
        {errors.category && (
          <p role="alert">
            <Icon name="error" size={17} />
            <span>{errors.category}</span>
          </p>
        )}
      </div>

      {/* Número condicional para Laboratorio y Aulas */}
      {numberIsRequired(values.category) && (
        <div className="animate-in">
          <div className="field-header">
            <label htmlFor="locationNumber">
              Número de {values.category === 'Laboratorio' ? 'Laboratorio' : 'Aula'} *
            </label>
            <span className={`char-counter-pill ${values.locationNumber.trim() ? 'valid' : ''}`}>
              {count.locationNumber}
            </span>
          </div>
          <div className="input-with-icon">
            <Icon name="pin" size={18} className="input-leading-icon" />
            <input
              id="locationNumber"
              maxLength="50"
              placeholder={values.category === 'Laboratorio' ? 'Ej. Lab Sistemas 302' : 'Ej. Bloque B · Aula 104'}
              value={values.locationNumber}
              onChange={(event) => update('locationNumber', event.target.value)}
            />
          </div>
          <div className="context-hint">
            <Icon name="info" size={17} />
            <span>Indicar el salón o laboratorio exacto agiliza la verificación y atención técnica.</span>
          </div>
          {errors.locationNumber && (
            <p role="alert">
              <Icon name="error" size={17} />
              <span>{errors.locationNumber}</span>
            </p>
          )}
        </div>
      )}

      {/* Asunto */}
      <div>
        <div className="field-header">
          <label htmlFor="subject">Asunto breve * (5 a 200 caracteres)</label>
          <span className={`char-counter-pill ${subjectLen > 0 ? (subjectValid ? 'valid' : 'warn') : ''}`}>
            {count.subject}
          </span>
        </div>
        <div className="input-with-icon">
          <Icon name="title" size={18} className="input-leading-icon" />
          <input
            id="subject"
            minLength="5"
            maxLength="200"
            placeholder="Resume la situación en una línea clara..."
            value={values.subject}
            onChange={(event) => update('subject', event.target.value)}
          />
        </div>
        <div className="field-meter-track">
          <div
            className={`field-meter-fill ${subjectValid ? 'valid' : ''}`}
            style={{ width: `${Math.min(100, (subjectLen / 200) * 100)}%` }}
          />
        </div>
        {errors.subject && (
          <p role="alert">
            <Icon name="error" size={17} />
            <span>{errors.subject}</span>
          </p>
        )}
      </div>

      {/* Descripción */}
      <div>
        <div className="field-header">
          <label htmlFor="description">Detalles de la queja * (20 a 500 caracteres)</label>
          <span className={`char-counter-pill ${descLen > 0 ? (descValid ? 'valid' : 'warn') : ''}`}>
            {count.description}
          </span>
        </div>
        <textarea
          id="description"
          minLength="20"
          maxLength="500"
          placeholder="Describe con precisión qué ocurrió, ubicación, frecuencia y detalles relevantes para su solución..."
          value={values.description}
          onChange={(event) => update('description', event.target.value)}
        />
        <div className="field-meter-track">
          <div
            className={`field-meter-fill ${descValid ? 'valid' : ''}`}
            style={{ width: `${Math.min(100, (descLen / 500) * 100)}%` }}
          />
        </div>
        {errors.description && (
          <p role="alert">
            <Icon name="error" size={17} />
            <span>{errors.description}</span>
          </p>
        )}
      </div>

      {/* Evidencia fotográfica */}
      <EvidenceUploader files={files} onChange={setFiles} error={errors.files} />

      {/* Aviso de privacidad */}
      <PrivacyNotice
        checked={values.privacyAccepted}
        onChange={(checked) => update('privacyAccepted', checked)}
        error={errors.privacyAccepted}
      />

      {/* Botón de envío */}
      <button type="submit" disabled={status.loading} className="btn-primary btn-lg">
        {status.loading ? (
          <>
            <span className="spinner" aria-hidden="true" />
            <span>Enviando reporte seguro...</span>
          </>
        ) : (
          <>
            <Icon name="send" size={19} />
            <span>Enviar Queja Académica</span>
          </>
        )}
      </button>

      {/* Error remoto de envío */}
      {status.error && (
        <p role="alert">
          <Icon name="error" size={18} />
          <span>{status.error}</span>
        </p>
      )}

      {/* Comprobante de recepción */}
      {status.message && (
        <div role="status" className="receipt-card animate-in" style={{ display: 'flex' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', color: 'var(--success-text)' }}>
            <Icon name="check_circle" filled size={22} />
            <span style={{ fontWeight: 700, fontSize: '0.96rem' }}>{status.message}</span>
          </div>
          {status.receipt?.complaintId && (
            <div className="receipt-id-row">
              <div>
                <span style={{ fontSize: '0.72rem', color: 'var(--slate-500)', display: 'block', textTransform: 'uppercase', fontWeight: 700 }}>
                  Radicado de seguimiento
                </span>
                <strong className="font-mono" style={{ fontSize: '0.88rem', color: 'var(--navy-900)' }}>
                  {status.receipt.complaintId}
                </strong>
              </div>
              <button
                type="button"
                className="btn-secondary btn-sm"
                onClick={() => copyComplaintId(status.receipt.complaintId)}
              >
                <Icon name={copiedId ? 'check' : 'content_copy'} size={15} />
                <span>{copiedId ? 'Copiado' : 'Copiar ID'}</span>
              </button>
            </div>
          )}
        </div>
      )}
    </form>
  );
}
