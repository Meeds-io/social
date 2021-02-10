import DlpQuarantineApp from './components/DlpQuarantineApp.vue';
import DlpKeywordsEditor from './components/DlpKeywordsEditor.vue';
import DlpEditPermissions from './components/DlpEditPermissions.vue';

const components = {
  'dlp-quarantine-app': DlpQuarantineApp,
  'dlp-keywords-editor': DlpKeywordsEditor,
  'dlp-edit-permissions': DlpEditPermissions
};

for (const key in components) {
  Vue.component(key, components[key]);
}
