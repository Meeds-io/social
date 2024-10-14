import SpacesOverview from './components/SpacesOverview.vue';
import SpacesOverviewCard from './components/SpacesOverviewCard.vue';
import SpacesOverviewDrawer from './components/SpacesOverviewDrawer.vue';
import SpacesOverviewSpacesList from './components/SpacesOverviewSpacesList.vue';
import SpacesOverviewSpacesListItem from './components/SpacesOverviewSpacesListItem.vue';

const components = {
  'spaces-overview': SpacesOverview,
  'spaces-overview-card': SpacesOverviewCard,
  'spaces-overview-drawer': SpacesOverviewDrawer,
  'spaces-overview-spaces-list': SpacesOverviewSpacesList,
  'spaces-overview-spaces-list-item': SpacesOverviewSpacesListItem,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
