import ActivityStream from './components/ActivityStream.vue';
import ActivityStreamFeatureSwitch from './components/ActivityStreamFeatureSwitch.vue';

const components = {
  'activity-stream': ActivityStream,
  'activity-stream-feature-switch': ActivityStreamFeatureSwitch,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
