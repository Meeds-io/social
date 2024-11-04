import SpaceBanner from './components/SpaceBanner.vue';

const components = {
  'space-banner': SpaceBanner,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
