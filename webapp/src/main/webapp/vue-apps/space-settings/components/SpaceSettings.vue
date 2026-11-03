<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <v-app class="application-body">
    <v-main v-if="$root.space" class="px-5 py-1 border-box-sizing">
      <!--
        Four groups. Social places its own rows; an extension row goes to the
        group its registration names in `group`, Applications when it names none.
      -->
      <div
        v-if="isActiveSection"
        id="SpaceSettingsGroupGeneral"
        class="space-settings-group-title text-caption font-weight-bold text-uppercase text-sub-title pt-4 pb-1">
        {{ $t('SpaceSettings.group.general') }}
      </div>
      <space-setting-overview id="SpaceSettingsOverview" />
      <space-setting-public-site
        id="SpaceSettingsPublicAccess"
        v-if="$root.space?.canManagePublicSite" />
      <space-setting-categories id="SpaceSettingsCategories" />
      <space-setting-sovereign />
      <space-setting-subspaces id="SpaceSettingsSubspaces" v-if="isParentSpace" />
      <extension-registry-components
        v-if="isActiveSection"
        :params="extensionParams"
        :filter="groupFilters.general"
        name="SpaceSettings"
        type="space-settings-components"
        parent-element="div"
        element="div" />

      <div
        v-if="isActiveSection"
        id="SpaceSettingsGroupMembers"
        class="space-settings-group-title text-caption font-weight-bold text-uppercase text-sub-title pt-4 pb-1">
        {{ $t('SpaceSettings.group.members') }}
      </div>
      <space-setting-roles id="SpaceSettingsRoles" />
      <space-setting-access id="SpaceSettingsAccess" />
      <extension-registry-components
        v-if="isActiveSection"
        :params="extensionParams"
        :filter="groupFilters.members"
        name="SpaceSettings"
        type="space-settings-components"
        parent-element="div"
        element="div" />

      <extension-registry-components
        v-if="isActiveSection"
        :params="extensionParams"
        :filter="groupFilters.applications"
        name="SpaceSettings"
        type="space-settings-components"
        parent-element="div"
        element="div">
        <template #header>
          <div
            id="SpaceSettingsGroupApplications"
            class="space-settings-group-title text-caption font-weight-bold text-uppercase text-sub-title pt-4 pb-1">
            {{ $t('SpaceSettings.group.applications') }}
          </div>
        </template>
      </extension-registry-components>

      <template v-if="isActiveSection">
        <div
          v-if="canDelete"
          id="SpaceSettingsGroupDanger"
          class="space-settings-group-title text-caption font-weight-bold text-uppercase error--text pt-4 pb-1">
          {{ $t('SpaceSettings.group.danger') }}
        </div>
        <extension-registry-components
          :params="extensionParams"
          :filter="groupFilters.danger"
          name="SpaceSettings"
          type="space-settings-components"
          parent-element="div"
          element="div">
          <template v-if="!canDelete" #header>
            <div
              id="SpaceSettingsGroupDanger"
              class="space-settings-group-title text-caption font-weight-bold text-uppercase error--text pt-4 pb-1">
              {{ $t('SpaceSettings.group.danger') }}
            </div>
          </template>
        </extension-registry-components>
      </template>
      <space-setting-delete-space v-if="canDelete" />
    </v-main>
  </v-app>
</template>
<script>
/**
 * The groups an extension row may name in its registration's `group`.
 */
const GROUPS = ['general', 'members', 'applications', 'danger'];

/**
 * The group of an extension row that names none, or one not in GROUPS.
 */
const DEFAULT_GROUP = 'applications';

export default {
  data: () => ({
    groupFilters: Object.fromEntries(GROUPS.map(group => [group, component => {
      const componentGroup = component?.componentOptions?.group;
      return (GROUPS.includes(componentGroup) ? componentGroup : DEFAULT_GROUP) === group;
    }])),
  }),
  computed: {
    extensionParams() {
      return {
        spaceId: this.$root.spaceId,
      };
    },
    isActiveSection() {
      return !this.$root.activeSection;
    },
    isParentSpace() {
      return this.$root.space?.isParentSpace;
    },
    canDelete() {
      return this.$root.space?.canDelete;
    },
  },
};
</script>
