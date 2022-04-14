import ExoTopBarFavorites from './components/ExoTopBarFavorites.vue';
import ExoFavoriteItem from './components/ExoFavoriteItem.vue';
import ActivityFavoriteItem from './components/ActivityFavoriteItem.vue';

const components = {
  'exo-top-bar-favorites': ExoTopBarFavorites,
  'exo-favorite-item': ExoFavoriteItem,
  'activity-favorite-item': ActivityFavoriteItem,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
