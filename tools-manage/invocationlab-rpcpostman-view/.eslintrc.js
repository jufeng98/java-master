// http://eslint.org/docs/user-guide/configuring

module.exports = {
  root: true,
  parserOptions: {
    parser: 'babel-eslint',
    sourceType: 'module',
    ecmaVersion: '2017'
  },
  env: {
    browser: true,
  },
  extends: [
    'eslint:recommended',
    'plugin:vue/strongly-recommended'
  ],
  rules: {
    'quotes': ['error', 'single'],
    'no-console': ['error', {allow: ['warn']}],
    'vue/max-attributes-per-line': 'off'
  },
  // required to lint *.vue files
  plugins: ['vue']
};
