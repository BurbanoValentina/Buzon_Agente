import { Icon } from '../../../components/Icon';

function statusBadgeLabel(status) {
  if (status === 'COMPLETADA') return 'Completada';
  if (status === 'NO_COMPLETADA') return 'No completada';
  return 'Pendiente';
}

export function StatusHistory({ history = [] }) {
  return (
    <section
      aria-label="Historial de estados"
      style={{
        background: '#ffffff',
        padding: '1.15rem',
        borderRadius: 'var(--radius-md)',
        border: '1px solid var(--border-subtle)',
        marginTop: '1rem'
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.45rem', marginBottom: '0.5rem' }}>
        <Icon name="history" size={19} style={{ color: 'var(--primary)' }} />
        <h2 style={{ margin: 0, fontSize: '0.96rem', color: 'var(--navy-900)' }}>Historial de Trazabilidad</h2>
      </div>

      {history.length === 0 ? (
        <p style={{ margin: '0.5rem 0 0', fontSize: '0.82rem', color: 'var(--slate-500)' }}>
          Sin cambios de estado registrados todavía. Esta queja permanece en estado inicial Pendiente.
        </p>
      ) : (
        <ol className="timeline-list">
          {history.map((item, index) => (
            <li key={`${item.changedAt}-${index}`} className="timeline-item">
              <span className="timeline-dot" aria-hidden="true" />
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.4rem' }}>
                <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem', fontSize: '0.8rem', fontWeight: 700, color: 'var(--navy-900)' }}>
                  {item.previousStatus && (
                    <>
                      <span style={{ color: 'var(--slate-500)' }}>{statusBadgeLabel(item.previousStatus)}</span>
                      <Icon name="arrow_forward" size={14} style={{ color: 'var(--slate-400)' }} />
                    </>
                  )}
                  <span style={{ color: 'var(--primary-hover)' }}>{statusBadgeLabel(item.newStatus)}</span>
                </div>
                <span style={{ fontSize: '0.72rem', color: 'var(--slate-500)' }}>
                  {item.changedAt ? new Date(item.changedAt).toLocaleString() : ''}
                </span>
              </div>
              <p style={{ margin: '0.28rem 0 0', fontSize: '0.82rem', color: 'var(--slate-700)', lineHeight: 1.45 }}>
                {item.comment}
              </p>
              {item.changedBy && (
                <span style={{ fontSize: '0.72rem', color: 'var(--slate-400)' }}>
                  Gestionado por: {item.changedBy}
                </span>
              )}
            </li>
          ))}
        </ol>
      )}
    </section>
  );
}
