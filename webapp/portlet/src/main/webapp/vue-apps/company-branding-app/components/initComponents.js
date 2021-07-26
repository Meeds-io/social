import ExoColorPicker from './modal/ExoColorPicker.vue';
import ExoCompanyBranding from './ExoCompanyBranding.vue';

const components = {
  'exo-company-branding': ExoCompanyBranding,
  'exo-company-branding-color-picker': ExoColorPicker,
};

for (const key in components) {
  Vue.component(key, components[key]);
}