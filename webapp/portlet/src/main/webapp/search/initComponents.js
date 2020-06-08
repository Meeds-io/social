import SearchApplication from './components/SearchApplication.vue';

const components = {
  'search-application': SearchApplication,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SearchApplication');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}