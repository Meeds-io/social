
import SiteDetails from './components/SiteDetails.vue';
import SiteNavigationTree from './components/SiteNavigationTree.vue';
import SiteNavigationItem from './components/SiteNavigationItem.vue';

const components = {
  'site-details': SiteDetails,
  'site-navigation-tree': SiteNavigationTree,
  'site-navigation-item': SiteNavigationItem,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
