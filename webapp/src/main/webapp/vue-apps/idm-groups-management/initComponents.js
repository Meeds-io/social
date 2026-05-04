import GroupsManagement from './components/GroupsManagement.vue';
import GroupsManagementTree from './components/sidebar/GroupsManagementTree.vue';
import GroupsManagementTreeItem from './components/sidebar/GroupsManagementTreeItem.vue';
import GroupsManagementTreeGroupMenu from './components/sidebar/GroupsManagementTreeGroupMenu.vue';
import GroupsManagementTreeToolbar from './components/sidebar/GroupsManagementTreeToolbar.vue';
import GroupsManagementMembershipToolbar from './components/GroupsManagementMembershipToolbar.vue';
import GroupsManagementFormDrawer from './components/drawer/GroupsManagementFormDrawer.vue';
import GroupsManagementMembershipList from './components/GroupsManagementMembershipList.vue';
import GroupsManagementMembershipFormDrawer from './components/drawer/GroupsManagementMembershipFormDrawer.vue';
import Toolbar from './components/header/Toolbar.vue';

const components = {
  'groups-management': GroupsManagement,
  'groups-management-tree': GroupsManagementTree,
  'groups-management-tree-item': GroupsManagementTreeItem,
  'groups-management-tree-group-menu': GroupsManagementTreeGroupMenu,
  'groups-management-tree-toolbar': GroupsManagementTreeToolbar,
  'groups-management-form-drawer': GroupsManagementFormDrawer,
  'groups-management-membership-toolbar': GroupsManagementMembershipToolbar,
  'groups-management-membership-list': GroupsManagementMembershipList,
  'groups-management-membership-form-drawer': GroupsManagementMembershipFormDrawer,
  'groups-management-toolbar': Toolbar
};

for (const key in components) {
  Vue.component(key, components[key]);
}
