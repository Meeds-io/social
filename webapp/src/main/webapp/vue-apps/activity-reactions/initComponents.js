import ActivityReactionsApp from './components/ActivityReactionsApp.vue';
import ActivityReactions from './components/ActivityReactions.vue';
import ActivityReactionsListItems from './components/ActivityReactionsListItems.vue';
import ActivityReactionsDrawer from './components/ActivityReactionsDrawer.vue';
import ActivityReactionsMobile from './components/ActivityReactionsMobile.vue';
import ActivityLikesList from './components/ActivityLikesList.vue';
import ActivityLikerItem from './components/ActivityLikerItem.vue';
const components = {
  'activity-reactions-app': ActivityReactionsApp,
  'activity-reactions': ActivityReactions,
  'activity-reactions-list-items': ActivityReactionsListItems,
  'activity-likes-list': ActivityLikesList,
  'activity-liker-item': ActivityLikerItem,
  'activity-reactions-drawer': ActivityReactionsDrawer,
  'activity-reactions-mobile': ActivityReactionsMobile
};

for (const key in components) {
  Vue.component(key, components[key]);
}