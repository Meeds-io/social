import SearchPageCard from './components/SearchPageCard.vue';

const components = {
  'page-search-result-card': SearchPageCard,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
