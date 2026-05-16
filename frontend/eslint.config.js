import js from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'
import globals from 'globals'
import vueA11y from 'eslint-plugin-vuejs-accessibility'

export default [
  js.configs.recommended,
  ...pluginVue.configs['flat/essential'],
  ...vueA11y.configs['flat/recommended'],
  {
    languageOptions: {
      globals: globals.browser
    },
    rules: {
      'vue/multi-word-component-names': 'off',
      'no-unused-vars': ['error', { ignoreRestSiblings: true }],
      'vuejs-accessibility/alt-text': 'warn',
      'vuejs-accessibility/anchor-has-content': 'warn',
      'vuejs-accessibility/aria-props': 'warn',
      'vuejs-accessibility/aria-role': 'warn',
      'vuejs-accessibility/aria-unsupported-elements': 'warn',
      'vuejs-accessibility/click-events-have-key-events': 'warn',
      'vuejs-accessibility/form-control-has-label': 'warn',
      'vuejs-accessibility/heading-has-content': 'warn',
      'vuejs-accessibility/interactive-supports-focus': 'warn',
      'vuejs-accessibility/label-has-for': 'warn',
      'vuejs-accessibility/media-has-caption': 'warn',
      'vuejs-accessibility/mouse-events-have-key-events': 'warn',
      'vuejs-accessibility/no-access-key': 'warn',
      'vuejs-accessibility/no-autofocus': 'warn',
      'vuejs-accessibility/no-distracting-elements': 'warn',
      'vuejs-accessibility/no-redundant-roles': 'warn',
      'vuejs-accessibility/role-has-required-aria-props': 'warn',
      'vuejs-accessibility/no-static-element-interactions': 'warn',
      'vuejs-accessibility/tabindex-no-positive': 'warn',
    }
  }
]
