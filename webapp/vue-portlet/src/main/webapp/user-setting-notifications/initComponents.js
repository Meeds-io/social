import UserSettingNotifications from './components/UserSettingNotifications.vue';

const components = {
  'user-setting-notifications': UserSettingNotifications,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
