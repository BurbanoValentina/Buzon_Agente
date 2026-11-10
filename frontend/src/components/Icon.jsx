export function Icon({ name, filled = false, size = 20, className = '', style = {} }) {
  return (
    <span
      className={`material-symbols-outlined ${filled ? 'icon-filled' : ''} ${className}`.trim()}
      aria-hidden="true"
      style={{
        fontSize: typeof size === 'number' ? `${size}px` : size,
        ...style
      }}
    >
      {name}
    </span>
  );
}
