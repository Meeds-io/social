import Popover from './components/Popover.vue';
import PopoverMenu from './components/PopoverMenu.vue';
import UserPopoverContent from './components/UserPopoverContent.vue';
import SpacePopoverContent from './components/SpacePopoverContent.vue';
import SpaceMuteNotificationButton from './components/SpaceMuteNotificationButton.vue';
import UsersListDrawer from './components/UsersListDrawer.vue';
import ExoSpaceFavoriteAction from '../spaces-list/components/ExoSpaceFavoriteAction.vue';

const components = {
  'popover': Popover,
  'popover-menu': PopoverMenu,
  'user-popover-content': UserPopoverContent,
  'space-popover-content': SpacePopoverContent,
  'space-mute-notification-button': SpaceMuteNotificationButton,
  'users-list-drawer': UsersListDrawer
};
components['exo-space-favorite-action'] = ExoSpaceFavoriteAction;

for (const key in components) {
  Vue.component(key, components[key]);
}