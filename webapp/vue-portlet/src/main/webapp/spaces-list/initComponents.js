import ExoSpacesList from './components/ExoSpacesList.vue';
import ExoSpacesCardItem from './components/ExoSpacesCardItem.vue';
import ExoSpacesCardFlip from './components/ExoSpacesCardFlip.vue';
import ExoConfirmDialog from './components/ExoConfirmDialog.vue';
import ExoSpacesCardItemFlipped from './components/ExoSpacesCardItemFlipped.vue';
import ExoUserAvatarsList from './components/ExoUserAvatarsList.vue';
import ExoUserAvatar from './components/ExoUserAvatar.vue';
import ExoDrawer from './components/ExoDrawer.vue';
import ExoSpaceManagersDrawer from './components/ExoSpaceManagersDrawer.vue';

const components = {
  'exo-user-avatars-list': ExoUserAvatarsList,
  'exo-user-avatar': ExoUserAvatar,
  'exo-drawer': ExoDrawer,
  'exo-space-managers-drawer': ExoSpaceManagersDrawer,
  'exo-confirm-dialog': ExoConfirmDialog,
  'exo-spaces-list': ExoSpacesList,
  'exo-spaces-card-item': ExoSpacesCardItem,
  'exo-spaces-card-item-flipped': ExoSpacesCardItemFlipped,
  'exo-spaces-card-flip': ExoSpacesCardFlip,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
