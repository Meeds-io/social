import UserSettingTimezone from './components/UserSettingTimezone.vue';
import UserTimezoneDrawer from './components/UserTimezoneDrawer.vue';

const components = {
  'user-setting-timezone': UserSettingTimezone,
  'user-timezone-drawer': UserTimezoneDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
