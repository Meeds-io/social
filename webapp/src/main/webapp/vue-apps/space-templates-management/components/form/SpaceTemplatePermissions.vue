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
    <div class="font-weight-bold">
      {{ $t(label) }}
    </div>
    <v-radio-group
      v-model="choice"
      class="mt-2 ms-n1"
      mandatory
      inset>
      <v-radio
        v-if="users"
        value="users"
        class="mt-0">
        <template #label>
          <div class="text-body">
            {{ $t('spaceTemplate.permissionsStepUsers') }}
          </div>
        </template>
      </v-radio>
      <v-radio
        v-if="admins"
        value="admins"
        class="mt-0">
        <template #label>
          <div class="text-body">
            {{ $t('spaceTemplate.permissionsStepAdministrators') }}
          </div>
        </template>
      </v-radio>
      <v-radio
        v-if="spaceAdmin"
        value="spaceAdmin"
        class="mt-0">
        <template #label>
          <div class="text-body">
            {{ $t('spaceTemplate.permissionsStepSpaceAdmins') }}
          </div>
        </template>
      </v-radio>
      <v-radio
        value="suggester"
        class="mt-0">
        <template #label>
          <div class="text-body">
            {{ $t('spaceTemplate.permissionsStepGroupMembers') }}
          </div>
        </template>
      </v-radio>
    </v-radio-group>
    <exo-identity-suggester
      v-if="isSpecificGroup"
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
    choice: null,
    specificGroupEntries: null,
  }),
  computed: {
    permissions() {
      if (this.choice === 'users') {
        return [this.$root.usersPermission];
      } else if (this.choice === 'admins') {
        return [this.$root.administratorsPermission];
      } else if (this.choice === 'spaceAdmin') {
        return ['spaceAdmin'];
      } else {
        return this.specificGroupEntries?.map?.(g => g.groupId)?.filter?.(g => g);
      }
    },
    isSpecificGroup() {
      return this.choice === 'suggester';
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
    if (this.users && this.value?.length === 1 && this.value?.[0] === this.$root.usersPermission) {
      this.choice = 'users';
      this.specificGroupEntries = null;
    } else if (this.admins && this.value?.length === 1 && this.value?.[0] === this.$root.administratorsPermission) {
      this.choice = 'admins';
      this.specificGroupEntries = null;
    } else if (this.spaceAdmin && this.value?.[0] === 'spaceAdmin') {
      this.choice = 'spaceAdmin';
      this.specificGroupEntries = null;
    } else {
      this.choice = 'suggester';
      this.specificGroupEntries = [];
      if (this.value?.length) {
        this.value.forEach(this.retrieveObject);
      }
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