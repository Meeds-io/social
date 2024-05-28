import SpaceSettings from './components/SpaceSettings.vue';
import SpaceSettingGeneral from './components/SpaceSettingGeneral.vue';
import SpaceSettingFormDrawer from './components/SpaceSettingFormDrawer.vue';
import SpaceSettingAvatar from './components/SpaceSettingAvatar.vue';

const components = {
  'space-settings': SpaceSettings,
  'space-setting-general': SpaceSettingGeneral,
  'space-setting-avatar': SpaceSettingAvatar,
  'space-setting-form-drawer': SpaceSettingFormDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
