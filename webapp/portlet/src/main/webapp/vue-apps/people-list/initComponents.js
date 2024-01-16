import PeopleList from './components/PeopleList.vue';
import PeopleCardList from './components/PeopleCardList.vue';
import PeopleToolbar from './components/PeopleToolbar.vue';
import PeopleCard from './components/PeopleCard.vue';
import PeopleCardFront from './components/PeopleCardFront.vue';
import PeopleCardReverse from './components/PeopleCardReverse.vue';
import PeopleAdvancedFilterDrawer from './components/PeopleAdvancedFilterDrawer.vue';
import AdvancedFilterInputItem from './components/AdvancedFilterInputItem.vue';
import PeopleConnectionOptionItem from './components/PeopleConnectionOptionItem.vue';
import PeopleCardFrontCompact from './components/PeopleCardFrontCompact.vue';

const components = {
  'people-list': PeopleList,
  'people-card-list': PeopleCardList,
  'people-toolbar': PeopleToolbar,
  'people-card': PeopleCard,
  'people-card-front': PeopleCardFront,
  'people-card-reverse': PeopleCardReverse,
  'people-advanced-filter-drawer': PeopleAdvancedFilterDrawer,
  'people-advanced-filter-input-item': AdvancedFilterInputItem,
  'people-connection-option-item': PeopleConnectionOptionItem,
  'people-card-front-compact': PeopleCardFrontCompact,
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
