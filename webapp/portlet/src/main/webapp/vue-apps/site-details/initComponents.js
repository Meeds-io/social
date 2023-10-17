
import SiteDetails from './components/SiteDetails.vue';
import SiteNavigationsTree from './components/SiteNavigationsTree.vue';
import SiteNavigationItem from './components/SiteNavigationItem.vue';

const components = {
  'site-details': SiteDetails,
  'site-navigations-tree': SiteNavigationsTree,
  'site-navigation-item': SiteNavigationItem,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
