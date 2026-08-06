import GroupManagement from './components/GroupManagement.vue';
import GroupManagementTree from './components/sidebar/GroupManagementTree.vue';
import GroupManagementTreeItem from './components/sidebar/GroupManagementTreeItem.vue';
import GroupManagementTreeGroupMenu from './components/sidebar/GroupManagementTreeGroupMenu.vue';
import GroupManagementTreeToolbar from './components/sidebar/GroupManagementTreeToolbar.vue';
import GroupManagementFormDrawer from './components/drawer/GroupManagementFormDrawer.vue';
import GroupManagementMembershipFormDrawer from './components/drawer/GroupManagementMembershipFormDrawer.vue';
import GroupManagementToolbar from './components/header/GroupManagementToolbar.vue';
import GroupManagementPlaceholder from './components/GroupManagementPlaceholder.vue';
import GroupMembersManagement from './components/members/GroupMembersManagement.vue';
import GroupMembersList from './components/members/GroupMembersList.vue';
import UsersManagementFilterDrawer from '../idm-users-management/components/UsersManagementFilterDrawer.vue';
import UsersManagementExportCSVResult from '../idm-users-management/components/UsersManagementExportCSVResult.vue';
import GroupMembersMembershipDrawer from './components/members/GroupMembersMembershipDrawer.vue';
import GroupMembersManagementToolbar from './components/members/GroupMembersManagementToolbar.vue';
import NestedGroupsManagement from './components/NestedGroups/NestedGroupsManagement.vue';
import NestedGroupsList from './components/NestedGroups/NestedGroupsList.vue';
import NestedGroupsToolbar from './components/NestedGroups/NestedGroupsToolbar.vue';
import GroupMembersCount from './components/members/GroupMembersCount.vue';
import GroupManagementSettingsDrawer from './components/drawer/GroupManagementSettingsDrawer.vue';


const components = {
  'group-management': GroupManagement,
  'group-management-tree': GroupManagementTree,
  'group-management-tree-item': GroupManagementTreeItem,
  'group-management-tree-group-menu': GroupManagementTreeGroupMenu,
  'group-management-tree-toolbar': GroupManagementTreeToolbar,
  'group-management-form-drawer': GroupManagementFormDrawer,
  'group-management-membership-form-drawer': GroupManagementMembershipFormDrawer,
  'group-management-toolbar': GroupManagementToolbar,
  'group-management-placeholder': GroupManagementPlaceholder,
  'group-members-management-toolbar': GroupMembersManagementToolbar,
  'group-members-management': GroupMembersManagement,
  'group-members-list': GroupMembersList,
  'group-members-filter-drawer': UsersManagementFilterDrawer,
  'users-management-export-csv-result': UsersManagementExportCSVResult,
  'group-members-membership-drawer': GroupMembersMembershipDrawer,
  'nested-groups-management': NestedGroupsManagement,
  'nested-groups-list': NestedGroupsList,
  'nested-groups-toolbar': NestedGroupsToolbar,
  'group-members-count': GroupMembersCount,
  'group-management-settings-drawer': GroupManagementSettingsDrawer
};

for (const key in components) {
  Vue.component(key, components[key]);
}
