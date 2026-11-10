import { useEffect, useState } from 'react';
import { Icon } from './components/Icon';
import { getAdminSession, logout } from './services/adminAuthApi';
import { PublicComplaintForm } from './features/complaints/PublicComplaintForm';
import { AdminAccessLogo } from './features/admin/account/AdminAccessLogo';
import { AdminLogin } from './features/admin/account/AdminLogin';
import { AdminComplaintsPage } from './features/admin/complaints/AdminComplaintsPage';
import { AdminDashboard } from './features/admin/dashboard/AdminDashboard';
import { AdminAccountSettings } from './features/admin/account/AdminAccountSettings';
import { WordReportEditor } from './features/admin/reports/WordReportEditor';
import { TelegramSettings } from './features/admin/telegram/TelegramSettings';
import { MonthlyFollowUp } from './features/admin/status/MonthlyFollowUp';

const ADMIN_TABS = [
  { id: 'complaints', label: 'Quejas', icon: 'inbox' },
  { id: 'dashboard', label: 'Estadísticas & IA', icon: 'monitoring' },
  { id: 'reports', label: 'Reportes Word', icon: 'description' },
  { id: 'status', label: 'Seguimiento', icon: 'calendar_month' },
  { id: 'telegram', label: 'Telegram Bot', icon: 'smart_toy' },
  { id: 'account', label: 'Mi Cuenta', icon: 'manage_accounts' }
];

export function App() {
  const [view, setView] = useState('public'); // 'public', 'login', 'admin'
  const [adminTab, setAdminTab] = useState('complaints');
  const [adminUser, setAdminUser] = useState(null);
  const [sessionMessage, setSessionMessage] = useState('');

  useEffect(() => {
    getAdminSession()
      .then((res) => {
        if (res?.authenticated && res?.username) {
          setAdminUser({ username: res.username });
        }
      })
      .catch(() => {});
  }, []);

  useEffect(() => {
    function handleSessionExpired(event) {
      if (view === 'admin' || adminUser) {
        setAdminUser(null);
        setSessionMessage(event?.detail || 'La sesión expiró por inactividad.');
        setView('login');
      }
    }
    window.addEventListener('ucc:session-expired', handleSessionExpired);
    return () => window.removeEventListener('ucc:session-expired', handleSessionExpired);
  }, [view, adminUser]);

  async function handleOpenLogin() {
    setSessionMessage('');
    try {
      const session = await getAdminSession();
      if (session?.authenticated && session?.username) {
        setAdminUser({ username: session.username });
        setView('admin');
        return;
      }
    } catch {
      // Ignore network hiccup and show login view
    }
    setAdminUser(null);
    setView('login');
  }

  const handleAuthenticated = (user) => {
    setSessionMessage('');
    setAdminUser(user);
    setView('admin');
  };

  async function handleLogout() {
    try {
      await logout();
    } catch {
      // Ignore network issues on logout cleanup
    }
    setAdminUser(null);
    setSessionMessage('');
    setView('public');
  }

  return (
    <div className="app-wrapper">
      {/* Header Institucional */}
      <header className="app-header">
        <div className="header-inner">
          <div className="brand-badge">
            <div className="brand-emblem" aria-hidden="true">
              <Icon name="mark_email_read" filled size={22} />
            </div>
            <div>
              <h1 className="brand-title">
                <span>Buzón UCC</span>
                <span className="brand-chip">VOZ ESTUDIANTIL</span>
              </h1>
              <p className="brand-subtitle">Representación Estudiantil · Facultad de Ingeniería</p>
            </div>
          </div>

          <div className="header-actions">
            {view !== 'public' && (
              <button
                type="button"
                className="btn-secondary btn-sm btn-pill"
                onClick={() => setView('public')}
              >
                <Icon name="arrow_back" size={16} />
                <span>Buzón Público</span>
              </button>
            )}
            <AdminAccessLogo onOpen={handleOpenLogin} />
          </div>
        </div>
      </header>

      {/* Contenido Principal */}
      <main className={`main-container ${view === 'public' ? 'narrow' : ''}`}>
        {view === 'public' && (
          <div className="animate-in">
            {/* Hero Banner */}
            <section className="hero-banner" aria-label="Bienvenida">
              <div className="hero-content">
                <div>
                  <span className="hero-tag">
                    <Icon name="verified_user" size={15} />
                    <span>Canal Oficial de Participación</span>
                  </span>
                  <h2 className="hero-title">Tu voz construye y transforma nuestra facultad</h2>
                  <p className="hero-desc">
                    Reporta situaciones académicas, de infraestructura o bienestar directamente a tu
                    representante estudiantil, con modalidad anónima o identificada y respaldo fotográfico seguro.
                  </p>
                </div>

                <div className="hero-stat-box">
                  <div className="hero-stat-item">
                    <Icon name="visibility_off" size={19} />
                    <span>Anonimato criptográfico real</span>
                  </div>
                  <div className="hero-stat-item">
                    <Icon name="enhanced_encryption" size={19} />
                    <span>Imágenes sin metadatos EXIF</span>
                  </div>
                  <div className="hero-stat-item">
                    <Icon name="track_changes" size={19} />
                    <span>Seguimiento mensual verificable</span>
                  </div>
                </div>
              </div>
            </section>

            {/* Pilares de Confianza */}
            <section className="pillars-row" aria-label="Garantías del buzón">
              <article className="pillar-card">
                <div className="pillar-icon-box sky">
                  <Icon name="shield_lock" size={22} />
                </div>
                <div>
                  <h3 className="pillar-title">100% Confidencial</h3>
                  <p className="pillar-text">
                    El modo anónimo no solicita ni vincula nombres, semestres ni direcciones IP.
                  </p>
                </div>
              </article>

              <article className="pillar-card">
                <div className="pillar-icon-box emerald">
                  <Icon name="photo_camera" size={22} />
                </div>
                <div>
                  <h3 className="pillar-title">Evidencia Protegida</h3>
                  <p className="pillar-text">
                    Soporte hasta 5 fotos JPG o PNG sanitizadas automáticamente en la recepción.
                  </p>
                </div>
              </article>

              <article className="pillar-card">
                <div className="pillar-icon-box indigo">
                  <Icon name="insights" size={22} />
                </div>
                <div>
                  <h3 className="pillar-title">Gestión Transparente</h3>
                  <p className="pillar-text">
                    Cada caso avanza desde Pendiente hasta su cumplimiento mensual documentado.
                  </p>
                </div>
              </article>
            </section>

            {/* Formulario Público */}
            <section className="content-card">
              <div className="card-header-styled">
                <h3 className="card-title-styled">
                  <span className="card-title-icon">
                    <Icon name="edit_note" size={21} />
                  </span>
                  <span>Registro de Queja Académica</span>
                </h3>
                <span className="char-counter-pill">Los campos con (*) son obligatorios</span>
              </div>
              <PublicComplaintForm />
            </section>
          </div>
        )}

        {view === 'login' && (
          <div className="content-card animate-in" style={{ maxWidth: '460px', margin: '2.5rem auto' }}>
            <AdminLogin onAuthenticated={handleAuthenticated} sessionMessage={sessionMessage} />
          </div>
        )}

        {view === 'admin' && (
          <div className="animate-in">
            <div className="admin-workspace-head">
              <div>
                <h2 style={{ margin: 0, fontSize: '1.5rem', fontWeight: 800, color: 'var(--navy-900)' }}>
                  Panel del Representante
                </h2>
                <p style={{ margin: '0.2rem 0 0', fontSize: '0.85rem', color: 'var(--slate-500)' }}>
                  Gestión integral de quejas, analítica estadistica, reportes Word y seguridad Telegram.
                </p>
              </div>
              {adminUser?.username && (
                <div className="admin-badge-pill">
                  <Icon name="verified" filled size={18} style={{ color: 'var(--success)' }} />
                  <span className="font-mono">{adminUser.username}</span>
                </div>
              )}
            </div>

            <nav className="admin-nav" aria-label="Navegación del panel">
              {ADMIN_TABS.map((tab) => (
                <button
                  key={tab.id}
                  type="button"
                  className={`admin-nav-btn ${adminTab === tab.id ? 'active' : ''}`}
                  onClick={() => setAdminTab(tab.id)}
                >
                  <Icon name={tab.icon} size={18} />
                  <span>{tab.label}</span>
                </button>
              ))}
              <button
                type="button"
                className="admin-nav-btn logout"
                onClick={handleLogout}
              >
                <Icon name="logout" size={18} />
                <span>Cerrar Sesión</span>
              </button>
            </nav>

            <div className="content-card">
              {adminTab === 'complaints' && <AdminComplaintsPage />}
              {adminTab === 'dashboard' && <AdminDashboard />}
              {adminTab === 'reports' && <WordReportEditor />}
              {adminTab === 'status' && <MonthlyFollowUp />}
              {adminTab === 'telegram' && <TelegramSettings />}
              {adminTab === 'account' && (
                <AdminAccountSettings
                  onUpdated={(updated) => setAdminUser((prev) => ({ ...prev, ...updated }))}
                />
              )}
            </div>
          </div>
        )}
      </main>

      <footer className="app-footer">
        <p style={{ margin: 0, display: 'inline-flex', alignItems: 'center', gap: '0.4rem', flexWrap: 'wrap', justifyContent: 'center' }}>
          <Icon name="school" size={16} style={{ color: 'var(--primary)' }} />
          <span>Buzón UCC · Voz Estudiantil · Universidad Cooperativa de Colombia</span>
        </p>
      </footer>
    </div>
  );
}
