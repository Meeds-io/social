import ExoSpacesList from './components/ExoSpacesList.vue';
import ExoSpaceCard from './components/ExoSpaceCard.vue';
import ExoSpaceCardFront from './components/ExoSpaceCardFront.vue';
import ExoSpaceCardReverse from './components/ExoSpaceCardReverse.vue';
import ExoSpaceManagersDrawer from './components/ExoSpaceManagersDrawer.vue';

const components = {
  'exo-spaces-list': ExoSpacesList,
  'exo-space-card': ExoSpaceCard,
  'exo-space-card-front': ExoSpaceCardFront,
  'exo-space-card-reverse': ExoSpaceCardReverse,
  'exo-space-managers-drawer': ExoSpaceManagersDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
