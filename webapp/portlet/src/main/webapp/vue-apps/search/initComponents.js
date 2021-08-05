import SearchApplication from './components/SearchApplication.vue';
import SearchToolbar from './components/SearchToolbar.vue';
import SearchResults from './components/SearchResults.vue';
import SearchResultCard from './components/SearchResultCard.vue';

const components = {
  'search-application': SearchApplication,
  'search-toolbar': SearchToolbar,
  'search-results': SearchResults,
  'search-result-card': SearchResultCard,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

// get overrided components if exists
if (extensionRegistry) {
  const overrideComponents = extensionRegistry.loadComponents('SearchApplication');
  if (overrideComponents && overrideComponents.length) {
    overrideComponents.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}