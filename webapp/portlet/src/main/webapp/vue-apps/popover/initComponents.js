import Popover from './components/Popover.vue';
import PopoverUser from './components/PopoverUser.vue';
import NotificationAlerts from './components/NotificationAlerts.vue';

const components = {
  'popover': Popover,
  'popover-user': PopoverUser,
  'notification-alerts': NotificationAlerts,
};

for (const key in components) {
  Vue.component(key, components[key]);
}