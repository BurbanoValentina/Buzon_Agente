import { Icon } from '../../components/Icon';

export function PrivacyNotice({ checked, onChange, error }) {
  return (
    <div className="privacy-box">
      <div className="privacy-head">
        <Icon name="verified_user" filled size={20} style={{ color: 'var(--success)' }} />
        <span>Aviso de Protección de Datos y Privacidad · Ley 1581 de 2012</span>
      </div>
      <p>
        Tus datos y evidencias serán tratados exclusivamente para la gestión académica de tu facultad
        conforme a las políticas de la UCC. Los metadatos EXIF de las imágenes son eliminados
        automáticamente antes de su almacenamiento seguro.
      </p>
      <label className="privacy-checkbox-label">
        <input
          type="checkbox"
          checked={checked}
          onChange={(event) => onChange(event.target.checked)}
        />
        <span>He leído y acepto el aviso de privacidad institucional</span>
      </label>
      {error && (
        <p role="alert">
          <Icon name="error" size={18} />
          <span>{error}</span>
        </p>
      )}
    </div>
  );
}
