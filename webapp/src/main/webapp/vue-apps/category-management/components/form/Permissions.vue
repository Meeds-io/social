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
      on-icon="fa-check-square"
      off-icon="far fa-square"
      class="mt-0 ms-n1"
      disabled>
      <template #label>
        <div class="text-body">
          {{ $t('categoryManagement.permissionsStepAdministrators') }}
        </div>
      </template>
    </v-checkbox>
    <v-checkbox
      v-model="isUserPermissions"
      :disabled="isAnyPermissions"
      on-icon="fa-check-square"
      off-icon="far fa-square"
      class="mt-0 ms-n1">
      <template #label>
        <div class="text-body">
          {{ $t('categoryManagement.permissionsStepUsers') }}
        </div>
      </template>
    </v-checkbox>
    <v-checkbox
      v-model="isGuestPermissions"
      :disabled="isAnyPermissions"
      on-icon="fa-check-square"
      off-icon="far fa-square"
      class="mt-0 ms-n1">
      <template #label>
        <div class="text-body">
          {{ $t('categoryManagement.permissionsStepGuests') }}
        </div>
      </template>
    </v-checkbox>
    <v-checkbox
      v-model="isAnyPermissions"
      on-icon="fa-check-square"
      off-icon="far fa-square"
      class="mt-0 ms-n1">
      <template #label>
        <div class="text-body">
          {{ $t('categoryManagement.permissionsStepEveryone') }}
        </div>
      </template>
    </v-checkbox>
    <v-checkbox
      v-model="isCustomPermissions"
      :disabled="isAnyPermissions"
      on-icon="fa-check-square"
      off-icon="far fa-square"
      class="mt-0 ms-n1"
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
      v-model="specificGroup"
      :labels="suggesterLabels"
      :search-options="{filterType: 'all'}"
      name="specificGroupPermissions"
      class="mb-n3"
      include-spaces
      include-groups
      all-groups-for-admin />
    <div v-if="specificGroupEntries?.length" class="mt-4">
      <v-list-item
        v-for="g in specificGroupEntries"
        :key="g.id"
        class="pa-1 pb-1"
        dense>
        <v-list-item-action class="pa-0 ma-0">
          <select
            v-model="g.role"
            aria-label="hidden"
            class="ignore-vuetify-classes width-auto pa-0 ma-0"
            @change="updateIndex++"
            @blur="updateIndex++">
            <option
              v-for="role in roles"
              :key="role.value"
              :value="role.value">
              {{ role.text }}
            </option>
          </select>
        </v-list-item-action>
        <v-list-item-content class="d-flex align-center pa-0">
          <v-list-item-title class="d-flex align-center text-truncate">
            <div class="px-2">
              {{ $t('categoryManagement.permission.in') }}
            </div>
            <template v-if="g.providerId === 'group'">
              <v-icon size="28" class="me-2">
                fa-users
              </v-icon>
              <span class="text-truncate">
                {{ g.displayName }}
              </span>
            </template>
            <space-avatar
              v-else
              :space-id="g.spaceId"
              class="text-truncate" />
          </v-list-item-title>
        </v-list-item-content>
        <v-list-item-action class="pa-0 my-auto">
          <v-btn
            :title="$t('categoryManagement.label.deleteCustomGroup')"
            icon
            @click.stop.prevent="deleteSpecificGroup(g)">
            <v-icon color="error" small>fa-trash</v-icon>
          </v-btn>
        </v-list-item-action>
      </v-list-item>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    administratorsPermission: '/platform/administrators',
    usersPermission: '/platform/users',
    externalsPermission: '/platform/externals',
    everyonePermission: 'Everyone',
    isAdministrationPermissions: true,
    isUserPermissions: false,
    isGuestPermissions: false,
    isAnyPermissions: false,
    isCustomPermissions: false,
    specificGroupEntries: null,
    specificGroup: null,
    defaultRole: '*',
    updateIndex: 1,
  }),
  computed: {
    permissions() {
      const permissions = [];
      if (this.isAnyPermissions) {
        permissions.push(this.everyonePermission);
      } else {
        if (this.isUserPermissions) {
          permissions.push(`*:${this.usersPermission}`);
        }
        if (this.isGuestPermissions) {
          permissions.push(`*:${this.externalsPermission}`);
        }
        if (this.updateIndex > 0) {
          if (this.specificGroupEntries?.length) {
            const specificGroupEntries = this.specificGroupEntries?.filter?.(g => g.groupId) || [];
            permissions.push(...specificGroupEntries.map(g => `${g.role || this.defaultRole}:${g.groupId}`));
          }
          if (!permissions.length) {
            permissions.push(`*:${this.administratorsPermission}`);
          }
        }
      }
      return permissions;
    },
    suggesterLabels() {
      return {
        placeholder: this.$t('categoryManagement.groupSuggester.placeholder'),
        noDataLabel: this.$t('categoryManagement.groupSuggester.noData')
      };
    },
    roles() {
      return [{
        value: '*',
        text: this.$t('categoryManagement.permission.everyone'),
      }, {
        value: 'redactor',
        text: this.$t('categoryManagement.permission.redactors'),
      }, {
        value: 'publisher',
        text: this.$t('categoryManagement.permission.publishers'),
      }, {
        value: 'manager',
        text: this.$t('categoryManagement.permission.managers'),
      }];
    },
  },
  watch: {
    permissions() {
      this.$emit('input', this.permissions);
    },
    isAnyPermissions() {
      if (this.isAnyPermissions) {
        this.isAdministrationPermissions = true;
        this.isUserPermissions = true;
        this.isGuestPermissions = true;
        this.isCustomPermissions = false;
      }
    },
    async specificGroup() {
      if (this.specificGroup) {
        if (!this.specificGroupEntries) {
          this.specificGroupEntries = [];
        }
        this.specificGroupEntries.push({
          ...this.specificGroup,
          role: this.defaultRole,
        });
        await this.$nextTick();
        this.specificGroup = null;
      }
    },
  },
  created() {
    const permissions = this.value?.slice?.();
    this.isAnyPermissions = permissions?.find?.(p => p === this.everyonePermission) && true || false;
    this.isUserPermissions = permissions?.find?.(p => (p.includes('*:') ? p.split(':')[1] : p) === this.usersPermission) && true || false;
    this.isGuestPermissions = permissions?.find?.(p => (p.includes('*:') ? p.split(':')[1] : p) === this.externalsPermission) && true || false;
    this.specificGroupEntries = [];

    const specificGroupEntries = permissions?.filter?.(p => p)?.filter?.(p => {
      const g = p.includes('*:') ? p.split(':')[1] : p;
      return g !== this.administratorsPermission
          && g !== this.usersPermission
          && g !== this.externalsPermission
          && g !== this.everyonePermission;
    }) || null;
    this.isCustomPermissions = !!specificGroupEntries?.length;

    if (specificGroupEntries?.length) {
      specificGroupEntries.forEach(id => this.retrieveObject(id, '*')); // * specifically to not have to migrate data
    }
  },
  methods: {
    deleteSpecificGroup(group) {
      const index = this.specificGroupEntries.findIndex(g => group.id === g.id);
      if (index >= 0) {
        this.specificGroupEntries.splice(index, 1);
      }
    },
    async retrieveObject(groupId, defaultRole) {
      try {
        const role = groupId.includes(':') ? groupId.split(':')[0] : defaultRole || this.defaultRole;
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
              role,
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
              role,
              profile: {
                fullName: group.profile?.fullname,
                originalName: group.profile?.fullname,
              },
            });
          }
        }
      } catch (e) {
        console.error('Error retrieving group details with id', groupId, e);
      }
    },
  },
};
</script>