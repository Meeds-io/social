import GroupManagement from './components/GroupManagement.vue';
import GroupManagementTree from './components/sidebar/GroupManagementTree.vue';
import GroupManagementTreeItem from './components/sidebar/GroupManagementTreeItem.vue';
import GroupManagementTreeGroupMenu from './components/sidebar/GroupManagementTreeGroupMenu.vue';
import GroupManagementTreeToolbar from './components/sidebar/GroupManagementTreeToolbar.vue';
import GroupManagementMembershipToolbar from './components/GroupManagementMembershipToolbar.vue';
import GroupManagementFormDrawer from './components/drawer/GroupManagementFormDrawer.vue';
import GroupManagementMembershipList from './components/GroupManagementMembershipList.vue';
import GroupManagementMembershipFormDrawer from './components/drawer/GroupManagementMembershipFormDrawer.vue';
import GroupManagementToolbar from './components/header/GroupManagementToolbar.vue';
import GroupManagementPlaceholder from './components/GroupManagementPlaceholder.vue';

const components = {
  'group-management': GroupManagement,
  'group-management-tree': GroupManagementTree,
  'group-management-tree-item': GroupManagementTreeItem,
  'group-management-tree-group-menu': GroupManagementTreeGroupMenu,
  'group-management-tree-toolbar': GroupManagementTreeToolbar,
  'group-management-form-drawer': GroupManagementFormDrawer,
  'group-management-membership-toolbar': GroupManagementMembershipToolbar,
  'group-management-membership-list': GroupManagementMembershipList,
  'group-management-membership-form-drawer': GroupManagementMembershipFormDrawer,
  'group-management-toolbar': GroupManagementToolbar,
  'group-management-placeholder': GroupManagementPlaceholder
};

for (const key in components) {
  Vue.component(key, components[key]);
}
