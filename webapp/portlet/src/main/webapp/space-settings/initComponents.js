import SpaceSettings from './components/SpaceSettings.vue';
import SpaceSettingGeneral from './components/SpaceSettingGeneral.vue';
import SpaceSettingFormDrawer from './components/SpaceSettingFormDrawer.vue';
import SpaceSettingAvatar from './components/SpaceSettingAvatar.vue';
import SpaceSettingApplications from './components/SpaceSettingApplications.vue';
import SpaceSettingApplicationsWindow from './components/SpaceSettingApplicationsWindow.vue';
import SpaceSettingApplicationCard from './components/SpaceSettingApplicationCard.vue';
import SpaceSettingAddApplicationDrawer from './components/SpaceSettingAddApplicationDrawer.vue';

const components = {
  'space-settings': SpaceSettings,
  'space-setting-general': SpaceSettingGeneral,
  'space-setting-avatar': SpaceSettingAvatar,
  'space-setting-form-drawer': SpaceSettingFormDrawer,
  'space-setting-applications': SpaceSettingApplications,
  'space-setting-applications-window': SpaceSettingApplicationsWindow,
  'space-setting-application-card': SpaceSettingApplicationCard,
  'space-setting-add-application-drawer': SpaceSettingAddApplicationDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
