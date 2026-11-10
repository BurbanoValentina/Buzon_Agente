import { useEffect, useState } from 'react';
import { Icon } from '../../../components/Icon';
import {
  confirmTelegramLink,
  getTelegramBotInfo,
  getTelegramStatus,
  sendTelegramTest,
  setTelegramNotifications,
  startTelegramLink,
  unlinkTelegram
} from '../../../services/telegramApi';

export function TelegramSettings() {
  const [status, setStatus] = useState({ linked: false, notificationsEnabled: false });
  const [botInfo, setBotInfo] = useState({ enabled: false, username: '', firstName: '' });
  const [accountId, setAccountId] = useState('');
  const [code, setCode] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function refresh() {
    try {
      const [currentStatus, currentBot] = await Promise.all([
        getTelegramStatus(),
        getTelegramBotInfo().catch(() => ({ enabled: false, username: '', firstName: '' }))
      ]);
      setStatus(currentStatus);
      setBotInfo(currentBot);
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  useEffect(() => {
    refresh();
  }, []);

  async function start(event) {
    event?.preventDefault?.();
    const trimmedId = accountId.trim();
    if (!/^-?\d+$/.test(trimmedId)) {
      setError('Ingresa el ID numérico de chat (solo dígitos) que te respondió el bot con /start.');
      return;
    }
    setError('');
    setMessage('');
    setBusy(true);
    try {
      await startTelegramLink(trimmedId);
      setAccountId(trimmedId);
      setMessage('Código enviado a tu Telegram (válido durante 10 minutos). Revisa tu chat con el bot.');
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function confirm(event) {
    event?.preventDefault?.();
    const trimmedId = accountId.trim();
    const trimmedCode = code.trim();
    if (!/^-?\d+$/.test(trimmedId) || !trimmedCode) {
      setError('Ingresa primero tu ID numérico de chat y el código de 6 dígitos recibido.');
      return;
    }
    setError('');
    setMessage('');
    setBusy(true);
    try {
      setStatus(await confirmTelegramLink(trimmedId, trimmedCode));
      setMessage('Cuenta Telegram vinculada correctamente. Autenticación 2FA activada.');
      setCode('');
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function unlink() {
    setError('');
    setMessage('');
    setBusy(true);
    try {
      setStatus(await unlinkTelegram());
      setMessage('Cuenta de Telegram desvinculada.');
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function updateNotifications(enabled) {
    setError('');
    try {
      setStatus(await setTelegramNotifications(enabled));
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function testConnection() {
    setError('');
    setMessage('');
    setBusy(true);
    try {
      const res = await sendTelegramTest();
      setMessage(res.message || 'Mensaje de prueba enviado.');
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  const botUrl = botInfo.username ? `https://t.me/${botInfo.username}` : null;

  return (
    <section aria-label="Configuración de Telegram">
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '1.35rem',
          flexWrap: 'wrap',
          gap: '1rem'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
          <span className="card-title-icon">
            <Icon name="smart_toy" size={20} />
          </span>
          <div>
            <h1 style={{ margin: 0, fontSize: '1.35rem', color: 'var(--navy-900)' }}>
              Bot Oficial de Telegram · 2FA & Alertas
            </h1>
            <p style={{ margin: '0.15rem 0 0', fontSize: '0.84rem', color: 'var(--slate-500)' }}>
              Verificación OTP de 15 segundos, notificaciones mínimas y consultas de solo lectura.
            </p>
          </div>
        </div>

        <div
          style={{
            padding: '0.38rem 0.9rem',
            borderRadius: 'var(--radius-full)',
            background: status.linked ? 'var(--success-bg)' : 'var(--bg-muted)',
            color: status.linked ? 'var(--success-text)' : 'var(--slate-600)',
            border: `1px solid ${status.linked ? 'var(--success-border)' : 'var(--border-subtle)'}`,
            fontWeight: 700,
            fontSize: '0.82rem',
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.45rem'
          }}
        >
          <span className={status.linked ? 'live-beacon' : 'offline-dot'} aria-hidden="true" />
          <span>Estado: {status.linked ? 'vinculado' : 'no vinculado'}</span>
        </div>
      </div>

      {/* Banner de conexión directa al Bot */}
      <div
        style={{
          background: 'linear-gradient(135deg, #0c4a6e 0%, #0284c7 100%)',
          color: '#ffffff',
          borderRadius: 'var(--radius-md)',
          padding: '1.15rem 1.35rem',
          marginBottom: '1.35rem',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1rem'
        }}
      >
        <div>
          <div style={{ fontSize: '0.74rem', textTransform: 'uppercase', letterSpacing: '0.06em', color: '#bae6fd', fontWeight: 700 }}>
            Bot Configurado en Backend
          </div>
          <div style={{ fontSize: '1.08rem', fontWeight: 800, marginTop: '0.15rem' }}>
            {botInfo.username ? `@${botInfo.username}` : botInfo.firstName || 'Bot Buzón UCC'}
          </div>
          <div style={{ fontSize: '0.8rem', color: '#e0f2fe', marginTop: '0.2rem' }}>
            Comandos de solo lectura disponibles: <code className="font-mono">Ayuda</code>,{' '}
            <code className="font-mono">Resumen</code>, <code className="font-mono">Pendientes</code>,{' '}
            <code className="font-mono">Mes</code>
          </div>
        </div>

        {botUrl && (
          <a
            href={botUrl}
            target="_blank"
            rel="noreferrer"
            className="btn btn-secondary btn-sm"
            style={{ color: 'var(--navy-900)' }}
          >
            <Icon name="open_in_new" size={16} />
            <span>Abrir Bot en Telegram</span>
          </a>
        )}
      </div>

      {message && (
        <p role="status" style={{ marginBottom: '1rem' }}>
          <Icon name="check_circle" filled size={18} />
          <span>{message}</span>
        </p>
      )}
      {error && (
        <p role="alert" style={{ marginBottom: '1rem' }}>
          <Icon name="error" size={18} />
          <span>{error}</span>
        </p>
      )}

      <div className="grid-2" style={{ alignItems: 'start' }}>
        {/* Tarjeta Asistente de Vinculación */}
        <div className="chart-surface" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Icon name="link" size={20} style={{ color: 'var(--primary)' }} />
            <h3 style={{ margin: 0, fontSize: '1.02rem', color: 'var(--navy-900)' }}>
              Asistente de Vinculación
            </h3>
          </div>

          <div className="wizard-step">
            <span className="step-number">1</span>
            <div style={{ fontSize: '0.81rem', color: 'var(--slate-700)' }}>
              <strong>Obtén tu ID numérico:</strong> abre el bot en Telegram, envía{' '}
              <code className="font-mono">/start</code> y copia el número que te responde.
            </div>
          </div>

          <div className="wizard-step">
            <span className="step-number">2</span>
            <form onSubmit={start} style={{ width: '100%', gap: '0.6rem' }}>
              <div>
                <label htmlFor="telegram-account">Identificador numérico de chat</label>
                <div className="input-with-icon">
                  <Icon name="tag" size={18} className="input-leading-icon" />
                  <input
                    id="telegram-account"
                    inputMode="numeric"
                    placeholder="Ej. 123456789"
                    value={accountId}
                    onChange={(event) => setAccountId(event.target.value)}
                  />
                </div>
              </div>
              <button
                type="submit"
                className="btn-primary btn-sm"
                disabled={busy}
                style={{ width: '100%' }}
              >
                <Icon name="send" size={16} />
                <span>Solicitar Código (10 min)</span>
              </button>
            </form>
          </div>

          <div className="wizard-step">
            <span className="step-number">3</span>
            <form onSubmit={confirm} style={{ width: '100%', gap: '0.6rem' }}>
              <div>
                <label htmlFor="telegram-code">Código temporal recibido en Telegram</label>
                <div className="input-with-icon">
                  <Icon name="pin" size={18} className="input-leading-icon" />
                  <input
                    id="telegram-code"
                    inputMode="numeric"
                    maxLength="6"
                    placeholder="Código de 6 dígitos"
                    value={code}
                    onChange={(event) => setCode(event.target.value)}
                  />
                </div>
              </div>
              <button
                type="submit"
                className="btn-success btn-sm"
                disabled={busy}
                style={{ width: '100%' }}
              >
                <Icon name="verified" size={17} />
                <span>Confirmar Vinculación</span>
              </button>
            </form>
          </div>
        </div>

        {/* Tarjeta Preferencias, Prueba y Seguridad */}
        <div className="chart-surface" style={{ display: 'flex', flexDirection: 'column', gap: '1.1rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Icon name="tune" size={20} style={{ color: 'var(--primary)' }} />
            <h3 style={{ margin: 0, fontSize: '1.02rem', color: 'var(--navy-900)' }}>
              Preferencias y Seguridad
            </h3>
          </div>

          <div
            style={{
              background: 'var(--bg-subtle)',
              padding: '1rem',
              borderRadius: 'var(--radius-sm)',
              border: '1px solid var(--border-subtle)'
            }}
          >
            <label
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.65rem',
                cursor: 'pointer',
                margin: 0,
                fontWeight: 700,
                fontSize: '0.88rem'
              }}
            >
              <input
                type="checkbox"
                checked={status.notificationsEnabled}
                onChange={(event) => updateNotifications(event.target.checked)}
                style={{ width: '18px', height: '18px', accentColor: 'var(--primary)' }}
              />
              <span>Activar alertas de nuevas quejas</span>
            </label>
            <p style={{ margin: '0.45rem 0 0 1.75rem', fontSize: '0.78rem', color: 'var(--slate-500)', lineHeight: 1.45 }}>
              Solo notifica identificador, categoría, ubicación, modalidad y fecha. Nunca envía
              asunto, descripción, nombres ni fotografías.
            </p>
          </div>

          <div
            style={{
              background: 'var(--bg-subtle)',
              padding: '1rem',
              borderRadius: 'var(--radius-sm)',
              border: '1px solid var(--border-subtle)',
              display: 'flex',
              flexDirection: 'column',
              gap: '0.65rem'
            }}
          >
            <div style={{ fontSize: '0.82rem', fontWeight: 700, color: 'var(--navy-800)', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <Icon name="notifications_active" size={18} style={{ color: 'var(--primary)' }} />
              <span>Verificación de entrega</span>
            </div>
            <button
              type="button"
              className="btn-tonal btn-sm"
              disabled={!status.linked || busy}
              onClick={testConnection}
              style={{ width: '100%' }}
            >
              <Icon name="mark_chat_read" size={17} />
              <span>Enviar Mensaje de Prueba</span>
            </button>
          </div>

          <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: '1rem' }}>
            <p style={{ margin: '0 0 0.55rem', fontSize: '0.8rem', color: 'var(--slate-500)' }}>
              Si deseas cambiar de cuenta o desactivar el segundo factor por Telegram:
            </p>
            <button
              type="button"
              className="btn-danger btn-sm"
              disabled={!status.linked || busy}
              onClick={unlink}
              style={{ width: '100%' }}
            >
              <Icon name="link_off" size={17} />
              <span>Desvincular Cuenta</span>
            </button>
          </div>
        </div>
      </div>
    </section>
  );
}
