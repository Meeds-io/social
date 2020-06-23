import ActivityReactionsApp from './components/ActivityReactionsApp.vue';
import ActivityReactionsListItems from './components/ActivityReactionsListItems.vue';
import ActivityReactionsDrawer from './components/ActivityReactionsDrawer.vue';
const components = {
  'activity-reactions': ActivityReactionsApp,
  'activity-reactions-list-items': ActivityReactionsListItems,
  'activity-reactions-drawer': ActivityReactionsDrawer
};

for (const key in components) {
  Vue.component(key, components[key]);
}