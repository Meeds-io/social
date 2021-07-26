import ExternalSpacesList from './components/ExternalSpacesList.vue';
import ExternalSpaceItem from './components/ExternalSpaceItem.vue';
import ExternalSpacesRequestsItems from './components/ExternalSpacesRequestsItems.vue';

const components = {
  'external-spaces-list': ExternalSpacesList,
  'external-space-item': ExternalSpaceItem,
  'external-spaces-requests-items': ExternalSpacesRequestsItems
};

for (const key in components) {
  Vue.component(key, components[key]);
}
