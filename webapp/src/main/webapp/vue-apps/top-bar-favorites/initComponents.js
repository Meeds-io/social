import TopBarFavorites from './components/TopBarFavorites.vue';
import FavoriteItem from './components/FavoriteItem.vue';
import TopBarFavoritesDrawer from './components/TopBarFavoritesDrawer.vue';
import TopBarFavoritesButton from './components/TopBarFavoritesButton.vue';
import ActivityFavoriteItem from './components/ActivityFavoriteItem.vue';
import SpaceFavoriteItem from './components/SpaceFavoriteItem.vue';
import FavoriteTypes from './components/FavoriteTypes.vue';
import FavoriteType from './components/FavoriteType.vue';
import FavoritePlaceholder from './components/FavoritePlaceholder.vue';
import FavoriteSpaceAvatar from './components/FavoriteSpaceAvatar.vue';
import FavoriteUserAvatar from './components/FavoriteUserAvatar.vue';

const components = {
  'top-bar-favorites': TopBarFavorites,
  'top-bar-favorites-button': TopBarFavoritesButton,
  'top-bar-favorites-drawer': TopBarFavoritesDrawer,
  'favorite-item': FavoriteItem,
  'favorite-types': FavoriteTypes,
  'favorite-type': FavoriteType,
  'favorite-placeholder': FavoritePlaceholder,
  'activity-favorite-item': ActivityFavoriteItem,
  'space-favorite-item': SpaceFavoriteItem,
  'favorite-space-avatar': FavoriteSpaceAvatar,
  'favorite-user-avatar': FavoriteUserAvatar,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
