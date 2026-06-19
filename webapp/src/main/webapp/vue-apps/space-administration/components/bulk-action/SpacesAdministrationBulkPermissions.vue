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
  <v-btn
    :disabled="$root.isBulkProcessing"
    color="primary"
    elevation="0"
    outlined
    @click="$root.$emit('space-administration-permissions-drawer-open', $root.selectedSpaces, $root.allSpacesSelected ? $root.spacesSize : $root.selectedSpaces.length, updatePermissions)">
    <v-icon size="16" class="me-2">fa-shield-alt</v-icon>
    {{ $t('social.spaces.administration.manageSpaces.editPermissions') }}
  </v-btn>
</template>
<script>
export default {
  methods: {
    updatePermissions(params) {
      // Workaround for context change, compute success message on processing start
      this.$root.applyOperationInBulk(
        async space => {
          const permissions = {};
          permissions.layoutPermissions = params.layoutPermissions.map(g => g.replace('spaceAdmin', `manager:${space.groupId}`));
          permissions.publicSitePermissions = params.publicSitePermissions.map(g => g.replace('spaceAdmin', `manager:${space.groupId}`));
          permissions.deletePermissions = params.deletePermissions.map(g => g.replace('spaceAdmin', `manager:${space.groupId}`));
          permissions.extendedPermissions = params.extendedPermissions;
          await this.$spaceAdministrationService.updateSpacePermissions(space.id, permissions);
        },
        null,
        () => {
          this.$root.$emit('alert-message', this.$root.$t('social.spaces.administration.manageSpaces.spacesPermissionsUpdatedSuccessfully'), 'success');
          this.$root.$emit('spaces-administration-list-refresh');
        });
    },
  },
};
</script>