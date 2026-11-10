import { useEffect, useState } from 'react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis
} from 'recharts';
import { Icon } from '../../../components/Icon';
import { getDashboard } from '../../../services/dashboardApi';

function ChartTooltip({ active, payload, label }) {
  if (!active || !payload?.length) return null;
  return (
    <div
      style={{
        background: '#0f172a',
        color: '#ffffff',
        padding: '0.6rem 0.85rem',
        borderRadius: '10px',
        boxShadow: '0 10px 24px rgba(15, 23, 42, 0.25)',
        fontSize: '0.8rem'
      }}
    >
      <div style={{ fontWeight: 700, marginBottom: '0.2rem', color: '#bae6fd' }}>{label}</div>
      <div>
        {payload[0].name}: <strong className="font-mono">{payload[0].value}</strong>
      </div>
    </div>
  );
}

export function AdminDashboard() {
  const [period, setPeriod] = useState('MONTHLY');
  const [dashboard, setDashboard] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    getDashboard({ period })
      .then(setDashboard)
      .catch((requestError) => setError(requestError.message));
  }, [period]);

  if (!dashboard) {
    return (
      <section aria-label="Dashboard" style={{ padding: '2.5rem 1rem', textAlign: 'center' }}>
        <p role="status" style={{ justifyContent: 'center' }}>
          <span className="spinner dark" aria-hidden="true" />
          <span>Cargando métricas reales y estimaciones de IA...</span>
        </p>
        {error && (
          <p role="alert">
            <Icon name="error" size={18} />
            <span>{error}</span>
          </p>
        )}
      </section>
    );
  }

  const realData =
    period === 'WEEKLY'
      ? dashboard.weekly.map((point) => ({ label: point.week || '', count: point.count }))
      : dashboard.monthly.map((point) => ({ label: point.month || '', count: point.count }));

  const categoryData = Object.entries(dashboard.byCategory || {})
    .map(([name, count]) => ({ name, count }))
    .sort((a, b) => b.count - a.count);

  const anonPercent = dashboard.total > 0 ? Math.round((dashboard.anonymous / dashboard.total) * 100) : 0;
  const thresholdProgress = Math.min(100, Math.round((dashboard.total / 5) * 100));
  const leadingCategory = dashboard.leadingCategories?.[0] || 'Sin registros';

  return (
    <section aria-label="Dashboard">
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
            <Icon name="monitoring" size={20} />
          </span>
          <div>
            <h1 style={{ margin: 0, fontSize: '1.35rem', color: 'var(--navy-900)' }}>
              Observatorio Estadístico & IA
            </h1>
            <p style={{ margin: '0.15rem 0 0', fontSize: '0.84rem', color: 'var(--slate-500)' }}>
              Separación clara entre cifras históricas almacenadas y estimaciones predictivas de apoyo.
            </p>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
          <label htmlFor="dashboard-period" style={{ margin: 0, fontSize: '0.8rem', color: 'var(--slate-600)' }}>
            Vista:
          </label>
          <select
            id="dashboard-period"
            value={period}
            onChange={(event) => setPeriod(event.target.value)}
            style={{ width: 'auto', padding: '0.45rem 0.9rem', fontSize: '0.85rem' }}
          >
            <option value="WEEKLY">Semanal</option>
            <option value="MONTHLY">Mensual</option>
          </select>
        </div>
      </div>

      {error && (
        <p role="alert">
          <Icon name="error" size={18} />
          <span>{error}</span>
        </p>
      )}

      {/* KPI Grid */}
      <div className="kpi-grid">
        <div className="kpi-card">
          <div>
            <p className="kpi-label">Total Almacenado</p>
            <p className="kpi-value">{dashboard.total}</p>
            <p className="kpi-sub">Datos reales verificados</p>
          </div>
          <span className="kpi-icon-box" style={{ background: 'var(--primary-soft)', color: 'var(--primary-hover)' }}>
            <Icon name="analytics" size={22} />
          </span>
        </div>

        <div className="kpi-card">
          <div>
            <p className="kpi-label">Anónimas</p>
            <p className="kpi-value" style={{ color: 'var(--primary-hover)' }}>
              {dashboard.anonymous}
            </p>
            <p className="kpi-sub">{anonPercent}% del total</p>
          </div>
          <span className="kpi-icon-box" style={{ background: '#e0f2fe', color: '#0369a1' }}>
            <Icon name="visibility_off" size={22} />
          </span>
        </div>

        <div className="kpi-card">
          <div>
            <p className="kpi-label">Identificadas</p>
            <p className="kpi-value" style={{ color: 'var(--ucc-teal)' }}>
              {dashboard.identified}
            </p>
            <p className="kpi-sub">{dashboard.total > 0 ? 100 - anonPercent : 0}% del total</p>
          </div>
          <span className="kpi-icon-box" style={{ background: '#ccfbf1', color: 'var(--ucc-teal)' }}>
            <Icon name="person" size={22} />
          </span>
        </div>

        <div className="kpi-card">
          <div>
            <p className="kpi-label">Mayor Frecuencia</p>
            <p className="kpi-value" style={{ fontSize: '1.1rem', lineHeight: 1.35, color: 'var(--navy-900)' }}>
              {leadingCategory}
            </p>
            <p className="kpi-sub">Categoría principal</p>
          </div>
          <span className="kpi-icon-box" style={{ background: 'var(--warning-bg)', color: 'var(--warning)' }}>
            <Icon name="trending_up" size={22} />
          </span>
        </div>
      </div>

      {/* Gráficas Recharts */}
      <div className="grid-2" style={{ alignItems: 'stretch' }}>
        <div className="chart-surface">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ margin: 0, fontSize: '1rem', color: 'var(--navy-900)', display: 'flex', alignItems: 'center', gap: '0.45rem' }}>
              <Icon name="bar_chart" size={20} style={{ color: 'var(--primary)' }} />
              <span>Tendencia Temporal ({period === 'WEEKLY' ? 'Semanal' : 'Mensual'})</span>
            </h3>
            <span className="char-counter-pill">Datos Reales</span>
          </div>

          {realData.length === 0 ? (
            <div style={{ height: '260px', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: 'var(--slate-400)', gap: '0.5rem' }}>
              <Icon name="show_chart" size={34} />
              <span style={{ fontSize: '0.85rem' }}>Aún no hay suficientes períodos registrados.</span>
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={realData} margin={{ top: 10, right: 10, left: -20, bottom: 10 }}>
                <defs>
                  <linearGradient id="uccBarGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#0284c7" stopOpacity={1} />
                    <stop offset="100%" stopColor="#38bdf8" stopOpacity={0.75} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                <XAxis dataKey="label" tick={{ fontSize: 12, fill: '#64748b' }} stroke="#cbd5e1" />
                <YAxis allowDecimals={false} tick={{ fontSize: 12, fill: '#64748b' }} stroke="#cbd5e1" />
                <Tooltip content={<ChartTooltip />} />
                <Bar dataKey="count" fill="url(#uccBarGrad)" radius={[8, 8, 0, 0]} name="Quejas registradas" />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        <div className="chart-surface">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ margin: 0, fontSize: '1rem', color: 'var(--navy-900)', display: 'flex', alignItems: 'center', gap: '0.45rem' }}>
              <Icon name="category" size={20} style={{ color: 'var(--ucc-teal)' }} />
              <span>Distribución por Categoría</span>
            </h3>
            <span className="char-counter-pill">{categoryData.length} categorías</span>
          </div>

          {categoryData.length === 0 ? (
            <div style={{ height: '260px', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: 'var(--slate-400)', gap: '0.5rem' }}>
              <Icon name="pie_chart" size={34} />
              <span style={{ fontSize: '0.85rem' }}>Sin quejas registradas por categoría.</span>
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={categoryData} layout="vertical" margin={{ top: 5, right: 20, left: 20, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="#e2e8f0" />
                <XAxis type="number" allowDecimals={false} tick={{ fontSize: 11, fill: '#64748b' }} stroke="#cbd5e1" />
                <YAxis dataKey="name" type="category" width={105} tick={{ fontSize: 11, fill: '#334155', fontWeight: 600 }} stroke="#cbd5e1" />
                <Tooltip content={<ChartTooltip />} />
                <Bar dataKey="count" radius={[0, 6, 6, 0]} name="Quejas">
                  {categoryData.map((entry, idx) => (
                    <Cell key={entry.name} fill={idx === 0 ? '#0284c7' : idx === 1 ? '#0d9488' : '#64748b'} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      {/* Sección de Predicciones de IA claramente diferenciada */}
      <div className="ai-observatory">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.65rem', marginBottom: '0.7rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.55rem' }}>
            <Icon name="auto_awesome" filled size={22} style={{ color: 'var(--ai-indigo)' }} />
            <h2 style={{ margin: 0, fontSize: '1.12rem', color: 'var(--navy-900)' }}>
              Predicciones de Volumen · Inteligencia Artificial
            </h2>
          </div>
          <span className="ai-badge">
            <Icon name="psychology" size={14} />
            <span>Estimación IA · No dato real</span>
          </span>
        </div>

        <p style={{ margin: '0 0 1rem', fontSize: '0.82rem', color: 'var(--slate-600)' }}>
          La IA apoya el análisis del representante mediante estimaciones agregadas sin acceder a nombres, semestres ni imágenes.
        </p>

        {dashboard.predictions.length === 0 ? (
          <div
            style={{
              padding: '1.15rem 1.25rem',
              background: '#ffffff',
              borderRadius: 'var(--radius-sm)',
              border: '1px solid var(--ai-border)',
              display: 'flex',
              flexDirection: 'column',
              gap: '0.65rem'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.85rem', color: 'var(--navy-800)', fontWeight: 600 }}>
              <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.45rem' }}>
                <Icon name="info" size={18} style={{ color: 'var(--ai-indigo)' }} />
                <span>No hay predicciones: se requieren al menos cinco quejas totales o por categoría.</span>
              </span>
              <span className="font-mono" style={{ color: 'var(--ai-indigo)' }}>
                {Math.min(dashboard.total, 5)}/5
              </span>
            </div>
            <div className="progress-track">
              <div className="progress-fill" style={{ width: `${thresholdProgress}%`, background: 'var(--ai-indigo)' }} />
            </div>
          </div>
        ) : (
          <ul
            style={{
              listStyle: 'none',
              padding: 0,
              margin: 0,
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
              gap: '0.85rem'
            }}
          >
            {dashboard.predictions.map((prediction, index) => (
              <li
                key={`${prediction.scope}-${prediction.category}-${index}`}
                style={{
                  background: '#ffffff',
                  padding: '1rem 1.1rem',
                  borderRadius: 'var(--radius-sm)',
                  border: '1px solid var(--ai-border)',
                  boxShadow: 'var(--shadow-xs)'
                }}
              >
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', fontSize: '0.7rem', fontWeight: 800, color: 'var(--ai-indigo)', textTransform: 'uppercase', marginBottom: '0.35rem' }}>
                  <Icon name="model_training" size={14} />
                  <span>Estimación de IA · {prediction.scope === 'total' ? 'Global' : 'Categoría'}</span>
                </span>
                <div style={{ fontSize: '0.92rem', color: 'var(--navy-900)', fontWeight: 700, marginBottom: '0.25rem' }}>
                  {prediction.category}
                </div>
                <div style={{ fontSize: '0.88rem', color: 'var(--primary-hover)', fontWeight: 600 }}>
                  {prediction.estimate}
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </section>
  );
}
