import { useEffect, useState } from 'react';
import { Icon } from '../../../components/Icon';
import {
  login,
  recoverWithSecurityQuestion,
  resendTelegramOtp,
  verifyTelegramOtp
} from '../../../services/adminAuthApi';

export function AdminLogin({ onAuthenticated, sessionMessage = '' }) {
  const [mode, setMode] = useState('login'); // 'login' | 'otp' | 'fallback' | 'recovery'
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [otpCode, setOtpCode] = useState('');
  const [secondsLeft, setSecondsLeft] = useState(15);
  const [answer, setAnswer] = useState('');
  const [showAnswer, setShowAnswer] = useState(false);
  const [question, setQuestion] = useState('¿Qué representante eres?');
  const [info, setInfo] = useState(sessionMessage);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (sessionMessage) {
      setInfo(sessionMessage);
    }
  }, [sessionMessage]);

  useEffect(() => {
    if (mode !== 'otp') return undefined;
    if (secondsLeft <= 0) return undefined;
    const timer = window.setInterval(() => {
      setSecondsLeft((prev) => Math.max(0, prev - 1));
    }, 1000);
    return () => window.clearInterval(timer);
  }, [mode, secondsLeft]);

  async function submit(event) {
    event.preventDefault();
    setBusy(true);
    setError('');
    setInfo('');
    try {
      if (mode === 'login') {
        const result = await login(username.trim(), password);
        if (result?.otpRequired) {
          setMode('otp');
          setOtpCode('');
          setSecondsLeft(Number(result.expiresInSeconds || 15));
          setInfo(result.message || 'Código enviado a tu Telegram.');
        } else {
          onAuthenticated?.(result);
        }
      } else if (mode === 'otp') {
        const result = await verifyTelegramOtp(otpCode.trim());
        onAuthenticated?.(result);
      } else {
        const result = await recoverWithSecurityQuestion(username.trim(), answer.trim());
        onAuthenticated?.(result);
      }
    } catch (requestError) {
      if (requestError.payload?.fallbackRequired) {
        setQuestion(requestError.payload.question || '¿Qué representante eres?');
        setMode('fallback');
        setOtpCode('');
        setAnswer('');
        setError(requestError.message);
      } else {
        setError(requestError.message);
      }
    } finally {
      setBusy(false);
    }
  }

  async function handleResendOtp() {
    setBusy(true);
    setError('');
    setInfo('');
    try {
      const result = await resendTelegramOtp();
      setSecondsLeft(Number(result.expiresInSeconds || 15));
      setOtpCode('');
      setInfo(result.message || 'Nuevo código OTP enviado a tu Telegram.');
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  const headerIcon =
    mode === 'otp' ? 'mark_chat_unread' : mode === 'fallback' || mode === 'recovery' ? 'help_center' : 'shield_lock';

  const headerTitle =
    mode === 'otp'
      ? 'Verificación en Telegram'
      : mode === 'fallback'
        ? 'Entrada Alternativa de Seguridad'
        : mode === 'recovery'
          ? 'Recuperación de Cuenta'
          : 'Acceso Administrativo';

  const headerSubtitle =
    mode === 'otp'
      ? 'Ingresa el código temporal de 6 dígitos enviado a tu bot vinculado.'
      : mode === 'fallback'
        ? 'Se agotaron los 3 intentos OTP. Responde la pregunta clave para continuar.'
        : mode === 'recovery'
          ? 'Responde la pregunta clave institucional para verificar tu identidad.'
          : 'Ingreso exclusivo para el Representante Estudiantil de Facultad.';

  return (
    <form onSubmit={submit} aria-label="Acceso de administrador" className="animate-in">
      <div style={{ textAlign: 'center', marginBottom: '0.75rem' }}>
        <div
          style={{
            width: '58px',
            height: '58px',
            background: mode === 'otp' ? '#eef2ff' : 'var(--primary-soft)',
            color: mode === 'otp' ? 'var(--ai-indigo)' : 'var(--primary-hover)',
            borderRadius: 'var(--radius-full)',
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            marginBottom: '0.75rem',
            border: '1px solid rgba(2, 132, 199, 0.2)'
          }}
        >
          <Icon name={headerIcon} filled size={28} />
        </div>
        <h1 style={{ margin: 0, fontSize: '1.35rem', fontWeight: 800, color: 'var(--navy-900)' }}>
          {headerTitle}
        </h1>
        <p style={{ margin: '0.3rem 0 0', fontSize: '0.83rem', color: 'var(--slate-500)' }}>
          {headerSubtitle}
        </p>
      </div>

      {mode === 'login' && (
        <>
          <div>
            <label htmlFor="admin-username">Nombre de Usuario</label>
            <div className="input-with-icon">
              <Icon name="person" size={18} className="input-leading-icon" />
              <input
                id="admin-username"
                autoComplete="username"
                placeholder="Ej. administrator2026-1"
                value={username}
                onChange={(event) => setUsername(event.target.value)}
              />
            </div>
          </div>

          <div>
            <label htmlFor="admin-password">Contraseña Maestra</label>
            <div className="input-with-icon input-with-trailing">
              <Icon name="key" size={18} className="input-leading-icon" />
              <input
                id="admin-password"
                autoComplete="current-password"
                type={showPassword ? 'text' : 'password'}
                placeholder="Ingresa tu contraseña"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
              />
              <button
                type="button"
                className="input-trailing-btn"
                aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                onClick={() => setShowPassword((prev) => !prev)}
              >
                <Icon name={showPassword ? 'visibility_off' : 'visibility'} size={18} />
              </button>
            </div>
          </div>
        </>
      )}

      {mode === 'otp' && (
        <div
          style={{
            background: 'var(--bg-subtle)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 'var(--radius-md)',
            padding: '1.15rem',
            display: 'flex',
            flexDirection: 'column',
            gap: '0.85rem'
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.82rem', fontWeight: 700 }}>
            <span style={{ color: 'var(--slate-600)', display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
              <Icon name="timer" size={17} />
              <span>Vigencia del código</span>
            </span>
            <span
              className="font-mono"
              style={{ color: secondsLeft <= 5 ? 'var(--danger)' : 'var(--primary-hover)' }}
            >
              {secondsLeft}s
            </span>
          </div>
          <div className="progress-track">
            <div
              className="progress-fill"
              style={{
                width: `${(secondsLeft / 15) * 100}%`,
                background: secondsLeft <= 5 ? 'var(--danger)' : undefined
              }}
            />
          </div>

          <div>
            <label htmlFor="telegram-otp-code">Código de 6 dígitos</label>
            <input
              id="telegram-otp-code"
              className="otp-input"
              inputMode="numeric"
              maxLength="6"
              placeholder="000000"
              value={otpCode}
              onChange={(event) => setOtpCode(event.target.value)}
            />
          </div>

          <button
            type="button"
            className="btn-tonal btn-sm"
            disabled={busy}
            onClick={handleResendOtp}
          >
            <Icon name="refresh" size={16} />
            <span>{secondsLeft === 0 ? 'Código expirado · Reenviar nuevo' : 'Reenviar código OTP'}</span>
          </button>
        </div>
      )}

      {(mode === 'recovery' || mode === 'fallback') && (
        <>
          <div>
            <label htmlFor="admin-username">Nombre de Usuario</label>
            <div className="input-with-icon">
              <Icon name="person" size={18} className="input-leading-icon" />
              <input
                id="admin-username"
                placeholder="Ej. administrator2026-1"
                value={username}
                onChange={(event) => setUsername(event.target.value)}
              />
            </div>
          </div>

          <div
            style={{
              background: '#f0f9ff',
              padding: '1rem',
              borderRadius: 'var(--radius-md)',
              border: '1px solid #bae6fd'
            }}
          >
            <p
              style={{
                margin: '0 0 0.6rem',
                fontWeight: 700,
                color: 'var(--primary-deep)',
                fontSize: '0.88rem',
                display: 'flex',
                alignItems: 'center',
                gap: '0.45rem'
              }}
            >
              <Icon name="quiz" size={18} />
              <span>{question}</span>
            </p>
            <label htmlFor="security-answer">Respuesta Secreta</label>
            <div className="input-with-icon input-with-trailing">
              <Icon name="verified" size={18} className="input-leading-icon" />
              <input
                id="security-answer"
                type={showAnswer ? 'text' : 'password'}
                placeholder="Ingresa tu respuesta clave..."
                value={answer}
                onChange={(event) => setAnswer(event.target.value)}
              />
              <button
                type="button"
                className="input-trailing-btn"
                aria-label={showAnswer ? 'Ocultar respuesta' : 'Mostrar respuesta'}
                onClick={() => setShowAnswer((prev) => !prev)}
              >
                <Icon name={showAnswer ? 'visibility_off' : 'visibility'} size={18} />
              </button>
            </div>
            <p style={{ margin: '0.45rem 0 0', fontSize: '0.75rem', color: 'var(--slate-600)' }}>
              Respeta exactamente mayúsculas y tildes tal como fue registrada.
            </p>
          </div>
        </>
      )}

      {info && !error && (
        <p role="status">
          <Icon name="info" size={18} />
          <span>{info}</span>
        </p>
      )}
      {error && (
        <p role="alert">
          <Icon name="error" size={18} />
          <span>{error}</span>
        </p>
      )}

      <button type="submit" disabled={busy} className="btn-primary btn-lg" style={{ width: '100%' }}>
        {busy ? (
          <>
            <span className="spinner" aria-hidden="true" />
            <span>Validando...</span>
          </>
        ) : (
          <>
            <Icon name={mode === 'otp' ? 'verified_user' : mode === 'login' ? 'login' : 'check_circle'} size={19} />
            <span>
              {mode === 'otp'
                ? 'Confirmar Código OTP'
                : mode === 'login'
                  ? 'Iniciar Sesión'
                  : 'Verificar Respuesta'}
            </span>
          </>
        )}
      </button>

      <button
        type="button"
        className="btn-ghost btn-sm"
        onClick={() => {
          setError('');
          setInfo('');
          setMode(mode === 'login' ? 'recovery' : 'login');
        }}
        style={{ width: '100%' }}
      >
        <Icon name={mode === 'login' ? 'help' : 'arrow_back'} size={17} />
        <span>
          {mode === 'login'
            ? '¿Olvidaste tu contraseña? Usar pregunta clave'
            : 'Volver al inicio de sesión'}
        </span>
      </button>
    </form>
  );
}
