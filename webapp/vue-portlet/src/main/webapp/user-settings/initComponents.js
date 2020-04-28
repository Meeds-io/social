import UserSettings from './components/UserSettings.vue';
import UserSettingLanguage from './components/UserSettingLanguage.vue';
import UserLanguageDrawer from './components/UserLanguageDrawer.vue';
import UserSettingTimezone from './components/UserSettingTimezone.vue';
import UserTimezoneDrawer from './components/UserTimezoneDrawer.vue';
import UserSettingNotifications from './components/UserSettingNotifications.vue';
import UserSettingSecurity from './components/UserSettingSecurity.vue';

const components = {
  'user-settings': UserSettings,
  'user-setting-language': UserSettingLanguage,
  'user-language-drawer': UserLanguageDrawer,
  'user-setting-timezone': UserSettingTimezone,
  'user-timezone-drawer': UserTimezoneDrawer,
  'user-setting-notifications': UserSettingNotifications,
  'user-setting-security': UserSettingSecurity,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
