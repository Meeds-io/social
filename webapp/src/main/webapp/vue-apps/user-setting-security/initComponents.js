import UserSettingSecurity from './components/UserSettingSecurity.vue';
import UserSettingSecurityPasswordDrawer from './components/UserSettingSecurityPasswordDrawer.vue';

const components = {
  'user-setting-security': UserSettingSecurity,
  'user-setting-security-password-drawer': UserSettingSecurityPasswordDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
