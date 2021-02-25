import DlpQuarantineApp from './components/DlpQuarantineApp.vue';
import DlpKeywordsEditor from './components/DlpKeywordsEditor.vue';

const components = {
  'dlp-quarantine-app': DlpQuarantineApp,
  'dlp-keywords-editor': DlpKeywordsEditor
};

for (const key in components) {
  Vue.component(key, components[key]);
}
