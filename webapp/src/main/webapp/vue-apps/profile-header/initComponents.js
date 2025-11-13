import ProfileHeader from './components/ProfileHeader.vue';
import ProfileHeaderText from './components/ProfileHeaderText.vue';
import ProfileHeaderActions from './components/ProfileHeaderActions.vue';
import ProfileHeaderAvatar from './components/ProfileHeaderAvatar.vue';
import ProfileHeaderBannerButton from './components/ProfileHeaderBannerButton.vue';
import ProfileHeaderSettingsDrawer from './components/settings/ProfileHeaderSettingsDrawer.vue';
import ProfileHeaderRelationButton from './components/ProfileHeaderRelationButton.vue';

import * as profileHeaderService from './profileHeaderService.js';

const components = {
  'profile-header': ProfileHeader,
  'profile-header-avatar': ProfileHeaderAvatar,
  'profile-header-banner-button': ProfileHeaderBannerButton,
  'profile-header-actions': ProfileHeaderActions,
  'profile-header-text': ProfileHeaderText,
  'profile-header-settings-drawer': ProfileHeaderSettingsDrawer,
  'profile-header-relation-button': ProfileHeaderRelationButton
};

if (!Vue.prototype.$profileHeaderService) {
  window.Object.defineProperty(Vue.prototype, '$profileHeaderService', {
    value: profileHeaderService,
  });
}

for (const key in components) {
  Vue.component(key, components[key]);
}
