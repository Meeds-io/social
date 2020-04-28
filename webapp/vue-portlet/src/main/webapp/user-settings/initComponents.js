import UserSettings from './components/UserSettings.vue';
import UserSettingLanguage from './components/UserSettingLanguage.vue';
import UserSettingTimezone from './components/UserSettingTimezone.vue';
import UserSettingNotifications from './components/UserSettingNotifications.vue';
import UserSettingSecurity from './components/UserSettingSecurity.vue';
import UserSettingSocialNetwork from './components/UserSettingSocialNetwork.vue';

const components = {
  'user-settings': UserSettings,
  'user-setting-language': UserSettingLanguage,
  'user-setting-timezone': UserSettingTimezone,
  'user-setting-notifications': UserSettingNotifications,
  'user-setting-security': UserSettingSecurity,
  'user-setting-social-network': UserSettingSocialNetwork,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
