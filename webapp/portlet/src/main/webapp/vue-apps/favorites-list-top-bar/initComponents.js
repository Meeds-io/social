import ExoTopBarFavorites from './components/ExoTopBarFavorites.vue';

const components = {
  'exo-top-bar-favorites': ExoTopBarFavorites,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
