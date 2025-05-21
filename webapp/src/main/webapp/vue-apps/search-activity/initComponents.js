import SearchActivityCard from './components/SearchActivityCard.vue';

const components = {
  'activity-search-result-card': SearchActivityCard,
};


for (const key in components) {
  Vue.component(key, components[key]);
}
