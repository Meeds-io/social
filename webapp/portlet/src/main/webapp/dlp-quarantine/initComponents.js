import DlpQuarantineApp from './components/DlpQuarantineApp.vue';
import DlpAuthorFullName from './components/DlpAuthorFullName.vue';

const components = {
  'dlp-quarantine-app': DlpQuarantineApp,
  'dlp-author-full-name': DlpAuthorFullName,
};

for (const key in components) {
  Vue.component(key, components[key]);
}