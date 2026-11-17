import UserSettingLanguage from './components/UserSettingLanguage.vue';
import UserLanguageDrawer from './components/UserLanguageDrawer.vue';
import UserSettingDisplayMode from './components/UserSettingDisplayMode.vue';

const components = {
  'user-setting-language': UserSettingLanguage,
  'user-language-drawer': UserLanguageDrawer,
  'user-setting-display-mode': UserSettingDisplayMode,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
