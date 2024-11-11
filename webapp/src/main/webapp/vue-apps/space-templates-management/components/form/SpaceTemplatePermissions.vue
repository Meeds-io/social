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
  <div class="d-flex flex-column">
    <help-label
      v-if="helpLabel"
      :label="helpLabel"
      :tooltip="helpTooltip"
      label-class="font-weight-bold">
      <template slot="helpContent">
        <slot name="helpContent"></slot>
      </template>
    </help-label>
    <div v-else class="font-weight-bold">
      {{ $t(label) }}
    </div>
    <v-checkbox
      v-if="admins"
      v-model="isAdministrationPermissions"
      class="mt-0"
      disabled>
      <template #label>
        <div class="text-body">
          {{ $t('spaceTemplate.permissionsStepAdministrators') }}
        </div>
      </template>
    </v-checkbox>
    <v-checkbox
      v-if="users"
      v-model="isUserPermissions"
      class="mt-0">
      <template #label>
        <div class="text-body">
          {{ $t('spaceTemplate.permissionsStepUsers') }}
        </div>
      </template>
    </v-checkbox>
    <v-checkbox
      v-if="spaceAdmin"
      v-model="isSpaceAdminPermissions"
      class="mt-0">
      <template #label>
        <div class="text-body">
          {{ $t('spaceTemplate.permissionsStepSpaceAdmins') }}
        </div>
      </template>
    </v-checkbox>
    <v-checkbox
      v-model="isCustomPermissions"
      class="mt-0"
      @click="specificGroupEntries = null">
      <template #label>
        <div class="text-body">
          {{ $t('spaceTemplate.permissionsStepGroupMembers') }}
        </div>
      </template>
    </v-checkbox>
    <exo-identity-suggester
      v-if="isCustomPermissions"
      ref="targetPermissions"
      v-model="specificGroupEntries"
      :labels="suggesterLabels"
      :group-member="userGroup"
      :search-options="{filterType: 'all'}"
      name="specificGroupPermissions"
      include-spaces
      include-groups
      all-groups-for-admin
      multiple
      required />
  </div>
</template>
<script>
export default {
  props: {
    label: {
      type: String,
      default: null,
    },
    helpLabel: {
      type: String,
      default: null,
    },
    helpTooltip: {
      type: String,
      default: null,
    },
    value: {
      type: String,
      default: null,
    },
    users: {
      type: Boolean,
      default: false,
    },
    admins: {
      type: Boolean,
      default: false,
    },
    spaceAdmin: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    isAdministrationPermissions: true,
    isUserPermissions: false,
    isSpaceAdminPermissions: false,
    isCustomPermissions: false,
    specificGroupEntries: null,
  }),
  computed: {
    isSpecificGroup() {
      return !!this.specificGroupEntries?.length;
    },
    permissions() {
      const permissions = [];
      if (this.isUserPermissions) {
        permissions.push(this.$root.usersPermission);
      }
      if (this.isSpaceAdminPermissions) {
        permissions.push('spaceAdmin');
      }
      if (this.specificGroupEntries?.length) {
        const specificGroupEntries = this.specificGroupEntries?.map?.(g => g.groupId)?.filter?.(g => g) || [];
        permissions.push(...specificGroupEntries);
      }
      if (!permissions.length) {
        permissions.push(this.$root.administratorsPermission);
      }
      return permissions;
    },
    suggesterLabels() {
      return {
        placeholder: this.$t('spaceTemplate.groupSuggester.placeholder'),
        noDataLabel: this.$t('spaceTemplate.groupSuggester.noData')
      };
    },
  },
  watch: {
    permissions() {
      this.$emit('input', this.permissions);
    },
  },
  created() {
    const permissions = this.value?.slice?.();
    this.isUserPermissions = this.users && permissions?.find?.(p => p === this.$root.usersPermission) && true || false;
    this.isSpaceAdminPermissions = this.spaceAdmin && permissions?.find?.(p => p === 'spaceAdmin') && true || false;
    this.specificGroupEntries = [];

    const specificGroupEntries = permissions?.filter?.(p =>
      p !== this.$root.administratorsPermission
      && (!p.includes(':') || p.split(':')[1] !== this.$root.administratorsPermission)
      && (!this.users || p !== this.$root.usersPermission)
      && (!this.spaceAdmin || p !== 'spaceAdmin')
    ) || null;
    this.isCustomPermissions = !!specificGroupEntries?.length;
    if (specificGroupEntries?.length) {
      specificGroupEntries.forEach(this.retrieveObject);
    }
  },
  methods: {
    async retrieveObject(groupId) {
      groupId = groupId.includes(':') ? groupId.split(':')[1] : groupId;
      if (groupId.indexOf('/spaces/') === 0) {
        const space = await this.$spaceService.getSpaceByGroupId(groupId);
        if (space) {
          this.specificGroupEntries.push({
            id: `space:${space.prettyName}`,
            remoteId: space.prettyName,
            spaceId: space.id,
            groupId: space.groupId,
            providerId: 'space',
            displayName: space.displayName,
            profile: {
              fullName: space.displayName,
              originalName: space.shortName,
              avatarUrl: space.avatarUrl ? space.avatarUrl : `/portal/rest/v1/social/spaces/${space.prettyName}/avatar`,
            },
          });
        }
      } else {
        const group = await this.$identityService.getIdentityByProviderIdAndRemoteId('group', groupId);
        if (group) {
          this.specificGroupEntries.push({
            id: `group:${group.remoteId}`,
            remoteId: group.remoteId,
            spaceId: groupId,
            groupId: groupId,
            providerId: 'group',
            displayName: group.profile?.fullname,
            profile: {
              fullName: group.profile?.fullname,
              originalName: group.profile?.fullname,
            },
          });
        }
      }
    },
  },
};
</script>