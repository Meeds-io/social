import SpacesList from './components/SpacesList.vue';
import SpacesToolbar from './components/SpacesToolbar.vue';
import SpacesCardList from './components/SpacesCardList.vue';
import SpacesListFilterDrawer from './components/SpacesListFilterDrawer.vue';
import SpaceCard from './components/SpaceCard.vue';
import SpaceCardFront from './components/SpaceCardFront.vue';
import SpaceCardReverse from './components/SpaceCardReverse.vue';
import SpaceManagersDrawer from './components/SpaceManagersDrawer.vue';
import SpaceFormDrawer from './components/SpaceFormDrawer.vue';
import SpaceFavoriteAction from './components/SpaceFavoriteAction.vue';

const components = {
  'spaces-list': SpacesList,
  'spaces-toolbar': SpacesToolbar,
  'spaces-card-list': SpacesCardList,
  'spaces-list-filter-drawer': SpacesListFilterDrawer,
  'space-card': SpaceCard,
  'space-card-front': SpaceCardFront,
  'space-card-reverse': SpaceCardReverse,
  'space-managers-drawer': SpaceManagersDrawer,
  'space-form-drawer': SpaceFormDrawer,
  'space-favorite-action': SpaceFavoriteAction,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

//get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpacesList');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}
