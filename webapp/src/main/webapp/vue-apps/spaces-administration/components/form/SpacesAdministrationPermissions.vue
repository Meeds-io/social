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
    <v-checkbox
      v-model="isAdministrationPermissions"
      :disabled="!admins"
      class="mt-0">
      <template #label>
        <div class="text-body">
          {{ $t('social.spaces.administration.manageSpaces.permissionsStepAdministrators') }}
        </div>
      </template>
    </v-checkbox>
    <v-checkbox
      v-if="users"
      v-model="isUsersPermissions"
      class="mt-0">
      <template #label>
        <div class="text-body">
          {{ $t('social.spaces.administration.manageSpaces.permissionsStepUsers') }}
        </div>
      </template>
    </v-checkbox>
    <v-checkbox
      v-if="externals"
      v-model="isExternalsPermissions"
      class="mt-0">
      <template #label>
        <div class="text-body">
          {{ $t('social.spaces.administration.manageSpaces.permissionsStepExternals') }}
        </div>
      </template>
    </v-checkbox>
    <v-checkbox
      v-if="spaceAdmin"
      v-model="isSpaceAdminPermissions"
      class="mt-0">
      <template #label>
        <div class="text-body">
          {{ $t('social.spaces.administration.manageSpaces.permissionsStepSpaceAdmins') }}
        </div>
      </template>
    </v-checkbox>
    <v-checkbox
      v-model="isCustomPermissions"
      class="mt-0"
      @click="specificGroupEntries = null">
      <template #label>
        <div class="text-body">
          {{ $t('social.spaces.administration.manageSpaces.permissionsStepGroupMembers') }}
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
    value: {
      type: String,
      default: null,
    },
    space: {
      type: Object,
      default: null,
    },
    admins: {
      type: Boolean,
      default: false,
    },
    users: {
      type: Boolean,
      default: false,
    },
    externals: {
      type: Boolean,
      default: false,
    },
    spaceAdmin: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    isAdministrationPermissions: false,
    isUsersPermissions: false,
    isExternalsPermissions: false,
    isSpaceAdminPermissions: false,
    isCustomPermissions: false,
    specificGroupEntries: null,
  }),
  computed: {
    isSpecificGroup() {
      return !!this.specificGroupEntries?.length;
    },
    spaceAdminMembershipType() {
      return this.space && `manager:${this.space.groupId}` || 'spaceAdmin';
    },
    permissions() {
      const permissions = [];
      if (this.isUsersPermissions) {
        permissions.push(this.$root.usersPermission);
      }
      if (this.isExternalsPermissions) {
        permissions.push(this.$root.externalsPermission);
      }
      if (this.isAdministrationPermissions && this.admins) {
        permissions.push(this.$root.administratorsPermission);
      }
      if (this.isSpaceAdminPermissions) {
        permissions.push(this.spaceAdminMembershipType);
      }
      if (this.specificGroupEntries?.length) {
        const specificGroupEntries = this.specificGroupEntries?.map?.(g => g.groupId)?.filter?.(g => g?.length) || [];
        permissions.push(...specificGroupEntries);
      }
      // Keep all time a permission
      // to not having to use the fallback
      // where the space is considered as without an associated template
      if (this.isAdministrationPermissions && !permissions.length) {
        permissions.push(this.$root.administratorsPermission);
      }
      return permissions;
    },
    suggesterLabels() {
      return {
        placeholder: this.$t('social.spaces.administration.groupSuggester.placeholder'),
        noDataLabel: this.$t('social.spaces.administration.groupSuggester.noData')
      };
    },
  },
  watch: {
    permissions() {
      this.$emit('input', this.permissions);
    },
    value() {
      if (JSON.stringify(this.permissions) !== JSON.stringify(this.value)) {
        this.refreshSelection();
      }
    },
  },
  created() {
    this.refreshSelection();
  },
  methods: {
    refreshSelection() {
      const permissions = this.value?.slice?.();

      this.isUsersPermissions = this.users && permissions?.find?.(p => p === this.$root.usersPermission) ? true : false;
      this.isExternalsPermissions = this.externals && permissions?.find?.(p => p === this.$root.externalsPermission) ? true : false;
      this.isAdministrationPermissions = !this.admins || permissions?.find?.(p => p === this.$root.administratorsPermission) ? true : false;
      this.isSpaceAdminPermissions = this.spaceAdmin && permissions?.find?.(p => p === this.spaceAdminMembershipType) && true || false;
      this.specificGroupEntries = [];

      const specificGroupEntries = permissions?.filter?.(p =>
        (p !== this.$root.administratorsPermission)
        && (!p.includes(':') || p.split(':')[1] !== this.$root.administratorsPermission)
        && (!this.users || p !== this.$root.usersPermission)
        && (!this.externals || p !== this.$root.externalsPermission)
        && (!this.spaceAdmin || p !== this.spaceAdminMembershipType)
      )?.filter?.(g => g?.length) || null;
      this.isCustomPermissions = !!specificGroupEntries?.length;
      if (specificGroupEntries?.length) {
        specificGroupEntries.forEach(this.retrieveObject);
      }
    },
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