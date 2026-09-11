import UserSettingNotifications from './components/UserSettingNotifications.vue';
import UserSettingNotificationsWindow from './components/UserSettingNotificationsWindow.vue';
import UserSettingNotificationChannel from './components/UserSettingNotificationChannel.vue';
import UserSettingNotificationGroup from './components/UserSettingNotificationGroup.vue';
import UserSettingNotificationPlugin from './components/UserSettingNotificationPlugin.vue';
import UserSettingNotificationDrawer from './components/UserSettingNotificationDrawer.vue';
import UserSettingNotificationMuteSpacesDrawer from './components/UserSettingNotificationMuteSpacesDrawer.vue';
import UserSettingDigestEntry from './components/UserSettingDigestEntry.vue';
import UserSettingDigestDrawer from './components/UserSettingDigestDrawer.vue';
import UserSettingDigestCategories from './components/UserSettingDigestCategories.vue';

const components = {
  'user-setting-notifications': UserSettingNotifications,
  'user-setting-notifications-window': UserSettingNotificationsWindow,
  'user-setting-notification-channel': UserSettingNotificationChannel,
  'user-setting-notification-group': UserSettingNotificationGroup,
  'user-setting-notification-plugin': UserSettingNotificationPlugin,
  'user-setting-notification-drawer': UserSettingNotificationDrawer,
  'user-setting-notification-mute-spaces-drawer': UserSettingNotificationMuteSpacesDrawer,
  'user-setting-digest-entry': UserSettingDigestEntry,
  'user-setting-digest-drawer': UserSettingDigestDrawer,
  'user-setting-digest-categories': UserSettingDigestCategories,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
