import SearchPeopleCard from './components/SearchPeopleCard.vue';

const components = {
  'people-search-result-card': SearchPeopleCard,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
