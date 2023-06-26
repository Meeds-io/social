import TopBarNotification from './components/TopBarNotification.vue';
import NotificationItem from './components/NotificationItem.vue';

const components = {
  'top-bar-notification': TopBarNotification,
  'top-bar-notification-item': NotificationItem,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
