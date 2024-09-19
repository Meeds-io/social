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
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    :loading="saving || loading"
    class="spaceSettingsUsersSelectionDrawer"
    allow-expand
    right
    @closed="$emit('closed')">
    <template #title>
      {{ $t(`SpaceSettings.users.add.drawer.${role}.title`) }}
    </template>
    <template v-if="drawer" #content>
      <div class="pa-4">
        <div>
          {{ $t('SpaceSettings.roles.restrictContentCreation.placeholder1') }}
        </div>
        <div class="pt-2">
          {{ $t('SpaceSettings.roles.restrictContentCreation.placeholder2') }}
        </div>
        <identity-suggester
          ref="suggester"
          id="userMemberSuggester"
          v-model="selectedUser"
          :labels="suggesterLabels"
          :search-options="searchOptions"
          :ignore-items="ignoreSuggesterItems"
          name="userMemberSuggester"
          class="user-suggester"
          width="220"
          include-users
          ignore-cache />
        <div v-if="users?.length">
          <space-setting-roles-list
            :users="users"
            @add="addUserMembership"
            @remove="removeUserMembership" />
        </div>
      </div>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="saving"
          class="btn me-2"
          @click="close">
          <template>
            {{ $t('SpaceSettings.button.cancel') }}
          </template>
        </v-btn>
        <v-btn
          :loading="saving"
          class="btn btn-primary"
          @click.prevent.stop="saveUsers">
          {{ $t('SpaceSettings.button.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    loading: false,
    saving: false,
    selectedUser: null,
    existingUsers: [],
    users: [],
    role: null,
  }),
  computed: {
    ignoreSuggesterItems() {
      return [...this.users, ...this.existingUsers].map(u => `organization:${u.username}`);
    },
    suggesterLabels() {
      return {
        searchPlaceholder: this.$t(`SpaceSettings.roles.${this.role}.searchPlaceholder`),
        placeholder: this.$t(`SpaceSettings.roles.${this.role}.placeholder`),
        noDataLabel: this.$t(`SpaceSettings.roles.${this.role}.noDataLabel`),
      };
    },
    searchOptions() {
      return {
        spacePrettyName: eXo.env.portal.spaceName,
        currentUser: false,
      };
    },
  },
  watch: {
    selectedUser() {
      if (this.selectedUser) {
        this.addUserMembership({
          id: this.selectedUser.identityId,
          fullname: this.selectedUser?.profile?.fullName,
          username: this.selectedUser?.remoteId,
          avatar: this.selectedUser?.profile?.avatarUrl,
          deleted: false,
          enabled: true,
        });
        this.$nextTick().then(() => this.selectedUser = null);
      }
    },
  },
  created() {
    this.$root.$on('space-settings-user-add', this.open);
  },
  beforeDestroy() {
    this.$root.$off('space-settings-user-add', this.open);
  },
  methods: {
    open(role) {
      this.role = role;
      this.users = [];
      this.$refs.drawer.open();
      this.init();
    },
    async init() {
      this.loading = true;
      try {
        this.existingUsers = await this.getAllUsers();
      } finally {
        this.loading = false;
      }
    },
    close() {
      this.$refs.drawer.close();
    },
    removeUserMembership(user) {
      const index = this.users.findIndex(u => u.id === user.id);
      if (index >= 0) {
        this.users.splice(index, 1);
      }
    },
    addUserMembership(user) {
      if (!this.users.find(u => u.id === user.id)) {
        this.users.unshift(user);
      }
    },
    async saveUsers() {
      if (this.saving) {
        return;
      }
      this.saving = true;
      try {
        let promiseFunction = null;
        if (this.role === 'manager') {
          promiseFunction = this.$spaceService.promoteManager;
        } else if (this.role === 'redactor') {
          promiseFunction = this.$spaceService.promoteRedactor;
        } else if (this.role === 'publisher') {
          promiseFunction = this.$spaceService.promotePublisher;
        } else {
          throw new Error(`Role ${this.role} isn't managed`);
        }
        for (const i in this.users) {
          // eslint-disable-next-line no-await-in-loop
          await promiseFunction(this.$root.space.id, this.users[i].username);
        }
        this.$root.$emit('alert-message', this.$t(`SpaceSettings.roles.${this.role}sUpdatedSuccessfully`), 'success');
        this.$refs.drawer.close();
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('SpaceSettings.error.unknownErrorWhenSavingRoles'), 'error');
      } finally {
        this.$root.$emit(`space-settings-${this.role}s-updated`, this.users);
        this.saving = false;
      }
    },
    async getAllUsers() {
      let limit = 50;
      let data;
      do {
        limit *= 2;
        // eslint-disable-next-line no-await-in-loop
        data = await this.$spaceService.getSpaceMemberships({
          space: this.$root.space.id,
          status: this.role,
          expand: 'users',
          offset: 0,
          limit,
        });
      } while (limit < 1000
          && data?.spacesMemberships?.length
          && data?.spacesMemberships?.length === limit);
      return data?.spacesMemberships?.map?.(r => r.user);
    },
  },
};
</script>