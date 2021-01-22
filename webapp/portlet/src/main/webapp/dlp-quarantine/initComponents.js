import DlpQuarantineApp from './components/DlpQuarantineApp.vue';
import DlpPreviewActions from './components/DlpPreviewActions.vue';

const components = {
  'dlp-quarantine-app': DlpQuarantineApp,
  'dlp-preview-actions': DlpPreviewActions,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
