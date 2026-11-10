import { useEffect, useState } from 'react';
import { Icon } from '../../../components/Icon';
import { getMonthlyFollowUp } from '../../../services/statusApi';

function eventMeta(type) {
  if (type === 'FIRST_COMPLETION' || type === 'COMPLETADA') {
    return { icon: 'check_circle', bg: 'var(--success-bg)', color: 'var(--success)', label: 'Completada' };
  }
  if (type === 'RE_COMPLETION') {
    return { icon: 'published_with_changes', bg: 'var(--primary-soft)', color: 'var(--primary-hover)', label: 'Re-completada' };
  }
  if (type === 'REOPENING' || type === 'REAPERTURA') {
    return { icon: 'replay', bg: 'var(--warning-bg)', color: 'var(--warning)', label: 'Reapertura' };
  }
  return { icon: 'event_note', bg: 'var(--bg-muted)', color: 'var(--slate-600)', label: type };
}

export function MonthlyFollowUp() {
  const [events, setEvents] = useState([]);
  const [range, setRange] = useState({ from: '', to: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function load(overrideRange) {
    const active = overrideRange !== undefined ? overrideRange : range;
    const params = {};
    if (active.from) params.from = active.from;
    if (active.to) params.to = active.to;
    setLoading(true);
    setError('');
    try {
      setEvents(await getMonthlyFollowUp(params));
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load({});
  }, []);

  return (
    <section aria-label="Seguimiento mensual">
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1rem',
          marginBottom: '1.35rem'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
          <span className="card-title-icon">
            <Icon name="calendar_month" size={20} />
          </span>
          <div>
            <h2 style={{ margin: 0, fontSize: '1.35rem', color: 'var(--navy-900)' }}>
              Seguimiento Mensual Inmutable
            </h2>
            <p style={{ margin: '0.15rem 0 0', fontSize: '0.84rem', color: 'var(--slate-500)' }}>
              Registro histórico por mes de resoluciones iniciales, reaperturas y cierres definitivos.
            </p>
          </div>
        </div>

        <span className="char-counter-pill">{events.length} eventos</span>
      </div>

      {/* Filtro por rango de fechas */}
      <form
        onSubmit={(event) => {
          event.preventDefault();
          load();
        }}
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(170px, 1fr))',
          gap: '0.85rem',
          alignItems: 'end',
          background: 'var(--bg-subtle)',
          padding: '1rem 1.15rem',
          borderRadius: 'var(--radius-md)',
          border: '1px solid var(--border-subtle)',
          marginBottom: '1.25rem'
        }}
      >
        <div>
          <label htmlFor="followup-from" style={{ fontSize: '0.78rem' }}>Desde</label>
          <input
            id="followup-from"
            type="date"
            value={range.from}
            onChange={(event) => setRange({ ...range, from: event.target.value })}
          />
        </div>
        <div>
          <label htmlFor="followup-to" style={{ fontSize: '0.78rem' }}>Hasta</label>
          <input
            id="followup-to"
            type="date"
            value={range.to}
            onChange={(event) => setRange({ ...range, to: event.target.value })}
          />
        </div>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button type="submit" className="btn-primary btn-sm" style={{ flex: 1, height: '42px' }}>
            <Icon name="search" size={17} />
            <span>Consultar</span>
          </button>
          <button
            type="button"
            className="btn-secondary btn-sm"
            style={{ height: '42px' }}
            title="Ver historial completo"
            onClick={() => {
              const empty = { from: '', to: '' };
              setRange(empty);
              load(empty);
            }}
          >
            <Icon name="restart_alt" size={18} />
          </button>
        </div>
      </form>

      {error && (
        <p role="alert">
          <Icon name="error" size={18} />
          <span>{error}</span>
        </p>
      )}

      {loading && (
        <p role="status">
          <span className="spinner dark" aria-hidden="true" />
          <span>Consultando historial mensual...</span>
        </p>
      )}

      {!loading && events.length === 0 ? (
        <div
          style={{
            padding: '2.5rem 1.25rem',
            textAlign: 'center',
            background: 'var(--bg-subtle)',
            borderRadius: 'var(--radius-md)',
            border: '1px dashed var(--border-strong)',
            color: 'var(--slate-500)',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '0.5rem'
          }}
        >
          <Icon name="event_available" size={34} style={{ color: 'var(--slate-400)' }} />
          <span>No hay eventos mensuales registrados en el rango seleccionado.</span>
        </div>
      ) : (
        <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          {events.map((event) => {
            const meta = eventMeta(event.eventType);
            return (
              <li
                key={`${event.complaintId}-${event.eventType}-${event.occurredAt}`}
                style={{
                  background: '#ffffff',
                  border: '1px solid var(--border-subtle)',
                  borderRadius: 'var(--radius-md)',
                  padding: '0.95rem 1.2rem',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  flexWrap: 'wrap',
                  gap: '0.75rem',
                  boxShadow: 'var(--shadow-xs)'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
                  <span
                    style={{
                      width: '38px',
                      height: '38px',
                      borderRadius: 'var(--radius-sm)',
                      background: meta.bg,
                      color: meta.color,
                      display: 'inline-flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      flexShrink: 0
                    }}
                  >
                    <Icon name={meta.icon} filled size={20} />
                  </span>
                  <div>
                    <div style={{ fontWeight: 700, color: 'var(--navy-900)', fontSize: '0.92rem' }}>
                      {meta.label}{' '}
                      <span className="font-mono" style={{ fontSize: '0.82rem', color: 'var(--slate-600)', fontWeight: 500 }}>
                        · {event.complaintId}
                      </span>
                    </div>
                    <div style={{ fontSize: '0.78rem', color: 'var(--slate-500)', marginTop: '0.12rem' }}>
                      Registrado: {new Date(event.occurredAt).toLocaleString()}
                    </div>
                  </div>
                </div>
                <span className="char-counter-pill" style={{ background: 'var(--primary-soft)', color: 'var(--primary-deep)', borderColor: '#bae6fd' }}>
                  Mes: {event.eventMonth}
                </span>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}
