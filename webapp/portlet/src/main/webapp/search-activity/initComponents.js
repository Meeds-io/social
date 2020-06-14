import SearchActivityCard from './components/SearchActivityCard.vue';

const components = {
  'activity-search-result-card': SearchActivityCard,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SearchActivityCard');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}