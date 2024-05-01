import ExoSpacesAdministrationModal from './components/modal/ExoSpacesAdministrationModal.vue';
import SpaceHomeLayoutResetConfirm from './components/modal/SpaceHomeLayoutResetConfirm.vue';
import ExoSpacesAdministrationManageSpaces from './components/ExoSpacesAdministrationManageSpaces.vue';
import ExoSpacesAdministrationSpacesPermissions from './components/ExoSpacesAdministrationSpacesPermissions.vue';
import ExoSpacesAdministrationSpaces  from './components/ExoSpacesAdministrationSpaces.vue';
import ExoGroupBindingDrawer from './components/drawer/ExoGroupBindingDrawer.vue';
import ExoGroupBindingSecondLevelDrawer from './components/drawer/ExoGroupBindingSecondLevelDrawer.vue';
import ExoSpacesAdministrationBindingReports from './components/ExoSpacesAdministrationBindingReports.vue';
import ExoSpacesTemplatesSpaces  from './components/ExoSpacesTemplatesSpaces.vue';
import ExoSpaceTemplate  from './components/ExoSpaceTemplate.vue';
import ExoSpaceApplications from './components/ExoSpaceApplications.vue';
import ExoSpaceApplicationCard from './components/ExoSpaceApplicationCard.vue';
import ExoSpaceApplicationCategoryCard from './components/ExoSpaceApplicationCategoryCard.vue';
import ExoSpaceAddApplicationDrawer from './components/drawer/ExoSpaceAddApplicationDrawer.vue';

const components = {
  'exo-spaces-administration-manage-spaces': ExoSpacesAdministrationManageSpaces,
  'exo-spaces-administration-manage-permissions': ExoSpacesAdministrationSpacesPermissions,
  'exo-spaces-administration-spaces': ExoSpacesAdministrationSpaces,
  'exo-spaces-administration-modal': ExoSpacesAdministrationModal,
  'spaces-administration-home-layout-confirm': SpaceHomeLayoutResetConfirm,
  'exo-group-binding-drawer': ExoGroupBindingDrawer,
  'exo-group-binding-second-level-drawer': ExoGroupBindingSecondLevelDrawer,
  'exo-spaces-administration-binding-reports': ExoSpacesAdministrationBindingReports,
  'exo-space-template': ExoSpaceTemplate,
  'exo-space-templates-spaces': ExoSpacesTemplatesSpaces,
  'exo-space-applications': ExoSpaceApplications,
  'exo-space-application-card': ExoSpaceApplicationCard,
  'exo-space-application-category-card': ExoSpaceApplicationCategoryCard,
  'exo-space-add-application-drawer': ExoSpaceAddApplicationDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}