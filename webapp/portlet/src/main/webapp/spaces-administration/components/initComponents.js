import ExoSpacesAdministrationModal from './modal/ExoSpacesAdministrationModal.vue';
import ExoSpacesAdministrationManageSpaces from './ExoSpacesAdministrationManageSpaces.vue';
import ExoSpacesAdministrationSpacesPermissions from './ExoSpacesAdministrationSpacesPermissions.vue';
import ExoSpacesAdministrationSpaces  from './ExoSpacesAdministrationSpaces.vue';
import ExoGroupBindingDrawer from './drawer/ExoGroupBindingDrawer.vue';
import ExoGroupBindingSecondLevelDrawer from './drawer/ExoGroupBindingSecondLevelDrawer.vue';
import ExoSpacesAdministrationBindingReports from './ExoSpacesAdministrationBindingReports.vue';
import ExoSpacesTemplatesSpaces  from './ExoSpacesTemplatesSpaces.vue';
import ExoSpaceTemplate  from './ExoSpaceTemplate.vue';
import ExoSpaceApplications from './ExoSpaceApplications.vue';
import ExoSpaceApplicationCard from './ExoSpaceApplicationCard.vue';
import ExoSpaceApplicationCategoryCard from './ExoSpaceApplicationCategoryCard.vue';
import ExoSpaceAddApplicationDrawer from './drawer/ExoSpaceAddApplicationDrawer.vue';

const components = {
  'exo-spaces-administration-manage-spaces': ExoSpacesAdministrationManageSpaces,
  'exo-spaces-administration-manage-permissions': ExoSpacesAdministrationSpacesPermissions,
  'exo-spaces-administration-spaces': ExoSpacesAdministrationSpaces,
  'exo-spaces-administration-modal': ExoSpacesAdministrationModal,
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