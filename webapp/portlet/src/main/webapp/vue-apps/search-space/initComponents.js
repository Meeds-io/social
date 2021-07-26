import SearchSpaceCard from './components/SearchSpaceCard.vue';
import SearchSpaceDrawers from './components/SearchSpaceDrawers.vue';

const components = {
  'space-search-result-card': SearchSpaceCard,
  'space-search-drawers': SearchSpaceDrawers,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SearchSpaceCard');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}