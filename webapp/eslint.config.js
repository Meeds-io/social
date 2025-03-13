import globals from "globals";
import pluginJs from "@eslint/js";
import pluginVue from "eslint-plugin-vue";
import vuetify from "eslint-config-vuetify";
import pluginVueA11y from "eslint-plugin-vuejs-accessibility";
import { globalIgnores } from "eslint/config";

const a11yConfig = pluginVueA11y.configs["flat/recommended"];
a11yConfig.forEach((x) => {
  delete x.languageOptions.globals;
});

export default [
  pluginJs.configs.recommended,
  ...pluginVue.configs["flat/recommended"],
  ...vuetify,
  ...a11yConfig,
  globalIgnores(["**/*.json"]),
  {files: ["**/*.{js,mjs,cjs,vue}"]},
  {languageOptions: { globals: globals.browser }},
  {
    "rules": {
      "vue/multi-word-component-names": "off",
      "vuejs-accessibility/no-autofocus": "off",
      "no-prototype-builtins": ["warn"],
      "vue/no-mutating-props": ["warn"],
      "no-irregular-whitespace": "error",
      "no-unexpected-multiline": "error",
      "complexity": ["warn", 32],
      "comma-dangle": "warn",
      "no-tabs": "error",
      "key-spacing": "error",
      "keyword-spacing": "error",
      "block-scoped-var": ["error"],
      "curly": ["error", "all"],
      "eqeqeq": ["error", "always", { "null": "ignore" }],
      "new-cap": ["error"],
      "new-parens": ["error"],
      "no-alert": ["error"],
      "no-caller": ["error"],
      "no-confusing-arrow": ["error"],
      "no-console": ["warn", {"allow": ["error"]}],
      "no-empty-function": ["error"],
      "no-eval": ["error"],
      "no-extra-label": ["error"],
      "no-extend-native": ["error"],
      "no-floating-decimal": ["error"],
      "no-implied-eval": ["error"],
      "no-iterator": ["error"],
      "no-labels": ["error"],
      "no-label-var": ["error"],
      "no-lone-blocks": ["error"],
      "no-loop-func": ["error"],
      "no-new-require": ["error"],
      "no-path-concat": ["error"],
      "no-process-env": ["error"],
      "no-process-exit": ["error"],
      "no-proto": ["error"],
      "no-restricted-imports": ["error", "axios"],
      "no-self-compare": ["error"],
      "no-sequences": ["error"],
      "no-shadow-restricted-names": ["error"],
      "no-sync": ["error"],
      "no-template-curly-in-string": ["error"],
      "no-throw-literal": ["error"],
      "no-undefined": ["error"],
      "no-unmodified-loop-condition": ["error"],
      "no-unused-expressions": ["error"],
      "no-useless-computed-key": ["error"],
      "no-useless-concat": ["error"],
      "no-var": ["error"],
      "no-with": ["error"],
      "prefer-const": ["error"],
      "prefer-promise-reject-errors": ["error"],
      "prefer-rest-params": ["error"],
      "prefer-spread": ["error"],
      "prefer-template": ["error"],
      "quotes": ["error", "single"],
      "vue/require-explicit-emits": ["warn", {
        "allowProps": false
      }],
      "no-unused-vars": ["error", {
          "vars": "all",
          "args": "after-used",
          "caughtErrors": "none",
          "ignoreRestSiblings": false,
          "reportUsedIgnorePattern": false
      }],
      "vue/custom-event-name-casing": ["warn", "kebab-case", {
        "ignores": []
      }],
      "require-await": ["error"],
      "semi": ["error", "always"],
      "space-before-function-paren": "off",
      "yoda": ["error", "never", { "exceptRange": true }],
      "vue/max-attributes-per-line": ["warn", {
        "singleline": {
          "max": 2
        },
        "multiline": {
          "max": 1
        }
      }],
      "vue/singleline-html-element-content-newline": ["off"],
      "vue/html-self-closing": ["warn", {
        "html": {
          "void": "never",
          "normal": "never",
          "component": "always"
        },
        "svg": "never",
        "math": "never"
      }],
      "vue/html-closing-bracket-newline": ["warn", {
        "singleline": "never",
        "multiline": "never"
      }],
      "vue/component-name-in-template-casing": ["error", "kebab-case"],
      "vuejs-accessibility/alt-text": "warn",
      "vuejs-accessibility/anchor-has-content": "warn",
      "vuejs-accessibility/aria-props": "warn",
      "vuejs-accessibility/aria-role": "warn",
      "vuejs-accessibility/aria-unsupported-elements": "warn",
      "vuejs-accessibility/click-events-have-key-events": "warn",
      "vuejs-accessibility/form-control-has-label": "warn",
      "vuejs-accessibility/heading-has-content": "warn",
      "vuejs-accessibility/iframe-has-title": "warn",
      "vuejs-accessibility/interactive-supports-focus": "warn",
      "vuejs-accessibility/label-has-for": "warn",
      "vuejs-accessibility/media-has-caption": "warn",
      "vuejs-accessibility/mouse-events-have-key-events": "warn",
      "vuejs-accessibility/no-access-key": "warn",
      "vuejs-accessibility/no-autofocus": "warn",
      "vuejs-accessibility/no-distracting-elements": "warn",
      "vuejs-accessibility/no-onchange": "warn",
      "vuejs-accessibility/no-redundant-roles": "warn",
      "vuejs-accessibility/no-static-element-interactions": "warn",
      "vuejs-accessibility/role-has-required-aria-props": "warn",
      "vuejs-accessibility/tabindex-no-positive": "warn"
    },
    "languageOptions": {
      "ecmaVersion": 2022,
      "sourceType": "module",
      "globals": {
        "Vue": true,
        "VueI18n": true,
        "VueEllipsis": true,
        "Vuetify": true,
        "eXo": true,
        "$": true,
        "global": true,
        "exoi18n": true,
        "extensionRegistry": true,
        "CKEDITOR": true,
        "cCometd": true,
        "activityComposer": true,
        "socialUIProfile": true,
        "QRCode": true,
        "DOMPurify": true,
        "ExtendedDomPurify": true,
        "Cropper": true,
        "Autolinker": true,
        "cCometd": true,
        "jspdf": true,
        "html2canvas": true
      }
    }
  }
];
