import js from '@eslint/js';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';

export default [
  { ignores: ['node_modules/', 'dist/', 'build/', 'coverage/', '*.min.js'] },
  js.configs.recommended,
  {
    files: ['**/*.{js,jsx}'],
    plugins: { 'react-hooks': reactHooks, 'react-refresh': reactRefresh },
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module',
      parserOptions: { ecmaFeatures: { jsx: true } },
      globals: {
        window: 'readonly', document: 'readonly', fetch: 'readonly',
        FormData: 'readonly', Blob: 'readonly', URL: 'readonly',
        URLSearchParams: 'readonly', console: 'readonly'
      }
    },
    rules: { 'no-console': 'warn', 'react-refresh/only-export-components': 'warn' }
  },
  {
    files: ['**/*.jsx'],
    rules: { 'no-unused-vars': 'off' }
  }
];
