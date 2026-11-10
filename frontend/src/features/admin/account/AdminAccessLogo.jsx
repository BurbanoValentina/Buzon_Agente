export function AdminAccessLogo({ onOpen }) {
  return (
    <button
      type="button"
      className="admin-logo-trigger"
      aria-label="Acceso institucional"
      title="Acceso institucional"
      onClick={onOpen}
    >
      <img src="/mailbox-logo.png" alt="Buzón UCC" />
    </button>
  );
}
