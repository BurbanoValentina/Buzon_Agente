import { useEffect, useState } from 'react';
import { Icon } from '../../../components/Icon';
import { updateComplaintStatus } from '../../../services/statusApi';

function getAvailableTransitions(current) {
  switch (current) {
    case 'COMPLETADA':
      return [['PENDIENTE', 'Reabrir caso (pasa a Pendiente)']];
    case 'NO_COMPLETADA':
      return [
        ['PENDIENTE', 'Reabrir caso (pasa a Pendiente)'],
        ['COMPLETADA', 'Completar / Resolver']
      ];
    case 'PENDIENTE':
    default:
      return [
        ['COMPLETADA', 'Completar / Resolver'],
        ['NO_COMPLETADA', 'Marcar No completada']
      ];
  }
}

export function StatusControls({ complaintId, currentStatus, onUpdated }) {
  const availableTransitions = getAvailableTransitions(currentStatus);
  const [status, setStatus] = useState(availableTransitions[0]?.[0] || 'PENDIENTE');
  const [comment, setComment] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const transitions = getAvailableTransitions(currentStatus);
    setStatus(transitions[0]?.[0] || 'PENDIENTE');
    setError('');
    setSuccess('');
    setComment('');
  }, [complaintId, currentStatus]);

  async function submit(event) {
    event.preventDefault();
    setSuccess('');
    if (!comment.trim()) {
      setError('El comentario de gestión es obligatorio.');
      return;
    }
    if (status === currentStatus) {
      setError('El caso ya se encuentra en este estado. Selecciona una nueva transición.');
      return;
    }
    setSaving(true);
    setError('');
    try {
      const history = await updateComplaintStatus(complaintId, status, comment);
      setComment('');
      setSuccess('Estado actualizado en el historial.');
      onUpdated?.(status, history);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <form
      onSubmit={submit}
      aria-label="Gestionar estado"
      style={{
        background: 'var(--bg-subtle)',
        padding: '1.15rem',
        borderRadius: 'var(--radius-md)',
        border: '1px solid var(--border-subtle)',
        gap: '0.9rem',
        maxWidth: '100%',
        boxSizing: 'border-box',
        overflow: 'hidden'
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
        <Icon name="sync_alt" size={19} style={{ color: 'var(--primary)' }} />
        <h4 style={{ margin: 0, fontSize: '0.95rem', color: 'var(--navy-900)' }}>
          Gestión y Cambio de Estado
        </h4>
      </div>

      <div>
        <label htmlFor="status">Nuevo estado</label>
        <select
          id="status"
          value={status}
          onChange={(event) => setStatus(event.target.value)}
          style={{ width: '100%', maxWidth: '100%', boxSizing: 'border-box' }}
        >
          {availableTransitions.map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </select>
      </div>

      <div>
        <label htmlFor="status-comment">Comentario o Motivo Obligatorio *</label>
        <textarea
          id="status-comment"
          placeholder="Documenta la acción realizada, motivo de no cumplimiento o razón de reapertura..."
          value={comment}
          onChange={(event) => setComment(event.target.value)}
          rows="3"
          style={{
            minHeight: '86px',
            width: '100%',
            maxWidth: '100%',
            boxSizing: 'border-box',
            overflowWrap: 'anywhere',
            wordBreak: 'break-word'
          }}
        />
      </div>

      {error && (
        <p role="alert" style={{ overflowWrap: 'anywhere', wordBreak: 'break-word' }}>
          <Icon name="error" size={17} />
          <span>{error}</span>
        </p>
      )}
      {success && (
        <p role="status" style={{ overflowWrap: 'anywhere', wordBreak: 'break-word' }}>
          <Icon name="check_circle" filled size={17} />
          <span>{success}</span>
        </p>
      )}

      <button type="submit" disabled={saving} className="btn-primary btn-sm">
        {saving ? (
          <>
            <span className="spinner" aria-hidden="true" />
            <span>Guardando...</span>
          </>
        ) : (
          <>
            <Icon name="save" size={17} />
            <span>Guardar Estado</span>
          </>
        )}
      </button>
    </form>
  );
}
