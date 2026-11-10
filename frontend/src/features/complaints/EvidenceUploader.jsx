import { useEffect, useMemo, useState } from 'react';
import { Icon } from '../../components/Icon';

function formatBytes(bytes) {
  if (!bytes || bytes <= 0) return '0 KB';
  const mb = bytes / (1024 * 1024);
  if (mb >= 1) return `${mb.toFixed(1)} MB`;
  return `${Math.max(1, Math.round(bytes / 1024))} KB`;
}

export function EvidenceUploader({ files, onChange, error }) {
  const [dragActive, setDragActive] = useState(false);

  const previews = useMemo(
    () =>
      files.map((file) => ({
        file,
        url: typeof URL?.createObjectURL === 'function' ? URL.createObjectURL(file) : ''
      })),
    [files]
  );

  useEffect(() => {
    return () => {
      previews.forEach((item) => {
        if (item.url && typeof URL?.revokeObjectURL === 'function') {
          URL.revokeObjectURL(item.url);
        }
      });
    };
  }, [previews]);

  function handleAddFiles(selectedList) {
    const incoming = [...selectedList];
    if (!incoming.length) return;
    onChange([...files, ...incoming]);
  }

  function handleRemove(indexToRemove) {
    onChange(files.filter((_, idx) => idx !== indexToRemove));
  }

  function handleDrop(event) {
    event.preventDefault();
    setDragActive(false);
    if (event.dataTransfer?.files?.length) {
      handleAddFiles(event.dataTransfer.files);
    }
  }

  return (
    <div
      className={`evidence-dropzone ${dragActive ? 'drag-active' : ''}`}
      onDragOver={(event) => {
        event.preventDefault();
        setDragActive(true);
      }}
      onDragLeave={() => setDragActive(false)}
      onDrop={handleDrop}
    >
      <div className="dropzone-icon-circle">
        <Icon name="add_a_photo" size={24} />
      </div>

      <div>
        <label htmlFor="evidence" className="dropzone-trigger-label">
          <Icon name="cloud_upload" size={18} />
          <span>Adjuntar Evidencias Fotográficas (Opcional)</span>
        </label>
        <p style={{ margin: '0.25rem 0 0', fontSize: '0.8rem', color: 'var(--slate-500)' }}>
          Arrastra tus imágenes aquí o selecciónalas. JPG, JPEG o PNG · Máx. 5 fotos (100 MB c/u).
        </p>
      </div>

      <input
        id="evidence"
        className="evidence-file-input"
        type="file"
        accept="image/jpeg,image/png"
        multiple
        onChange={(event) => {
          handleAddFiles(event.target.files || []);
          event.target.value = '';
        }}
      />

      <div>
        <span
          className="evidence-count-badge"
          style={files.length > 5 ? { borderColor: 'var(--danger-border)', color: 'var(--danger-text)', background: 'var(--danger-bg)' } : undefined}
        >
          <Icon name="photo_library" size={16} />
          <span>
            {files.length}/5 imágenes seleccionadas{files.length > 5 ? ' (máximo 5)' : ''}
          </span>
        </span>
      </div>

      {previews.length > 0 && (
        <div className="thumb-grid">
          {previews.map((item, idx) => (
            <div key={`${item.file.name}-${idx}`} className="thumb-card animate-in">
              {item.url ? (
                <img src={item.url} alt={item.file.name} className="thumb-preview" />
              ) : (
                <div
                  className="thumb-preview"
                  style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--slate-400)' }}
                >
                  <Icon name="image" size={26} />
                </div>
              )}
              <div className="thumb-meta">
                <div style={{ minWidth: 0 }}>
                  <div className="thumb-name" title={item.file.name}>
                    {item.file.name}
                  </div>
                  <div style={{ fontSize: '0.68rem', color: 'var(--slate-500)' }}>
                    {formatBytes(item.file.size)}
                  </div>
                </div>
                <button
                  type="button"
                  className="thumb-remove-btn"
                  aria-label={`Quitar ${item.file.name}`}
                  onClick={() => handleRemove(idx)}
                >
                  <Icon name="close" size={15} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {error && (
        <p role="alert">
          <Icon name="error" size={18} />
          <span>{error}</span>
        </p>
      )}
    </div>
  );
}
