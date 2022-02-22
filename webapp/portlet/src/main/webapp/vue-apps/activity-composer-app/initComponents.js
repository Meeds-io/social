import ActivityComposer  from './components/ActivityComposer.vue';
import ActivityComposerDrawer  from './components/ActivityComposerDrawer.vue';

const components = {
  'activity-composer': ActivityComposer,
  'activity-composer-drawer': ActivityComposerDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}