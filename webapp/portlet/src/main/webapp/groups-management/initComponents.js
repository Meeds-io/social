import GroupsManagement from './components/GroupsManagement.vue';

const components = {
  'groups-management': GroupsManagement,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
