import PeopleList from './components/PeopleList.vue';
import PeopleCardList from './components/PeopleCardList.vue';
import PeopleToolbar from './components/PeopleToolbar.vue';
import PeopleAdvancedFilterDrawer from './components/PeopleAdvancedFilterDrawer.vue';
import AdvancedFilterInputItem from './components/AdvancedFilterInputItem.vue';
import PeopleCard from './components/usercard/PeopleCard.vue';
import PeopleUserCompactCard from './components/usercard/PeopleUserCompactCard.vue';
import PeopleUserCard from './components/usercard/PeopleUserCard.vue';
import PeopleUserRole from './components/usercard/PeopleUserRole.vue';

const components = {
  'people-list': PeopleList,
  'people-card-list': PeopleCardList,
  'people-toolbar': PeopleToolbar,
  'people-card': PeopleCard,
  'people-advanced-filter-drawer': PeopleAdvancedFilterDrawer,
  'people-advanced-filter-input-item': AdvancedFilterInputItem,
  'people-user-compact-card': PeopleUserCompactCard,
  'people-user-card': PeopleUserCard,
  'people-user-role': PeopleUserRole,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
//get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('peopleList');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}
