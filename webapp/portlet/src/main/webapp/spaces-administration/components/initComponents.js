/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import ExoModal from './modal/ExoModal.vue';
import ExoSpacesAdministrationManageSpaces from './ExoSpacesAdministrationManageSpaces.vue';
import ExoSpacesAdministrationSpacesPermissions from './ExoSpacesAdministrationSpacesPermissions.vue';
import ExoSpacesAdministrationSpaces  from './ExoSpacesAdministrationSpaces.vue';
import ExoGroupBindingDrawer from './drawer/ExoGroupBindingDrawer.vue';
import ExoGroupBindingSecondLevelDrawer from './drawer/ExoGroupBindingSecondLevelDrawer.vue';
import ExoSpacesAdministrationBindingReports from './ExoSpacesAdministrationBindingReports.vue';
import ExoSuggester from './suggester/ExoSuggester.vue';
import ExoSpacesTemplatesSpaces  from './ExoSpacesTemplatesSpaces.vue';
import ExoSpaceTemplate  from './ExoSpaceTemplate.vue';
import ExoSpaceApplications from './ExoSpaceApplications.vue';
import ExoSpaceApplicationCard from './ExoSpaceApplicationCard.vue';
import ExoSpaceApplicationCategoryCard from './ExoSpaceApplicationCategoryCard.vue';
import ExoSpaceAddApplicationDrawer from './drawer/ExoSpaceAddApplicationDrawer.vue';

const components = {
  'exo-spaces-administration-manage-spaces': ExoSpacesAdministrationManageSpaces,
  'exo-spaces-administration-manage-permissions' : ExoSpacesAdministrationSpacesPermissions,
  'exo-spaces-administration-spaces' : ExoSpacesAdministrationSpaces,
  'exo-modal' : ExoModal,
  'exo-group-binding-drawer' : ExoGroupBindingDrawer,
  'exo-group-binding-second-level-drawer' : ExoGroupBindingSecondLevelDrawer,
  'exo-spaces-administration-binding-reports' : ExoSpacesAdministrationBindingReports,
  'exo-suggester' : ExoSuggester,
  'exo-space-template' : ExoSpaceTemplate,
  'exo-space-templates-spaces' : ExoSpacesTemplatesSpaces,
  'exo-space-applications' : ExoSpaceApplications,
  'exo-space-application-card' : ExoSpaceApplicationCard,
  'exo-space-application-category-card' : ExoSpaceApplicationCategoryCard,
  'exo-space-add-application-drawer' : ExoSpaceAddApplicationDrawer,
};

for(const key in components) {
  Vue.component(key, components[key]);
}