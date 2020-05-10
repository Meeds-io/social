import SpaceSettings from './components/SpaceSettings.vue';
import SpaceSettingsGeneral from './components/SpaceSettingsGeneral.vue';
import SpaceSettingsFormDrawer from './components/SpaceSettingsFormDrawer.vue';
import SpaceSettingsAvatar from './components/SpaceSettingsAvatar.vue';

const components = {
  'space-settings': SpaceSettings,
  'space-settings-general': SpaceSettingsGeneral,
  'space-settings-avatar': SpaceSettingsAvatar,
  'space-settings-form-drawer': SpaceSettingsFormDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
