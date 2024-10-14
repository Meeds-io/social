import UserSettingSecurity from './components/UserSettingSecurity.vue';
import UserSettingSecurityWindow from './components/UserSettingSecurityWindow.vue';

const components = {
  'user-setting-security': UserSettingSecurity,
  'user-setting-security-window': UserSettingSecurityWindow,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
