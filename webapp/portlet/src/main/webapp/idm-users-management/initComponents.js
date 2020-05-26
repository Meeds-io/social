import UsersManagement from './components/UsersManagement.vue';
import UsersManagementList from './components/UsersManagementList.vue';
import UsersManagementToolbar from './components/UsersManagementToolbar.vue';
import UsersManagementUserFormDrawer from './components/UsersManagementUserFormDrawer.vue';

const components = {
  'users-management': UsersManagement,
  'users-management-list': UsersManagementList,
  'users-management-toolbar': UsersManagementToolbar,
  'users-management-user-form-drawer': UsersManagementUserFormDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
