import SpacesOverview from './components/SpacesOverview.vue';
import SpacesOverviewCard from './components/SpacesOverviewCard.vue';
import SpacesOverviewDrawer from './components/SpacesOverviewDrawer.vue';

const components = {
  'spaces-overview': SpacesOverview,
  'spaces-overview-card': SpacesOverviewCard,
  'spaces-overview-drawer': SpacesOverviewDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
