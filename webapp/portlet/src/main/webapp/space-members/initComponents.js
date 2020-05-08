import SpaceMembers from './components/SpaceMembers.vue';
import SpaceMembersToolbar from './components/SpaceMembersToolbar.vue';

const components = {
  'space-members': SpaceMembers,
  'space-members-toolbar': SpaceMembersToolbar,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
