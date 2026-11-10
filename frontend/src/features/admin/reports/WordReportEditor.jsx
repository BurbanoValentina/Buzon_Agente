import { useState } from 'react';
import { Icon } from '../../../components/Icon';
import { CATEGORIES } from '../../complaints/complaintValidation';
import {
  createWordReport,
  downloadWordReport,
  updateWordReport
} from '../../../services/wordReportApi';

const REPORT_SECTIONS = [
  { num: '1', label: 'Portada', icon: 'article' },
  { num: '2', label: 'Resumen', icon: 'summarize' },
  { num: '3', label: 'Detalle', icon: 'list_alt' },
  { num: '4', label: 'Estadísticas', icon: 'bar_chart' },
  { num: '5', label: 'Completado', icon: 'check_circle' },
  { num: '6', label: 'No Completado', icon: 'pending' },
  { num: '7', label: 'Conclusiones', icon: 'auto_awesome' }
];

export function WordReportEditor() {
  const [draft, setDraft] = useState(null);
  const [selection, setSelection] = useState({ complaintIds: [], category: '' });
  const [error, setError] = useState('');
  const [statusMessage, setStatusMessage] = useState('');
  const [busy, setBusy] = useState(false);

  async function generate(event) {
    event.preventDefault();
    setError('');
    setStatusMessage('');
    setBusy(true);
    try {
      const generated = await createWordReport(selection);
      setDraft(generated);
      setStatusMessage('Borrador generado con 7 secciones listas para revisión y edición.');
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function save() {
    setError('');
    setStatusMessage('');
    setBusy(true);
    try {
      setDraft(await updateWordReport(draft.id, draft));
      setStatusMessage('Cambios guardados correctamente en el borrador.');
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function download() {
    setError('');
    setBusy(true);
    try {
      const blob = await downloadWordReport(draft.id);
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'reporte.docx';
      link.click();
      URL.revokeObjectURL(url);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  const wordCount = draft?.content ? draft.content.trim().split(/\s+/).filter(Boolean).length : 0;

  return (
    <section aria-label="Reportes Word editables">
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', marginBottom: '1.35rem' }}>
        <span className="card-title-icon">
          <Icon name="description" size={20} />
        </span>
        <div>
          <h1 style={{ margin: 0, fontSize: '1.35rem', color: 'var(--navy-900)' }}>
            Estudio de Reportes Ejecutivos (.docx)
          </h1>
          <p style={{ margin: '0.15rem 0 0', fontSize: '0.84rem', color: 'var(--slate-500)' }}>
            Genera borradores estructurados en 7 secciones obligatorias, edita su contenido y descarga en Word.
          </p>
        </div>
      </div>

      {/* 7 Mandatory Sections Blueprint */}
      <div className="word-sections-bar">
        {REPORT_SECTIONS.map((sec) => (
          <div key={sec.num} className="word-section-pill">
            <Icon name={sec.icon} size={18} style={{ color: 'var(--primary)' }} />
            <span>
              {sec.num}. {sec.label}
            </span>
          </div>
        ))}
      </div>

      {error && (
        <p role="alert">
          <Icon name="error" size={18} />
          <span>{error}</span>
        </p>
      )}
      {statusMessage && (
        <p role="status">
          <Icon name="check_circle" filled size={18} />
          <span>{statusMessage}</span>
        </p>
      )}

      <div className="chart-surface" style={{ marginTop: '1rem', marginBottom: '1.5rem' }}>
        <form onSubmit={generate}>
          <div className="grid-2">
            <div>
              <label htmlFor="word-category">Seleccionar por Categoría</label>
              <select
                id="word-category"
                value={selection.category}
                onChange={(event) => setSelection({ ...selection, category: event.target.value })}
              >
                <option value="">Selecciona una categoría...</option>
                {CATEGORIES.map((category) => (
                  <option key={category} value={category}>
                    {category}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label htmlFor="word-ids">O especificar IDs públicos (separados por coma)</label>
              <div className="input-with-icon">
                <Icon name="tag" size={18} className="input-leading-icon" />
                <input
                  id="word-ids"
                  placeholder="Ej. uuid-1, uuid-2"
                  value={selection.complaintIds.join(', ')}
                  onChange={(event) =>
                    setSelection({
                      ...selection,
                      complaintIds: event.target.value
                        .split(',')
                        .map((item) => item.trim())
                        .filter(Boolean)
                    })
                  }
                />
              </div>
            </div>
          </div>

          <div>
            <button type="submit" disabled={busy} className="btn-primary">
              {busy ? (
                <>
                  <span className="spinner" aria-hidden="true" />
                  <span>Generando borrador...</span>
                </>
              ) : (
                <>
                  <Icon name="auto_awesome" size={18} />
                  <span>Generar Borrador Editable</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>

      {draft && (
        <div
          className="expediente-panel animate-in"
          style={{ marginTop: '1.25rem', borderColor: '#cbd5e1' }}
        >
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              flexWrap: 'wrap',
              gap: '0.75rem',
              marginBottom: '1.15rem',
              borderBottom: '1px solid var(--border-subtle)',
              paddingBottom: '0.85rem'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.55rem' }}>
              <Icon name="edit_document" size={22} style={{ color: 'var(--primary)' }} />
              <div>
                <h3 style={{ margin: 0, fontSize: '1.08rem', color: 'var(--navy-900)' }}>
                  Borrador de Documento #{draft.id}
                </h3>
                <span style={{ fontSize: '0.76rem', color: 'var(--slate-500)' }}>
                  Propuesta editable antes de generar el archivo Word oficial
                </span>
              </div>
            </div>
            <span className="char-counter-pill font-mono">{wordCount} palabras</span>
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label htmlFor="word-title">Título del Documento</label>
            <input
              id="word-title"
              value={draft.title}
              onChange={(event) => setDraft({ ...draft, title: event.target.value })}
            />
          </div>

          <div style={{ marginBottom: '1.25rem' }}>
            <label htmlFor="word-content">Contenido Completo (7 Secciones)</label>
            <textarea
              id="word-content"
              rows="15"
              className="font-mono"
              style={{
                fontSize: '0.84rem',
                lineHeight: 1.65,
                background: '#fcfdff',
                padding: '1.1rem'
              }}
              value={draft.content}
              onChange={(event) => setDraft({ ...draft, content: event.target.value })}
            />
          </div>

          <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
            <button type="button" className="btn-secondary" disabled={busy} onClick={save}>
              <Icon name="save" size={18} />
              <span>Guardar Edición</span>
            </button>
            <button type="button" className="btn-primary" disabled={busy} onClick={download}>
              <Icon name="download" size={18} />
              <span>Descargar Word (.docx)</span>
            </button>
          </div>
        </div>
      )}
    </section>
  );
}
