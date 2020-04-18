import ProfileHeader from './components/ProfileHeader.vue';
import ProfileHeaderActions from './components/ProfileHeaderActions.vue';
import ProfileHeaderAvatar from './components/ProfileHeaderAvatar.vue';
import ProfileHeaderBannerButton from './components/ProfileHeaderBannerButton.vue';

const components = {
  'profile-header': ProfileHeader,
  'profile-header-avatar': ProfileHeaderAvatar,
  'profile-header-banner-button': ProfileHeaderBannerButton,
  'profile-header-actions': ProfileHeaderActions,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
