import ExoSpacesList from './components/ExoSpacesList.vue';
import ExoSpacesCardItem from './components/ExoSpacesCardItem.vue';
import ExoSpacesCardFlip from './components/ExoSpacesCardFlip.vue';
import ExoSpacesConfirmDialog from './components/ExoSpacesConfirmDialog.vue';

const components = {
  'exo-spaces-list': ExoSpacesList,
  'exo-spaces-card-item': ExoSpacesCardItem,
  'exo-spaces-card-flip': ExoSpacesCardFlip,
  'exo-spaces-confirm-dialog': ExoSpacesConfirmDialog,
};

for(const key in components) {
  Vue.component(key, components[key]);
}
