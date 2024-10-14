import TopBarFavorites from './components/TopBarFavorites.vue';
import FavoriteItem from './components/FavoriteItem.vue';
import TopBarFavoritesDrawer from './components/TopBarFavoritesDrawer.vue';
import TopBarFavoritesButton from './components/TopBarFavoritesButton.vue';
import ActivityFavoriteItem from './components/ActivityFavoriteItem.vue';
import SpaceFavoriteItem from './components/SpaceFavoriteItem.vue';

const components = {
  'top-bar-favorites': TopBarFavorites,
  'top-bar-favorites-button': TopBarFavoritesButton,
  'top-bar-favorites-drawer': TopBarFavoritesDrawer,
  'favorite-item': FavoriteItem,
  'activity-favorite-item': ActivityFavoriteItem,
  'space-favorite-item': SpaceFavoriteItem,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
