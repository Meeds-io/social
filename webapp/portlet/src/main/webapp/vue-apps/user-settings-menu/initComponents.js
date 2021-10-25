import UserSettingsMenu from './components/UserSettingsMenu.vue';

const components = {
  'user-settings-menu': UserSettingsMenu,
};

for (const key in components) {
  Vue.component(key, components[key]);
}