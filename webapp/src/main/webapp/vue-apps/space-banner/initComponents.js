import SpaceHeader from './components/SpaceHeader.vue';

const components = {
  'space-header': SpaceHeader,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
