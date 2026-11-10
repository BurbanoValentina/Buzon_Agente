import { describe, expect, it } from 'vitest';
import { renderToStaticMarkup } from 'react-dom/server';
import { Icon } from './Icon';

describe('Google Fonts Material Symbols Icon', () => {
  it('renders Material Symbols Outlined span without raw emojis', () => {
    const html = renderToStaticMarkup(<Icon name="verified_user" filled size={24} />);
    expect(html).toContain('material-symbols-outlined');
    expect(html).toContain('icon-filled');
    expect(html).toContain('font-size:24px');
    expect(html).toContain('aria-hidden="true"');
    expect(html).toContain('>verified_user<');
  });
});
