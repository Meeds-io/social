import SpaceMembers from './components/SpaceMembers.vue';
import SpaceMembersToolbar from './components/SpaceMembersToolbar.vue';
import SpaceInvitationDrawer from './components/SpaceInvitationDrawer.vue';

const components = {
  'space-members': SpaceMembers,
  'space-members-toolbar': SpaceMembersToolbar,
  'space-invitation-drawer': SpaceInvitationDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
