import { Icon } from '../../../components/Icon';
import { CATEGORIES } from '../../complaints/complaintValidation';

export function ComplaintFilters({ filters, onChange, onSubmit, onReset }) {
  return (
    <form
      onSubmit={onSubmit}
      aria-label="Filtros de quejas"
      style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(175px, 1fr))',
        gap: '0.85rem',
        alignItems: 'end',
        background: 'var(--bg-subtle)',
        padding: '1.1rem 1.25rem',
        borderRadius: 'var(--radius-md)',
        border: '1px solid var(--border-subtle)',
        marginBottom: '1rem'
      }}
    >
      <div>
        <label htmlFor="filter-category" style={{ fontSize: '0.78rem', marginBottom: '0.25rem' }}>
          Categoría
        </label>
        <select
          id="filter-category"
          value={filters.category || ''}
          onChange={(event) => onChange({ ...filters, category: event.target.value })}
        >
          <option value="">Todas las categorías</option>
          {CATEGORIES.map((category) => (
            <option key={category} value={category}>
              {category}
            </option>
          ))}
        </select>
      </div>

      <div>
        <label htmlFor="filter-status" style={{ fontSize: '0.78rem', marginBottom: '0.25rem' }}>
          Estado
        </label>
        <select
          id="filter-status"
          value={filters.status || ''}
          onChange={(event) => onChange({ ...filters, status: event.target.value })}
        >
          <option value="">Todos los estados</option>
          <option value="PENDIENTE">Pendiente</option>
          <option value="COMPLETADA">Completada</option>
          <option value="NO_COMPLETADA">No completada</option>
        </select>
      </div>

      <div>
        <label htmlFor="filter-anonymous" style={{ fontSize: '0.78rem', marginBottom: '0.25rem' }}>
          Modalidad
        </label>
        <select
          id="filter-anonymous"
          value={filters.anonymous ?? ''}
          onChange={(event) => onChange({ ...filters, anonymous: event.target.value })}
        >
          <option value="">Todas</option>
          <option value="true">Anónimas</option>
          <option value="false">Identificadas</option>
        </select>
      </div>

      <div style={{ display: 'flex', gap: '0.5rem' }}>
        <button type="submit" className="btn-primary btn-sm" style={{ flex: 1, height: '42px' }}>
          <Icon name="filter_list" size={17} />
          <span>Filtrar</span>
        </button>
        <button
          type="button"
          className="btn-secondary btn-sm"
          style={{ height: '42px' }}
          title="Limpiar filtros"
          onClick={onReset}
        >
          <Icon name="restart_alt" size={18} />
        </button>
      </div>
    </form>
  );
}
