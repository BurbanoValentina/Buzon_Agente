import { useEffect, useState } from 'react';
import { Icon } from '../../components/Icon';
import { getComplaintStatus } from '../../services/complaintsApi';

const STATUS_CONFIG = {
  PENDIENTE: {
    label: 'Pendiente de gestión',
    icon: 'hourglass_top',
    color: '#b45309',
    bgColor: '#fef3c7',
    borderColor: '#fde68a',
    desc: 'Tu queja ha sido radicada correctamente y está en espera de revisión por el representante estudiantil.'
  },
  COMPLETADA: {
    label: 'Completada / Resuelta',
    icon: 'check_circle',
    color: '#047857',
    bgColor: '#d1fae5',
    borderColor: '#a7f3d0',
    desc: 'La situación reportada ha sido atendida, gestionada y resuelta formalmente.'
  },
  NO_COMPLETADA: {
    label: 'No completada / Cerrada',
    icon: 'cancel',
    color: '#be123c',
    bgColor: '#ffe4e6',
    borderColor: '#fecdd3',
    desc: 'El caso fue revisado pero no procedió o no pudo completarse en las condiciones actuales.'
  }
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

export function PublicComplaintTracker({ initialRadicado = '', onNewComplaint }) {
  const [radicado, setRadicado] = useState(initialRadicado || '');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [complaint, setComplaint] = useState(null);
  const [copied, setCopied] = useState(false);

  async function handleSearch(searchId) {
    const idToSearch = (searchId !== undefined ? searchId : radicado).trim();
    if (!idToSearch) {
      setError('Por favor ingresa un código de radicado.');
      return;
    }
    setLoading(true);
    setError('');
    setComplaint(null);
    try {
      const data = await getComplaintStatus(idToSearch);
      setComplaint(data);
    } catch (err) {
      setError(
        err.message ||
          'No se encontró ninguna queja con el radicado proporcionado. Verifica que el código esté escrito correctamente.'
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (initialRadicado) {
      setRadicado(initialRadicado);
      handleSearch(initialRadicado);
    }
  }, [initialRadicado]);

  async function copyRadicado() {
    if (complaint?.publicId) {
      try {
        await window.navigator?.clipboard?.writeText(complaint.publicId);
        setCopied(true);
        window.setTimeout(() => setCopied(false), 2000);
      } catch {
        // Clipboard API unavailable in restricted contexts
      }
    }
  }

  const currentStatus = complaint?.status
    ? STATUS_CONFIG[complaint.status] || {
        label: complaint.status,
        icon: 'info',
        color: '#475569',
        bgColor: '#f1f5f9',
        borderColor: '#e2e8f0',
        desc: ''
      }
    : null;

  return (
    <div className="animate-in">
      {/* Buscador de radicado */}
      <section className="content-card" style={{ marginBottom: '1.5rem' }}>
        <div className="card-header-styled">
          <h3 className="card-title-styled">
            <span className="card-title-icon">
              <Icon name="track_changes" size={21} />
            </span>
            <span>Seguimiento de Radicado</span>
          </h3>
          <span className="char-counter-pill">Consulta en Tiempo Real</span>
        </div>

        <p style={{ fontSize: '0.88rem', color: 'var(--slate-600)', margin: '0 0 1.25rem', lineHeight: 1.5 }}>
          Ingresa el código único de radicado recibido al registrar tu queja académica para verificar su estado,
          avances y resoluciones oficiales.
        </p>

        <form
          className="search-tracker-form"
          onSubmit={(e) => {
            e.preventDefault();
            handleSearch();
          }}
          style={{
            display: 'flex',
            flexDirection: 'row',
            gap: '0.65rem',
            alignItems: 'center',
            flexWrap: 'wrap',
            marginTop: '0.75rem',
            marginBottom: 0
          }}
        >
          <div className="input-with-icon" style={{ flex: '1 1 300px', minWidth: '240px', maxWidth: '100%', boxSizing: 'border-box' }}>
            <Icon name="tag" size={19} className="input-leading-icon" style={{ color: 'var(--primary)' }} />
            <input
              type="text"
              placeholder="Ej. 484e798a-14c5-4336-bded-7c1ed57efa95"
              value={radicado}
              onChange={(e) => setRadicado(e.target.value)}
              className="font-mono"
              style={{ fontSize: '0.92rem', paddingLeft: '2.5rem', height: '44px', boxSizing: 'border-box' }}
              aria-label="Código de radicado"
            />
          </div>

          <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', flexWrap: 'wrap' }}>
            <button
              type="submit"
              disabled={loading}
              className="btn-primary"
              style={{ padding: '0 1.35rem', height: '44px', display: 'inline-flex', alignItems: 'center', gap: '0.5rem', whiteSpace: 'nowrap' }}
            >
              {loading ? (
                <>
                  <span className="spinner" aria-hidden="true" />
                  <span>Consultando...</span>
                </>
              ) : (
                <>
                  <Icon name="search" size={18} />
                  <span>Consultar Estado</span>
                </>
              )}
            </button>

            {radicado && (
              <button
                type="button"
                className="btn-secondary"
                style={{ height: '44px', padding: '0 0.9rem', display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}
                onClick={() => {
                  setRadicado('');
                  setComplaint(null);
                  setError('');
                }}
                title="Limpiar búsqueda"
              >
                <Icon name="close" size={17} />
                <span>Limpiar</span>
              </button>
            )}
          </div>
        </form>

        {error && (
          <div
            role="alert"
            className="animate-in"
            style={{
              marginTop: '1rem',
              background: '#fff1f2',
              border: '1px solid #fecdd3',
              color: '#be123c',
              padding: '0.85rem 1rem',
              borderRadius: 'var(--radius-md)',
              display: 'flex',
              alignItems: 'center',
              gap: '0.6rem',
              fontSize: '0.88rem'
            }}
          >
            <Icon name="error" size={19} />
            <span>{error}</span>
          </div>
        )}
      </section>

      {/* Resultados de la consulta */}
      {complaint && (
        <div className="animate-in" style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          {/* Tarjeta de Resumen y Estado Actual */}
          <section className="content-card">
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'flex-start',
                flexWrap: 'wrap',
                gap: '1rem',
                borderBottom: '1px solid var(--border-subtle)',
                paddingBottom: '1.25rem',
                marginBottom: '1.25rem'
              }}
            >
              <div>
                <span
                  style={{
                    fontSize: '0.74rem',
                    textTransform: 'uppercase',
                    letterSpacing: '0.05em',
                    fontWeight: 700,
                    color: 'var(--slate-500)',
                    display: 'block',
                    marginBottom: '0.25rem'
                  }}
                >
                  Radicado de seguimiento
                </span>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', flexWrap: 'wrap', minWidth: 0, maxWidth: '100%' }}>
                  <span className="font-mono" style={{ fontSize: '1.05rem', fontWeight: 700, color: 'var(--navy-900)', overflowWrap: 'anywhere', wordBreak: 'break-word' }}>
                    {complaint.publicId}
                  </span>
                  <button
                    type="button"
                    className="btn-secondary btn-sm"
                    onClick={copyRadicado}
                    title="Copiar radicado"
                    style={{ padding: '0.25rem 0.6rem' }}
                  >
                    <Icon name={copied ? 'check' : 'content_copy'} size={15} />
                    <span>{copied ? 'Copiado' : 'Copiar'}</span>
                  </button>
                </div>
              </div>

              {/* Badge del estado actual */}
              <div
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  padding: '0.55rem 1rem',
                  borderRadius: 'var(--radius-full)',
                  background: currentStatus.bgColor,
                  color: currentStatus.color,
                  fontWeight: 700,
                  fontSize: '0.9rem',
                  border: `1px solid ${currentStatus.borderColor}`
                }}
              >
                <Icon name={currentStatus.icon} filled size={19} />
                <span>{currentStatus.label}</span>
              </div>
            </div>

            {/* Metadatos en Grid */}
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
                gap: '1rem',
                background: 'var(--bg-subtle)',
                padding: '1.15rem',
                borderRadius: 'var(--radius-md)',
                border: '1px solid var(--border-subtle)'
              }}
            >
              <div>
                <span style={{ fontSize: '0.75rem', color: 'var(--slate-500)', display: 'block' }}>Categoría</span>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.45rem', marginTop: '0.2rem', fontWeight: 600, color: 'var(--navy-900)' }}>
                  <Icon
                    name={CATEGORY_ICONS[complaint.category] || 'folder'}
                    size={17}
                    style={{ color: 'var(--primary)' }}
                  />
                  <span>{complaint.category}</span>
                </div>
              </div>

              <div>
                <span style={{ fontSize: '0.75rem', color: 'var(--slate-500)', display: 'block' }}>Fecha de Radicación</span>
                <span style={{ fontSize: '0.88rem', fontWeight: 600, color: 'var(--navy-900)', display: 'block', marginTop: '0.2rem' }}>
                  {new Date(complaint.registeredAt).toLocaleString()}
                </span>
              </div>

              {complaint.completedAt && (
                <div>
                  <span style={{ fontSize: '0.75rem', color: 'var(--slate-500)', display: 'block' }}>Fecha de Resolución</span>
                  <span style={{ fontSize: '0.88rem', fontWeight: 600, color: '#047857', display: 'block', marginTop: '0.2rem' }}>
                    {new Date(complaint.completedAt).toLocaleString()}
                  </span>
                </div>
              )}
            </div>

            {/* Asunto */}
            <div style={{ marginTop: '1.25rem', minWidth: 0, maxWidth: '100%' }}>
              <span style={{ fontSize: '0.78rem', fontWeight: 700, color: 'var(--slate-600)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                Asunto reportado
              </span>
              <p style={{ margin: '0.35rem 0 0', fontSize: '0.98rem', color: 'var(--navy-900)', fontWeight: 600, lineHeight: 1.45, overflowWrap: 'anywhere', wordBreak: 'break-word' }}>
                {complaint.subject}
              </p>
            </div>
          </section>

          {/* Línea de tiempo oficial de atención */}
          <section className="content-card">
            <div className="card-header-styled">
              <h3 className="card-title-styled">
                <span className="card-title-icon">
                  <Icon name="timeline" size={21} />
                </span>
                <span>Línea de Tiempo y Resoluciones Oficiales</span>
              </h3>
              <span className="char-counter-pill">
                {(complaint.history?.length || 0) + 1} hitos
              </span>
            </div>

            <ul className="timeline-list">
              {/* Hito 1: Radicación inicial */}
              <li className="timeline-item">
                <span className="timeline-dot" style={{ background: 'var(--primary)' }} />
                <div style={{ background: '#ffffff', padding: '0.9rem 1.15rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.5rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <Icon name="check_circle" size={17} style={{ color: 'var(--primary)' }} />
                      <strong style={{ fontSize: '0.9rem', color: 'var(--navy-900)' }}>Queja Académica Radicada</strong>
                    </div>
                    <span style={{ fontSize: '0.76rem', color: 'var(--slate-500)' }}>
                      {new Date(complaint.registeredAt).toLocaleString()}
                    </span>
                  </div>
                  <p style={{ margin: '0.35rem 0 0', fontSize: '0.84rem', color: 'var(--slate-600)' }}>
                    El caso fue recibido formalmente con respaldo criptográfico y confidencialidad protegida.
                  </p>
                </div>
              </li>

              {/* Hitos de gestión oficial */}
              {complaint.history && complaint.history.length > 0 ? (
                complaint.history.map((item, idx) => {
                  const isCompletion = item.newStatus === 'COMPLETADA';
                  const isReopen = item.newStatus === 'PENDIENTE';

                  const itemColor = isCompletion ? '#047857' : isReopen ? '#b45309' : '#475569';
                  const itemTitle = isCompletion
                    ? 'Resolución Oficial / Caso Completado'
                    : isReopen
                    ? 'Reapertura para Seguimiento Adicional'
                    : 'Cierre No Completado';

                  return (
                    <li key={idx} className="timeline-item">
                      <span className="timeline-dot" style={{ background: itemColor }} />
                      <div
                        style={{
                          background: isCompletion ? '#f0fdf4' : isReopen ? '#fffbeb' : '#f8fafc',
                          padding: '0.95rem 1.15rem',
                          borderRadius: 'var(--radius-md)',
                          border: `1px solid ${isCompletion ? '#bbf7d0' : isReopen ? '#fde68a' : 'var(--border-subtle)'}`
                        }}
                      >
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.5rem' }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                            <Icon
                              name={isCompletion ? 'task_alt' : isReopen ? 'replay' : 'info'}
                              size={17}
                              style={{ color: itemColor }}
                            />
                            <strong style={{ fontSize: '0.9rem', color: 'var(--navy-900)' }}>{itemTitle}</strong>
                          </div>
                          <span style={{ fontSize: '0.76rem', color: 'var(--slate-500)' }}>
                            {new Date(item.changedAt).toLocaleString()}
                          </span>
                        </div>

                        {/* Comentario oficial dejado por la administración */}
                        <div
                          style={{
                            marginTop: '0.65rem',
                            padding: '0.75rem 0.95rem',
                            background: '#ffffff',
                            borderRadius: 'var(--radius-sm)',
                            border: '1px solid var(--border-subtle)'
                          }}
                        >
                          <span
                            style={{
                              fontSize: '0.72rem',
                              fontWeight: 700,
                              textTransform: 'uppercase',
                              color: 'var(--slate-500)',
                              display: 'block',
                              marginBottom: '0.2rem'
                            }}
                          >
                            Respuesta u Observación Oficial:
                          </span>
                          <p style={{ margin: 0, fontSize: '0.86rem', color: 'var(--navy-900)', lineHeight: 1.5, whiteSpace: 'pre-wrap', overflowWrap: 'anywhere', wordBreak: 'break-word', maxWidth: '100%' }}>
                            {item.comment}
                          </p>
                        </div>
                      </div>
                    </li>
                  );
                })
              ) : (
                <li className="timeline-item">
                  <span className="timeline-dot" style={{ background: '#f59e0b' }} />
                  <div
                    style={{
                      background: '#fffbeb',
                      padding: '0.95rem 1.15rem',
                      borderRadius: 'var(--radius-md)',
                      border: '1px dashed #fcd34d',
                      color: '#92400e'
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <Icon name="pending_actions" size={18} />
                      <strong style={{ fontSize: '0.88rem' }}>En revisión inicial</strong>
                    </div>
                    <p style={{ margin: '0.35rem 0 0', fontSize: '0.82rem', lineHeight: 1.45 }}>
                      Tu reporte se encuentra en cola de espera para revisión y trámite por parte de la Representación
                      Estudiantil. Las observaciones y resoluciones aparecerán aquí en cuanto sean emitidas.
                    </p>
                  </div>
                </li>
              )}
            </ul>
          </section>
        </div>
      )}
    </div>
  );
}
