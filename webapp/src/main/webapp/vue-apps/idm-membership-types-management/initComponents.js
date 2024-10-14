import MembershipTypesManagement from './components/MembershipTypesManagement.vue';
import MembershipTypesManagementList from './components/MembershipTypesManagementList.vue';
import MembershipTypesManagementToolbar from './components/MembershipTypesManagementToolbar.vue';
import MembershipTypesManagementFormDrawer from './components/MembershipTypesManagementFormDrawer.vue';

const components = {
  'membership-types-management': MembershipTypesManagement,
  'membership-types-management-toolbar': MembershipTypesManagementToolbar,
  'membership-types-management-list': MembershipTypesManagementList,
  'membership-types-management-form-drawer': MembershipTypesManagementFormDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
