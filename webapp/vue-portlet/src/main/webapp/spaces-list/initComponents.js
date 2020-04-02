import ExoSpacesList from './components/ExoSpacesList.vue';
import ExoSpacesListItem from './components/ExoSpacesListItem.vue';

const components = {
  'exo-spaces-list': ExoSpacesList,
  'exo-spaces-list-item': ExoSpacesListItem,
};

for(const key in components) {
  Vue.component(key, components[key]);
}
