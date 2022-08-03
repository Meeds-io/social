import Popover from './components/Popover.vue';
import PopoverMenu from './components/PopoverMenu.vue';
import UserPopoverContent from './components/UserPopoverContent.vue';
import SpacePopoverContent from './components/SpacePopoverContent.vue';
import NotificationAlerts from './components/NotificationAlerts.vue';

const components = {
  'popover': Popover,
  'popover-menu': PopoverMenu,
  'user-popover-content': UserPopoverContent,
  'space-popover-content': SpacePopoverContent,
  'notification-alerts': NotificationAlerts,
};

for (const key in components) {
  Vue.component(key, components[key]);
}