import UsersManagement from './components/UsersManagement.vue';
import UsersManagementList from './components/UsersManagementList.vue';
import UsersManagementToolbar from './components/UsersManagementToolbar.vue';
import UsersManagementUserFormDrawer from './components/UsersManagementUserFormDrawer.vue';
import UsersManagementUserMembershipDrawer from './components/UsersManagementUserMembershipDrawer.vue';
import UsersManagementImportCSVButton from './components/UsersManagementImportCSVButton.vue';
import UsersManagementImportCSVErrorMessage from './components/UsersManagementImportCSVErrorMessage.vue';
import UsersManagementImportCSVResult from './components/UsersManagementImportCSVResult.vue';
import UsersManagementFilterDrawer from './components/UsersManagementFilterDrawer.vue';

const components = {
  'users-management': UsersManagement,
  'users-management-list': UsersManagementList,
  'users-management-toolbar': UsersManagementToolbar,
  'users-management-user-form-drawer': UsersManagementUserFormDrawer,
  'users-management-user-membership-drawer': UsersManagementUserMembershipDrawer,
  'users-management-import-csv-button': UsersManagementImportCSVButton,
  'users-management-import-csv-result': UsersManagementImportCSVResult,
  'users-management-import-csv-error-message': UsersManagementImportCSVErrorMessage,
  'users-management-filter-drawer': UsersManagementFilterDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
