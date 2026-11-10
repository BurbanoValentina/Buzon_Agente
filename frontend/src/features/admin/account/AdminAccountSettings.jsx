import { useEffect, useState } from 'react';
import { Icon } from '../../../components/Icon';
import { changeCredentials, requestCredentialsOtp } from '../../../services/adminAuthApi';
import { getTelegramStatus } from '../../../services/telegramApi';

export function AdminAccountSettings({ onUpdated }) {
  const [form, setForm] = useState({
    username: '',
    currentPassword: '',
    newPassword: '',
    securityAnswer: '',
    otpCode: ''
  });
  const [showCurrent, setShowCurrent] = useState(false);
  const [showNew, setShowNew] = useState(false);
  const [telegramLinked, setTelegramLinked] = useState(false);
  const [otpSeconds, setOtpSeconds] = useState(0);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    getTelegramStatus()
      .then((res) => setTelegramLinked(Boolean(res?.linked)))
      .catch(() => {});
  }, []);

  useEffect(() => {
    if (otpSeconds <= 0) return undefined;
    const timer = window.setInterval(() => {
      setOtpSeconds((prev) => Math.max(0, prev - 1));
    }, 1000);
    return () => window.clearInterval(timer);
  }, [otpSeconds]);

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function sendOtp() {
    setError('');
    setMessage('');
    setBusy(true);
    try {
      const res = await requestCredentialsOtp();
      if (res?.otpRequired) {
        setOtpSeconds(Number(res.expiresInSeconds || 15));
      }
      setMessage(res?.message || 'Código enviado a tu Telegram.');
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function submit(event) {
    event.preventDefault();
    setMessage('');
    setError('');
    setBusy(true);
    try {
      const result = await changeCredentials(form);
      setMessage('Credenciales actualizadas correctamente.');
      setForm({ username: '', currentPassword: '', newPassword: '', securityAnswer: '', otpCode: '' });
      setOtpSeconds(0);
      onUpdated?.(result);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <form onSubmit={submit} aria-label="Configuración de administrador" style={{ maxWidth: '640px' }}>
      <div style={{ marginBottom: '0.5rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          <span className="card-title-icon">
            <Icon name="manage_accounts" size={20} />
          </span>
          <div>
            <h1 style={{ margin: 0, fontSize: '1.35rem', color: 'var(--navy-900)' }}>
              Configuración de Cuenta Administrativa
            </h1>
            <p style={{ margin: '0.15rem 0 0', fontSize: '0.84rem', color: 'var(--slate-500)' }}>
              Actualiza tus credenciales institucionales y la pregunta clave de recuperación.
            </p>
          </div>
        </div>
      </div>

      {/* Guía de formato institucional */}
      <div className="context-hint" style={{ marginTop: 0 }}>
        <Icon name="policy" size={18} />
        <span>
          Formato exigido: usuario <code className="font-mono">administrator&lt;período&gt;</code> y contraseña{' '}
          <code className="font-mono">RepresentanteIng####</code>.
        </span>
      </div>

      <div className="grid-2">
        <div>
          <label htmlFor="new-username">Nuevo usuario</label>
          <div className="input-with-icon">
            <Icon name="badge" size={18} className="input-leading-icon" />
            <input
              id="new-username"
              placeholder="Ej. administrator2026-2"
              value={form.username}
              onChange={(event) => update('username', event.target.value)}
            />
          </div>
        </div>

        <div>
          <label htmlFor="current-password">Contraseña actual</label>
          <div className="input-with-icon input-with-trailing">
            <Icon name="lock" size={18} className="input-leading-icon" />
            <input
              id="current-password"
              type={showCurrent ? 'text' : 'password'}
              placeholder="Contraseña vigente"
              value={form.currentPassword}
              onChange={(event) => update('currentPassword', event.target.value)}
            />
            <button
              type="button"
              className="input-trailing-btn"
              aria-label={showCurrent ? 'Ocultar contraseña actual' : 'Mostrar contraseña actual'}
              onClick={() => setShowCurrent((prev) => !prev)}
            >
              <Icon name={showCurrent ? 'visibility_off' : 'visibility'} size={18} />
            </button>
          </div>
        </div>
      </div>

      <div>
        <label htmlFor="new-password">Nueva contraseña</label>
        <div className="input-with-icon input-with-trailing">
          <Icon name="key" size={18} className="input-leading-icon" />
          <input
            id="new-password"
            type={showNew ? 'text' : 'password'}
            placeholder="Ej. RepresentanteIng2027"
            value={form.newPassword}
            onChange={(event) => update('newPassword', event.target.value)}
          />
          <button
            type="button"
            className="input-trailing-btn"
            aria-label={showNew ? 'Ocultar nueva contraseña' : 'Mostrar nueva contraseña'}
            onClick={() => setShowNew((prev) => !prev)}
          >
            <Icon name={showNew ? 'visibility_off' : 'visibility'} size={18} />
          </button>
        </div>
      </div>

      <div
        style={{
          background: 'var(--bg-subtle)',
          padding: '1rem 1.15rem',
          borderRadius: 'var(--radius-md)',
          border: '1px solid var(--border-subtle)'
        }}
      >
        <label htmlFor="security-answer" style={{ color: 'var(--primary-deep)' }}>
          Nueva respuesta a la pregunta clave: <em>¿Qué representante eres?</em>
        </label>
        <div className="input-with-icon">
          <Icon name="shield" size={18} className="input-leading-icon" />
          <input
            id="security-answer"
            placeholder="Tu respuesta secreta de recuperación"
            value={form.securityAnswer}
            onChange={(event) => update('securityAnswer', event.target.value)}
          />
        </div>
      </div>

      {telegramLinked && (
        <div
          style={{
            background: '#f0f9ff',
            border: '1px solid #bae6fd',
            borderRadius: 'var(--radius-md)',
            padding: '1rem 1.15rem',
            display: 'flex',
            flexDirection: 'column',
            gap: '0.75rem'
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.5rem' }}>
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.45rem', fontWeight: 700, fontSize: '0.88rem', color: 'var(--primary-deep)' }}>
              <Icon name="security" size={18} />
              <span>Confirmación Telegram 2FA (15s)</span>
            </span>
            <button type="button" className="btn-tonal btn-sm" onClick={sendOtp} disabled={busy}>
              <Icon name="send" size={16} />
              <span>{otpSeconds > 0 ? `Vigencia ${otpSeconds}s · Reenviar` : 'Solicitar Código OTP'}</span>
            </button>
          </div>
          <div>
            <label htmlFor="credentials-otp">Código OTP de Telegram</label>
            <input
              id="credentials-otp"
              className="otp-input"
              inputMode="numeric"
              maxLength="6"
              placeholder="000000"
              value={form.otpCode}
              onChange={(event) => update('otpCode', event.target.value)}
            />
          </div>
        </div>
      )}

      {message && (
        <p role="status">
          <Icon name="check_circle" filled size={18} />
          <span>{message}</span>
        </p>
      )}
      {error && (
        <p role="alert">
          <Icon name="error" size={18} />
          <span>{error}</span>
        </p>
      )}

      <div>
        <button type="submit" disabled={busy} className="btn-primary">
          {busy ? (
            <>
              <span className="spinner" aria-hidden="true" />
              <span>Actualizando...</span>
            </>
          ) : (
            <>
              <Icon name="save" size={18} />
              <span>Guardar Cambios</span>
            </>
          )}
        </button>
      </div>
    </form>
  );
}
