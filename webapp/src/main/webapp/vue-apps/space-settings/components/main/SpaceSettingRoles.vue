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
  <div v-if="$root.isRolesSection || $root.isAllSections">
    <space-setting-roles-section
      v-if="$root.isRolesSection" />
    <space-setting-row
      v-else
      :title="$t('SpaceSettings.roles')"
      :summary="summary">
      <template #action>
        <v-btn
          small
          icon
          @click="$root.showSection('roles')">
          <v-icon size="18" class="icon-default-color">fa-caret-right</v-icon>
        </v-btn>
      </template>
    </space-setting-row>
  </div>
</template>
<script>
export default {
  computed: {
    /**
     * @returns {String} the number of members of the space, as one line
     */
    summary() {
      const membersCount = this.$root.space.membersCount || 0;
      return membersCount === 1
        ? this.$t('SpaceSettings.roles.summary.one')
        : this.$t('SpaceSettings.roles.summary.many', {0: membersCount});
    },
  },
};
</script>
