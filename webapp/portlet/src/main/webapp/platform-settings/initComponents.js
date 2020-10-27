import PlatformSettings from './components/PlatformSettings.vue';
import UsersVisibility from './components/UsersVisibility.vue';
import DocumentSharingPermissions from './components/DocumentSharingPermissions.vue';


const components = {
  'platform-settings': PlatformSettings,
  'users-visibility': UsersVisibility,
  'document-sharing-permissions': DocumentSharingPermissions,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
