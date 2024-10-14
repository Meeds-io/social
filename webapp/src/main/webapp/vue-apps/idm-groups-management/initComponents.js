import GroupsManagement from './components/GroupsManagement.vue';
import GroupsManagementTree from './components/GroupsManagementTree.vue';
import GroupsManagementTreeItem from './components/GroupsManagementTreeItem.vue';
import GroupsManagementTreeGroupMenu from './components/GroupsManagementTreeGroupMenu.vue';
import GroupsManagementTreeToolbar from './components/GroupsManagementTreeToolbar.vue';
import GroupsManagementMembershipToolbar from './components/GroupsManagementMembershipToolbar.vue';
import GroupsManagementFormDrawer from './components/GroupsManagementFormDrawer.vue';
import GroupsManagementMembershipList from './components/GroupsManagementMembershipList.vue';
import GroupsManagementMembershipFormDrawer from './components/GroupsManagementMembershipFormDrawer.vue';

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
};

for (const key in components) {
  Vue.component(key, components[key]);
}
