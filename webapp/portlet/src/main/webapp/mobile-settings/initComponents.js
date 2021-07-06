import MobileSettings from './components/MobileSettings.vue';
import MobileSettingsWindow from './components/MobileSettingsWindow.vue';


const components = {
  'mobile-settings': MobileSettings,
  'mobile-settings-window': MobileSettingsWindow,
};

for (const key in components) {
  Vue.component(key, components[key]);
}