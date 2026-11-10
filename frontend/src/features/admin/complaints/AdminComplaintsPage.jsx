import { useEffect, useState } from 'react';
import { Icon } from '../../../components/Icon';
import { downloadComplaints, getComplaint, listComplaints } from '../../../services/adminComplaintsApi';
import { getStatusHistory } from '../../../services/statusApi';
import { ComplaintDetail } from './ComplaintDetail';
import { ComplaintFilters } from './ComplaintFilters';
import { ComplaintTable } from './ComplaintTable';

export function AdminComplaintsPage() {
  const [filters, setFilters] = useState({});
  const [complaints, setComplaints] = useState([]);
  const [detail, setDetail] = useState(null);
  const [history, setHistory] = useState([]);
  const [exporting, setExporting] = useState(false);
  const [state, setState] = useState({ loading: false, error: '' });

  async function load(event, overrideFilters) {
    event?.preventDefault();
    const activeFilters = overrideFilters !== undefined ? overrideFilters : filters;
    setState({ loading: true, error: '' });
    try {
      const data = await listComplaints(activeFilters);
      setComplaints(data);
      setState({ loading: false, error: '' });
    } catch (error) {
      setState({ loading: false, error: error.message });
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function select(id) {
    try {
      const [detailData, historyData] = await Promise.all([
        getComplaint(id),
        getStatusHistory(id).catch(() => [])
      ]);
      setDetail(detailData);
      setHistory(historyData);
    } catch (error) {
      setState({ loading: false, error: error.message });
    }
  }

  async function handleStatusUpdated(id, _nextStatus, updatedHistory) {
    setHistory(updatedHistory);
    await Promise.all([
      load(),
      getComplaint(id).then(setDetail).catch(() => {})
    ]);
  }

  async function download() {
    setExporting(true);
    try {
      const blob = await downloadComplaints(filters);
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = 'quejas.xlsx';
      anchor.click();
      URL.revokeObjectURL(url);
    } catch (error) {
      setState({ loading: false, error: error.message });
    } finally {
      setExporting(false);
    }
  }

  function resetFilters() {
    setFilters({});
    load(undefined, {});
  }

  const total = complaints.length;
  const pending = complaints.filter((c) => c.status === 'PENDIENTE').length;
  const completed = complaints.filter((c) => c.status === 'COMPLETADA').length;
  const resolutionRate = total > 0 ? Math.round((completed / total) * 100) : 0;

  return (
    <section aria-label="Gestión de quejas">
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1rem',
          marginBottom: '1.5rem'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
          <span className="card-title-icon">
            <Icon name="inbox" size={20} />
          </span>
          <div>
            <h1 style={{ margin: 0, fontSize: '1.35rem', color: 'var(--navy-900)' }}>
              Listado y Gestión de Quejas
            </h1>
            <p style={{ margin: '0.15rem 0 0', fontSize: '0.84rem', color: 'var(--slate-500)' }}>
              Consulta, filtra, actualiza estados con trazabilidad y exporta a Excel.
            </p>
          </div>
        </div>

        <button
          type="button"
          className="btn-success"
          disabled={exporting}
          onClick={download}
        >
          {exporting ? (
            <>
              <span className="spinner" aria-hidden="true" />
              <span>Generando Excel...</span>
            </>
          ) : (
            <>
              <Icon name="table_view" size={18} />
              <span>Descargar Excel (.xlsx)</span>
            </>
          )}
        </button>
      </div>

      {/* KPI Cards */}
      <div className="kpi-grid">
        <div className="kpi-card">
          <div>
            <p className="kpi-label">Total Quejas</p>
            <p className="kpi-value">{total}</p>
            <p className="kpi-sub">Registros filtrados</p>
          </div>
          <span className="kpi-icon-box" style={{ background: 'var(--primary-soft)', color: 'var(--primary-hover)' }}>
            <Icon name="folder_open" size={22} />
          </span>
        </div>

        <div className="kpi-card">
          <div>
            <p className="kpi-label">Pendientes</p>
            <p className="kpi-value" style={{ color: 'var(--warning)' }}>{pending}</p>
            <p className="kpi-sub">En espera de gestión</p>
          </div>
          <span className="kpi-icon-box" style={{ background: 'var(--warning-bg)', color: 'var(--warning)' }}>
            <Icon name="pending_actions" size={22} />
          </span>
        </div>

        <div className="kpi-card">
          <div>
            <p className="kpi-label">Completadas</p>
            <p className="kpi-value" style={{ color: 'var(--success)' }}>{completed}</p>
            <p className="kpi-sub">Atendidas con respaldo</p>
          </div>
          <span className="kpi-icon-box" style={{ background: 'var(--success-bg)', color: 'var(--success)' }}>
            <Icon name="task_alt" size={22} />
          </span>
        </div>

        <div className="kpi-card">
          <div>
            <p className="kpi-label">Tasa de Cierre</p>
            <p className="kpi-value" style={{ color: 'var(--ai-indigo)' }}>{resolutionRate}%</p>
            <p className="kpi-sub">Cumplimiento global</p>
          </div>
          <span className="kpi-icon-box" style={{ background: 'var(--ai-indigo-soft)', color: 'var(--ai-indigo)' }}>
            <Icon name="donut_large" size={22} />
          </span>
        </div>
      </div>

      <ComplaintFilters
        filters={filters}
        onChange={setFilters}
        onSubmit={load}
        onReset={resetFilters}
      />

      {state.loading && (
        <p role="status">
          <span className="spinner dark" aria-hidden="true" />
          <span>Cargando listado de quejas...</span>
        </p>
      )}
      {state.error && (
        <p role="alert">
          <Icon name="error" size={18} />
          <span>{state.error}</span>
        </p>
      )}

      <ComplaintTable complaints={complaints} selectedId={detail?.id} onSelect={select} />

      <ComplaintDetail
        complaint={detail}
        history={history}
        onStatusUpdated={handleStatusUpdated}
        onClose={() => setDetail(null)}
      />
    </section>
  );
}
