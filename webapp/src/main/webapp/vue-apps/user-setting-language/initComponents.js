import UserSettingLanguage from './components/UserSettingLanguage.vue';
import UserLanguageDrawer from './components/UserLanguageDrawer.vue';

const components = {
  'user-setting-language': UserSettingLanguage,
  'user-language-drawer': UserLanguageDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
