import SpaceMembers from './components/SpaceMembers.vue';
import SpaceMembersToolbar from './components/SpaceMembersToolbar.vue';
import SpaceInvitationDrawer from './components/SpaceInvitationDrawer.vue';
import AlertSpaceMembers from './components/AlertSpaceMembers.vue';

const components = {
  'space-members': SpaceMembers,
  'space-members-toolbar': SpaceMembersToolbar,
  'space-invitation-drawer': SpaceInvitationDrawer,
  'alert-space-members': AlertSpaceMembers,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
