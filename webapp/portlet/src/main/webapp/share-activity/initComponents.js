import ShareActivity from './components/ShareActivity.vue';

const components = {
  'share-activity': ShareActivity,
};
for (const key in components) {
  Vue.component(key, components[key]);
} 