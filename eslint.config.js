import js from '@eslint/js';
import globals from 'globals';

export default [
  { ignores: ['src/generated/**', 'node_modules/**'] },
  js.configs.recommended,
  {
    files: ['**/*.js'],
    languageOptions: {
      globals: globals.node,
    },
  },
];
