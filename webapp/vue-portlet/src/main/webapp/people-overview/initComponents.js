import PeopleOverview from './components/PeopleOverview.vue';
import PeopleOverviewCard from './components/PeopleOverviewCard.vue';
import PeopleOverviewDrawer from './components/PeopleOverviewDrawer.vue';
import PeopleOverviewPeopleList from './components/PeopleOverviewPeopleList.vue';
import PeopleOverviewPeopleListItem from './components/PeopleOverviewPeopleListItem.vue';

const components = {
  'people-overview': PeopleOverview,
  'people-overview-card': PeopleOverviewCard,
  'people-overview-drawer': PeopleOverviewDrawer,
  'people-overview-people-list': PeopleOverviewPeopleList,
  'people-overview-people-list-item': PeopleOverviewPeopleListItem,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
