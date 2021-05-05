import ShareActivityDrawer from './components/ShareActivityDrawer.vue';
import ShareActivity from './components/ShareActivity.vue';
import ShareActivitySuggester from './components/ShareActivitySuggester.vue';
import ShareActivitySpaceItem from './components/ShareActivitySpaceItem.vue';

const components = {
  'share-activity-drawer': ShareActivityDrawer,
  'share-activity': ShareActivity,
  'share-activity-suggester': ShareActivitySuggester,
  'share-activity-space-item': ShareActivitySpaceItem,
};
for (const key in components) {
  Vue.component(key, components[key]);
} 