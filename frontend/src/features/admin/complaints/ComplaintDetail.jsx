import { useState } from 'react';
import { Icon } from '../../../components/Icon';
import { StatusControls } from '../status/StatusControls';
import { StatusHistory } from '../status/StatusHistory';

function getStatusBadge(status) {
  if (status === 'COMPLETADA') return { cls: 'completada', icon: 'check_circle', text: 'Completada' };
  if (status === 'NO_COMPLETADA') return { cls: 'no-completada', icon: 'cancel', text: 'No completada' };
  return { cls: 'pendiente', icon: 'schedule', text: 'Pendiente' };
}

export function ComplaintDetail({ complaint, history = [], onStatusUpdated, onClose }) {
  const [copied, setCopied] = useState(false);

  if (!complaint) return null;

  const badge = getStatusBadge(complaint.status);

  async function copyId() {
    try {
      await window.navigator?.clipboard?.writeText(complaint.id);
      setCopied(true);
      window.setTimeout(() => setCopied(false), 1800);
    } catch {
      // Clipboard unsupported
    }
  }

  return (
    <section aria-label="Detalle de queja" className="expediente-panel animate-in">
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start',
          flexWrap: 'wrap',
          gap: '0.75rem',
          borderBottom: '1px solid var(--border-subtle)',
          paddingBottom: '1rem'
        }}
      >
        <div>
          <span
            style={{
              fontSize: '0.73rem',
              fontWeight: 800,
              color: 'var(--primary-hover)',
              textTransform: 'uppercase',
              letterSpacing: '0.06em'
            }}
          >
            Expediente Académico
          </span>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginTop: '0.2rem', flexWrap: 'wrap' }}>
            <h2 className="font-mono" style={{ margin: 0, fontSize: '1.15rem', color: 'var(--navy-900)' }}>
              {complaint.id}
            </h2>
            <button type="button" className="btn-ghost btn-sm" onClick={copyId} title="Copiar ID completo">
              <Icon name={copied ? 'check' : 'content_copy'} size={15} />
              <span>{copied ? 'Copiado' : 'Copiar'}</span>
            </button>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.55rem', flexWrap: 'wrap' }}>
          <span className={`status-pill ${badge.cls}`}>
            <Icon name={badge.icon} filled size={15} />
            <span>{badge.text}</span>
          </span>
          <span className="status-pill" style={{ background: 'var(--bg-muted)', color: 'var(--navy-800)' }}>
            <Icon name={complaint.anonymous ? 'visibility_off' : 'person'} size={15} />
            <span>{complaint.anonymous ? 'Anónima' : 'Identificada'}</span>
          </span>
          {onClose && (
            <button type="button" className="btn-secondary btn-sm" onClick={onClose} aria-label="Cerrar expediente">
              <Icon name="close" size={16} />
            </button>
          )}
        </div>
      </div>

      <div className="expediente-split">
        {/* Columna izquierda: información y evidencias */}
        <div>
          <div className="meta-grid">
            <div className="meta-tile">
              <span className="meta-label">Categoría</span>
              <strong style={{ color: 'var(--navy-900)' }}>{complaint.category}</strong>
            </div>

            {complaint.locationNumber && (
              <div className="meta-tile">
                <span className="meta-label">Ubicación / Número</span>
                <strong style={{ color: 'var(--navy-900)' }}>{complaint.locationNumber}</strong>
              </div>
            )}

            <div className="meta-tile">
              <span className="meta-label">Fecha de Registro</span>
              <strong style={{ color: 'var(--navy-900)' }}>
                {complaint.registeredAt ? new Date(complaint.registeredAt).toLocaleString() : 'N/A'}
              </strong>
            </div>

            {complaint.completedAt && (
              <div className="meta-tile">
                <span className="meta-label">Fecha de Cumplimiento</span>
                <strong style={{ color: 'var(--success-text)' }}>
                  {new Date(complaint.completedAt).toLocaleString()}
                </strong>
              </div>
            )}

            {!complaint.anonymous && (
              <>
                <div className="meta-tile highlight">
                  <span className="meta-label" style={{ color: 'var(--primary-hover)' }}>Estudiante</span>
                  <strong style={{ color: 'var(--navy-900)' }}>{complaint.name}</strong>
                </div>
                <div className="meta-tile highlight">
                  <span className="meta-label" style={{ color: 'var(--primary-hover)' }}>Semestre</span>
                  <strong style={{ color: 'var(--navy-900)' }}>{complaint.semester}</strong>
                </div>
              </>
            )}
          </div>

          <div className="meta-tile" style={{ marginBottom: '1rem' }}>
            <span className="meta-label">Asunto</span>
            <strong style={{ fontSize: '1rem', color: 'var(--navy-900)' }}>{complaint.subject}</strong>
          </div>

          <div style={{ marginBottom: '1.15rem' }}>
            <h4 style={{ margin: '0 0 0.45rem', color: 'var(--navy-900)', fontSize: '0.88rem' }}>
              Descripción Detallada
            </h4>
            <p
              style={{
                margin: 0,
                padding: '1rem',
                background: 'var(--bg-subtle)',
                borderRadius: 'var(--radius-sm)',
                border: '1px solid var(--border-subtle)',
                whiteSpace: 'pre-line',
                lineHeight: 1.6,
                fontSize: '0.9rem'
              }}
            >
              {complaint.description}
            </p>
          </div>

          <div>
            <h4 style={{ margin: '0 0 0.5rem', color: 'var(--navy-900)', fontSize: '0.88rem' }}>
              Evidencias Adjuntas ({complaint.evidenceLinks?.length || 0})
            </h4>
            {complaint.evidenceLinks && complaint.evidenceLinks.length > 0 ? (
              <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                {complaint.evidenceLinks.map((link, idx) => (
                  <li key={link}>
                    <a
                      href={link}
                      target="_blank"
                      rel="noreferrer"
                      className="btn btn-tonal btn-sm"
                    >
                      <Icon name="image" size={16} />
                      <span>Ver Evidencia {idx + 1}</span>
                    </a>
                  </li>
                ))}
              </ul>
            ) : (
              <p style={{ margin: 0, fontSize: '0.82rem', color: 'var(--slate-500)' }}>
                No se adjuntaron evidencias fotográficas en este radicado.
              </p>
            )}
          </div>
        </div>

        {/* Columna derecha: gestión de estado + historial */}
        <div>
          <StatusControls
            complaintId={complaint.id}
            currentStatus={complaint.status}
            onUpdated={(nextStatus, updatedHistory) => onStatusUpdated?.(complaint.id, nextStatus, updatedHistory)}
          />
          <StatusHistory history={history} />
        </div>
      </div>
    </section>
  );
}
