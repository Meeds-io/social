import MembershipsManagement from './components/MembershipsManagement.vue';

const components = {
  'memberships-management': MembershipsManagement,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
