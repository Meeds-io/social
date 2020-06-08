import ExoSpacesList from './components/ExoSpacesList.vue';
import ExoSpacesCardList from './components/ExoSpacesCardList.vue';
import ExoSpacesToolbar from './components/ExoSpacesToolbar.vue';
import ExoSpaceCard from './components/ExoSpaceCard.vue';
import ExoSpaceCardFront from './components/ExoSpaceCardFront.vue';
import ExoSpaceCardReverse from './components/ExoSpaceCardReverse.vue';
import ExoSpaceManagersDrawer from './components/ExoSpaceManagersDrawer.vue';
import ExoSpaceFormDrawer from './components/ExoSpaceFormDrawer.vue';

const components = {
  'exo-spaces-list': ExoSpacesList,
  'exo-spaces-card-list': ExoSpacesCardList,
  'exo-spaces-toolbar': ExoSpacesToolbar,
  'exo-space-card': ExoSpaceCard,
  'exo-space-card-front': ExoSpaceCardFront,
  'exo-space-card-reverse': ExoSpaceCardReverse,
  'exo-space-managers-drawer': ExoSpaceManagersDrawer,
  'exo-space-form-drawer': ExoSpaceFormDrawer,
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
