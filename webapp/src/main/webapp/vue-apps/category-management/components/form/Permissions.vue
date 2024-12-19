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
      class="mt-0"
      disabled>
      <template #label>
        <div class="text-body">
          {{ $t('categoryManagement.permissionsStepAdministrators') }}
        </div>
      </template>
    </v-checkbox>
    <v-checkbox
      v-model="isUserPermissions"
      class="mt-0">
      <template #label>
        <div class="text-body">
          {{ $t('categoryManagement.permissionsStepUsers') }}
        </div>
      </template>
    </v-checkbox>
    <v-checkbox
      v-model="isGuestPermissions"
      class="mt-0">
      <template #label>
        <div class="text-body">
          {{ $t('categoryManagement.permissionsStepGuests') }}
        </div>
      </template>
    </v-checkbox>
    <v-checkbox
      v-model="isCustomPermissions"
      class="mt-0"
      @click="specificGroupEntries = null">
      <template #label>
        <div class="text-body">
          {{ $t('categoryManagement.permissionsStepGroupMembers') }}
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
      class="mb-n3"
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
    isGuestPermissions: false,
    isCustomPermissions: false,
    specificGroupEntries: null,
    permissionIdentityIds: null,
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
      if (this.isGuestPermissions) {
        permissions.push(this.$root.guestsPermission);
      }
      if (this.specificGroupEntries?.length) {
        const specificGroupEntries = this.specificGroupEntries?.map?.(g => g.groupId)?.filter?.(g => g) || [];
        permissions.push(...specificGroupEntries);
      }
      return permissions;
    },
    suggesterLabels() {
      return {
        placeholder: this.$t('categoryManagement.groupSuggester.placeholder'),
        noDataLabel: this.$t('categoryManagement.groupSuggester.noData')
      };
    },
  },
  watch: {
    permissions() {
      this.computePermissionIdentityIds();
    },
    permissionIdentityIds() {
      this.$emit('input', this.permissionIdentityIds);
    },
  },
  async created() {
    const identityIds = this.value?.slice?.() || [];
    const permissions = await Promise.all(identityIds.map(this.retrieveGroupIdByIdentityId));
    this.isUserPermissions = permissions?.find?.(p => p === this.$root.usersPermission) && true || false;
    this.isGuestPermissions = permissions?.find?.(p => p === this.$root.guestsPermission) && true || false;
    this.specificGroupEntries = [];

    const specificGroupEntries = permissions?.filter?.(p => p
      && p !== this.$root.administratorsPermission
      && p !== this.$root.usersPermission
      && p !== this.$root.guestsPermission
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
    async computePermissionIdentityIds() {
      const permissionIdentityIds = await Promise.all(this.permissions.map(this.retrieveIdentityIdByGroupId));
      this.permissionIdentityIds = permissionIdentityIds.filter(id => id);
    },
    async retrieveIdentityIdByGroupId(groupId) {
      if (Object.hasOwn(this.$root.identityIdPerGroup, groupId)) {
        return this.$root.identityIdPerGroup[groupId];
      } else {
        try {
          if (groupId?.startsWith('/spaces/')) {
            const space = await this.$spaceService.getSpaceByGroupId(groupId);
            if (space) {
              const identity = await this.$identityService.getIdentityByProviderIdAndRemoteId('space', space.prettyName);
              if (identity) {
                const identityId = Number(identity.id);
                this.$root.identityIdPerGroup[groupId] = identityId;
                return identityId;
              }
            }
          } else if (groupId?.startsWith('/')) {
            const identity = await this.$identityService.getIdentityByProviderIdAndRemoteId('group', groupId);
            if (identity) {
              const identityId = Number(identity.id);
              this.$root.identityIdPerGroup[groupId] = identityId;
              return identityId;
            }
          }
        } catch (e) {
          // eslint-disable-next-line no-console
          console.error('Error while retrieving Identity with groupId : ', groupId);
        }
        return 0;
      }
    },
    async retrieveGroupIdByIdentityId(identityId) {
      if (Object.hasOwn(this.$root.groupPerIdentityId, identityId)) {
        return this.$root.groupPerIdentityId[identityId];
      } else {
        try {
          const identity = await this.$identityService.getIdentityById(identityId);
          if (identity.providerId === 'space') {
            const groupId = identity?.space?.groupId;
            this.$root.groupPerIdentityId[identityId] = groupId;
            return groupId;
          } else {
            const groupId = identity?.remoteId;
            this.$root.groupPerIdentityId[identityId] = groupId;
            return groupId;
          }
        } catch (e) {
          // eslint-disable-next-line no-console
          console.error('Error while retrieving Identity with id : ', identityId);
        }
        this.$root.groupPerIdentityId[identityId] = null;
        return null;
      }
    },
  },
};
</script>