import UserSettingDateTime from './components/UserSettingDateTime.vue';
import UserDateTimeDrawer from './components/UserDateTimeDrawer.vue';
import UserSettingsDateTimeInfo from './components/UserSettingsDateTimeInfo.vue';
import UserSettingDateTab from './components/UserSettingDateTab.vue';
import UserSettingTimeTab from './components/UserSettingTimeTab.vue';
import UserSettingTimeZoneTab from './components/UserSettingTimeZoneTab.vue';

const components = {
  'user-setting-date-time': UserSettingDateTime,
  'user-date-time-drawer': UserDateTimeDrawer,
  'user-setting-date-time-info': UserSettingsDateTimeInfo,
  'user-setting-date-tab': UserSettingDateTab,
  'user-setting-time-tab': UserSettingTimeTab,
  'user-setting-time-zone-tab': UserSettingTimeZoneTab,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
