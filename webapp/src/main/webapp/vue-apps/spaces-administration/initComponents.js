import ExoSpacesAdministrationModal from './components/modal/ExoSpacesAdministrationModal.vue';
import SpaceHomeLayoutResetConfirm from './components/modal/SpaceHomeLayoutResetConfirm.vue';
import ExoSpacesAdministrationManageSpaces from './components/ExoSpacesAdministrationManageSpaces.vue';
import ExoSpacesAdministrationSpacesPermissions from './components/ExoSpacesAdministrationSpacesPermissions.vue';
import ExoSpacesAdministrationSpaces  from './components/ExoSpacesAdministrationSpaces.vue';
import ExoGroupBindingDrawer from './components/drawer/ExoGroupBindingDrawer.vue';
import ExoGroupBindingSecondLevelDrawer from './components/drawer/ExoGroupBindingSecondLevelDrawer.vue';
import ExoSpacesAdministrationBindingReports from './components/ExoSpacesAdministrationBindingReports.vue';

const components = {
  'exo-spaces-administration-manage-spaces': ExoSpacesAdministrationManageSpaces,
  'exo-spaces-administration-manage-permissions': ExoSpacesAdministrationSpacesPermissions,
  'exo-spaces-administration-spaces': ExoSpacesAdministrationSpaces,
  'exo-spaces-administration-modal': ExoSpacesAdministrationModal,
  'spaces-administration-home-layout-confirm': SpaceHomeLayoutResetConfirm,
  'exo-group-binding-drawer': ExoGroupBindingDrawer,
  'exo-group-binding-second-level-drawer': ExoGroupBindingSecondLevelDrawer,
  'exo-spaces-administration-binding-reports': ExoSpacesAdministrationBindingReports,
};

for (const key in components) {
  Vue.component(key, components[key]);
}