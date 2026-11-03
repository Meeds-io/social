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
    <v-main v-if="$root.space" class="border-box-sizing">
      <!--
        One card per group, as the user Settings page draws its sections. Social
        places its own rows; an extension row goes to the group its registration
        names in `group`, Applications when it names none. The Identity and Roles
        sections open inside their group's card, with the padding they are laid out for.
      -->
      <v-card
        v-if="isActiveSection || $root.isOverviewSection"
        id="SpaceSettingsGroupGeneralCard"
        :class="!isActiveSection && 'px-5 py-1'"
        class="space-settings-group application-layout-style mb-4"
        flat>
        <space-setting-group-title
          v-if="isActiveSection"
          id="SpaceSettingsGroupGeneral"
          :title="$t('SpaceSettings.group.general')" />
        <space-setting-overview id="SpaceSettingsOverview" />
        <space-setting-public-site
          id="SpaceSettingsPublicAccess"
          v-if="$root.space?.canManagePublicSite"
          divider />
        <space-setting-categories id="SpaceSettingsCategories" divider />
        <space-setting-sovereign divider />
        <space-setting-subspaces
          id="SpaceSettingsSubspaces"
          v-if="isParentSpace"
          divider />
        <space-setting-group-extensions
          v-if="isActiveSection"
          :params="extensionParams"
          :filter="groupFilters.general"
          divider-first />
      </v-card>

      <v-card
        v-if="isActiveSection || $root.isRolesSection"
        id="SpaceSettingsGroupMembersCard"
        :class="!isActiveSection && 'px-5 py-1'"
        class="space-settings-group application-layout-style mb-4"
        flat>
        <space-setting-group-title
          v-if="isActiveSection"
          id="SpaceSettingsGroupMembers"
          :title="$t('SpaceSettings.group.members')" />
        <space-setting-roles id="SpaceSettingsRoles" />
        <space-setting-access id="SpaceSettingsAccess" divider />
        <space-setting-group-extensions
          v-if="isActiveSection"
          :params="extensionParams"
          :filter="groupFilters.members"
          divider-first />
      </v-card>

      <space-setting-group-extensions
        v-if="isActiveSection"
        id="SpaceSettingsGroupApplicationsCard"
        :params="extensionParams"
        :filter="groupFilters.applications"
        :title="$t('SpaceSettings.group.applications')"
        title-id="SpaceSettingsGroupApplications"
        class="space-settings-group application-layout-style mb-4" />

      <!-- The last card carries no title: Delete space, and the rows that name the danger group -->
      <template v-if="isActiveSection">
        <v-card
          v-if="canDelete"
          id="SpaceSettingsGroupDangerCard"
          class="space-settings-group application-layout-style mb-4"
          flat>
          <space-setting-group-extensions
            :params="extensionParams"
            :filter="groupFilters.danger"
            divider-last />
          <space-setting-delete-space />
        </v-card>
        <space-setting-group-extensions
          v-else
          id="SpaceSettingsGroupDangerCard"
          :params="extensionParams"
          :filter="groupFilters.danger"
          class="space-settings-group application-layout-style mb-4" />
      </template>
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
