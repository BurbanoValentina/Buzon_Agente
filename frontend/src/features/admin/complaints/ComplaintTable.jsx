import { Icon } from '../../../components/Icon';

function getStatusMeta(status) {
  if (status === 'COMPLETADA') {
    return { cls: 'completada', icon: 'check_circle', label: 'Completada' };
  }
  if (status === 'NO_COMPLETADA') {
    return { cls: 'no-completada', icon: 'cancel', label: 'No completada' };
  }
  return { cls: 'pendiente', icon: 'schedule', label: 'Pendiente' };
}

export function ComplaintTable({ complaints, selectedId, onSelect }) {
  return (
    <div className="table-container">
      <table>
        <caption>Quejas registradas en el sistema ({complaints.length})</caption>
        <thead>
          <tr>
            <th>Radicado</th>
            <th>Fecha</th>
            <th>Categoría</th>
            <th>Modalidad</th>
            <th>Estado</th>
            <th style={{ textAlign: 'right' }}>Expediente</th>
          </tr>
        </thead>
        <tbody>
          {complaints.map((complaint) => {
            const statusMeta = getStatusMeta(complaint.status);
            const isSelected = selectedId === complaint.id;
            return (
              <tr
                key={complaint.id}
                className={isSelected ? 'selected-row' : ''}
                style={{ cursor: 'pointer' }}
                onClick={() => onSelect(complaint.id)}
              >
                <td>
                  <span
                    className="font-mono"
                    title={complaint.id}
                    style={{
                      fontSize: '0.8rem',
                      fontWeight: 600,
                      background: 'var(--bg-muted)',
                      padding: '0.2rem 0.5rem',
                      borderRadius: 'var(--radius-xs)'
                    }}
                  >
                    {complaint.id.length > 12 ? `${complaint.id.slice(0, 8)}...` : complaint.id}
                  </span>
                </td>
                <td style={{ color: 'var(--slate-600)', whiteSpace: 'nowrap' }}>
                  {complaint.registeredAt ? new Date(complaint.registeredAt).toLocaleDateString() : 'N/A'}
                </td>
                <td>
                  <div style={{ fontWeight: 700, color: 'var(--navy-900)' }}>{complaint.category}</div>
                  {complaint.locationNumber && (
                    <span style={{ fontSize: '0.75rem', color: 'var(--slate-500)' }}>
                      Ubicación: {complaint.locationNumber}
                    </span>
                  )}
                </td>
                <td>
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem', fontSize: '0.8rem', color: 'var(--slate-600)' }}>
                    <Icon name={complaint.anonymous ? 'visibility_off' : 'person'} size={16} />
                    <span>{complaint.anonymous ? 'Anónima' : 'Identificada'}</span>
                  </span>
                </td>
                <td>
                  <span className={`status-pill ${statusMeta.cls}`}>
                    <Icon name={statusMeta.icon} filled size={15} />
                    <span>{statusMeta.label}</span>
                  </span>
                </td>
                <td style={{ textAlign: 'right' }}>
                  <button
                    type="button"
                    className={`${isSelected ? 'btn-primary' : 'btn-tonal'} btn-sm`}
                    onClick={(event) => {
                      event.stopPropagation();
                      onSelect(complaint.id);
                    }}
                  >
                    <Icon name="open_in_new" size={15} />
                    <span>{isSelected ? 'Abierto' : 'Ver detalle'}</span>
                  </button>
                </td>
              </tr>
            );
          })}
          {!complaints.length && (
            <tr>
              <td colSpan="6" style={{ textAlign: 'center', padding: '2.5rem 1rem', color: 'var(--slate-500)' }}>
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.5rem' }}>
                  <Icon name="inbox" size={34} style={{ color: 'var(--slate-400)' }} />
                  <span>No hay quejas registradas con los filtros seleccionados.</span>
                </div>
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
