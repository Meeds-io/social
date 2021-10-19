import ActivityReactionsApp from './components/ActivityReactionsApp.vue';
import ActivityReactions from './components/ActivityReactions.vue';
import ActivityReactionsListItems from './components/ActivityReactionsListItems.vue';
import ActivityReactionsDrawer from './components/ActivityReactionsDrawer.vue';
import ActivityReactionsMobile from './components/ActivityReactionsMobile.vue';
import ActivityLockReactions from './components/ActivityLockReactions.vue';
import ActivityLockReactionsMobile from './components/ActivityLockReactionsMobile.vue';

const components = {
  'activity-reactions-app': ActivityReactionsApp,
  'activity-reactions': ActivityReactions,
  'activity-reactions-list-items': ActivityReactionsListItems,
  'activity-reactions-drawer': ActivityReactionsDrawer,
  'activity-reactions-mobile': ActivityReactionsMobile,
  'activity-lock-reactions': ActivityLockReactions,
  'activity-lock-reactions-mobile': ActivityLockReactionsMobile,
};

for (const key in components) {
  Vue.component(key, components[key]);
}