import ActivityReactionsApp from './components/ActivityReactionsApp.vue';
import ActivityReactions from './components/ActivityReactions.vue';
import ActivityReactionsListItems from './components/ActivityReactionsListItems.vue';
import ActivityReactionsDrawer from './components/ActivityReactionsDrawer.vue';
import ActivityReactionsMobile from './components/ActivityReactionsMobile.vue';
import ActivityLikerItem from './components/ActivityLikerItem.vue';
import ActivityReactionsList from './components/ActivityReactionsList.vue';
import ReactionChooser from './components/ReactionChooser.vue';
import ObjectReactions from './components/ObjectReactions.vue';
import ObjectReactionsDrawer from './components/ObjectReactionsDrawer.vue';
const components = {
  'reaction-chooser': ReactionChooser,
  'object-reactions': ObjectReactions,
  'object-reactions-drawer': ObjectReactionsDrawer,
  'activity-reactions-list': ActivityReactionsList,
  'activity-reactions-app': ActivityReactionsApp,
  'activity-reactions': ActivityReactions,
  'activity-reactions-list-items': ActivityReactionsListItems,
  'activity-liker-item': ActivityLikerItem,
  'activity-reactions-drawer': ActivityReactionsDrawer,
  'activity-reactions-mobile': ActivityReactionsMobile
};

for (const key in components) {
  Vue.component(key, components[key]);
}