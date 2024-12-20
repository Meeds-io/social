import SearchSpaceCard from './components/SearchSpaceCard.vue';

const components = {
  'space-search-result-card': SearchSpaceCard,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
