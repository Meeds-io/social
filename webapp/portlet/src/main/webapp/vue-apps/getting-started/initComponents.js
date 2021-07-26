import ExoGettingStarted from './components/ExoGettingStarted.vue';

const components = {
  'exo-getting-started': ExoGettingStarted,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
