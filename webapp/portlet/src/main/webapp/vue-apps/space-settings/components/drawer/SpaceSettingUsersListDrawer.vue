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
    id="spaceSettingsUsersDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="loading"
    allow-expand
    right
    @closed="$emit('closed')">
    <template #title>
      {{ $t(`SpaceSettings.users.list.drawer.${role}.title`) }}
    </template>
    <template v-if="drawer" #content>
      <div class="pa-4">
        {{ $t(`SpaceSettings.users.list.drawer.${role}.description`) }}
      </div>
      <space-setting-users-list-toolbar
        :pending-button="role === 'member'"
        :pending-count="pendingCount"
        :has-users="hasUsers"
        :role="role"
        @add="$root.$emit('space-settings-user-add', role)"
        @query="query = $event"
        @loading="loading = $event" />
      <div class="px-4 pb-4">
        <space-setting-role-list
          :users="users"
          @remove="removeUserMembership" />
        <div v-if="query && !users.length && !loading" class="d-flex align-center justify-center pa-5">
          {{ $t('SpaceSettings.users.noSearchResult') }}
        </div>
      </div>
      <confirm-dialog
        v-if="userToDelete"
        ref="confirmDialog"
        :title="$t(`SpaceSettings.${role}.delete.title`)"
        :message="$t(`SpaceSettings.${role}.delete.message`, {
          0: userToDelete.fullname,
        })"
        :ok-label="$t('SpaceSettings.yes')"
        :cancel-label="$t('SpaceSettings.no')"
        persistent
        @ok="removeUserMembershipEffectively"
        @cancel="userToDelete = null" />
    </template>
    <template v-if="hasMore" #footer>
      <div class="d-flex">
        <v-btn
          :loading="loading"
          class="btn me-2"
          block
          @click="loadMore">
          <template>
            {{ $t('SpaceSettings.button.loadMore') }}
          </template>
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
    hasMore: false,
    hasUsers: false,
    isLastAdmin: false,
    pendingCount: 0,
    pageSize: 20,
    page: 0,
    size: 0,
    role: null,
    users: null,
    query: null,
    userToDelete: null,
  }),
  watch: {
    query() {
      this.getSpaceMemberships(true, true);
    },
  },
  created() {
    this.$root.$on('space-settings-users-list-open', this.open);
    this.$root.$on('space-settings-managers-updated', this.refreshMemberships);
    this.$root.$on('space-settings-publishers-updated', this.refreshMemberships);
    this.$root.$on('space-settings-redactors-updated', this.refreshMemberships);
    this.$root.$on('space-settings-members-updated', this.refreshMemberships);
  },
  beforeDestroy() {
    this.$root.$off('space-settings-users-list-open', this.open);
    this.$root.$off('space-settings-managers-updated', this.refreshMemberships);
    this.$root.$off('space-settings-publishers-updated', this.refreshMemberships);
    this.$root.$off('space-settings-redactors-updated', this.refreshMemberships);
    this.$root.$off('space-settings-members-updated', this.refreshMemberships);
  },
  methods: {
    open(role) {
      if (this.role !== role) {
        this.role = role;
        this.reset();
        this.getSpaceMemberships();
      }
      this.$refs.drawer.open();
    },
    reset() {
      this.pageSize = parseInt((window.innerHeight - 180) / 56);
      this.page = 0;
      this.size = 0;
      this.users = [];
      this.userToDelete = null;
      this.isLastAdmin = false;
      this.hasUsers = false;
      this.loading = false;
      this.hasMore = false;
      this.query = null;
    },
    loadMore() {
      this.page++;
      this.getSpaceMemberships();
    },
    refreshMemberships() {
      if (this.drawer) {
        this.getSpaceMemberships(true);
      }
    },
    async getSpaceMemberships(resetUsers, resetPage) {
      this.loading = true;
      try {
        if (resetPage) {
          this.page = 0;
        }
        const data = await this.$spaceService.getSpaceMemberships({
          space: eXo.env.portal.spaceId,
          offset: resetUsers && !resetPage ? 0 : this.page * this.pageSize,
          limit: resetUsers && !resetPage ? (this.page + 1) * this.pageSize + 1 : this.pageSize + 1,
          status: this.role,
          query: this.query,
          returnSize: false,
          expand: this.$root.space.canEdit && 'users,createdDate' || 'users',
        });
        const users = data?.spacesMemberships;
        if (users?.length) {
          if (resetUsers) {
            this.users = users.slice(0, this.pageSize).map(m => m?.user).filter(u => u);
          } else {
            this.users.push(...users.slice(0, this.pageSize).map(m => m.user));
          }
          this.hasMore = users.length > this.pageSize;
        } else {
          if (resetUsers) {
            this.users = [];
          }
          this.hasMore = false;
        }
        if (!this.query?.length) {
          this.hasUsers = !!this.users?.length;
        }
        if (this.role === 'manager') {
          this.isLastAdmin = this.users?.length < 2;
        }
        if (this.role === 'redactor'
            && !this.users?.length) {
          this.$refs.drawer.close();
        }
      } finally {
        this.loading = false;
      }
    },
    close() {
      this.$refs.drawer.close();
    },
    async removeUserMembership(user) {
      this.userToDelete = user;
      await this.checkLastAdmin();
      if (!this.isLastAdmin) {
        this.$nextTick(() => this.$refs.confirmDialog.open());
      }
    },
    async removeUserMembershipEffectively() {
      if (this.loading) {
        return;
      } else {
        await this.checkLastAdmin();
        if (this.isLastAdmin) {
          return;
        }
      }
      this.loading = true;
      try {
        let promiseFunction = null;
        if (this.role === 'manager') {
          promiseFunction = this.$spaceService.removeManager;
        } else if (this.role === 'redactor') {
          promiseFunction = this.$spaceService.removeRedactor;
        } else if (this.role === 'publisher') {
          promiseFunction = this.$spaceService.removePublisher;
        } else if (this.role === 'member') {
          promiseFunction = this.$spaceService.removeMember;
        } else {
          throw new Error(`Role ${this.role} isn't managed`);
        }
        await promiseFunction(this.$root.space.id, this.userToDelete.username);
        this.$root.$emit('alert-message', this.$t(`SpaceSettings.roles.${this.role}RoleDeletedSuccessfully`), 'success');
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('SpaceSettings.error.unknownErrorWhenSavingRoles'), 'error');
      } finally {
        this.loading = false;
        this.$root.$emit(`space-settings-${this.role}s-updated`);
      }
    },
    async checkLastAdmin() {
      if (this.role === 'member') {
        await this.computeLastAdmin();
      }
      if (this.isLastAdmin) {
        this.$root.$emit('alert-message', this.$t('SpaceSettings.roles.atLeastOneManager.message'), 'warning');
      }
      return this.isLastAdmin;
    },
    async computeLastAdmin() {
      const data = await this.$spaceService.getSpaceMemberships({
        space: eXo.env.portal.spaceId,
        offset: 0,
        limit: 2,
        status: 'manager',
        expand: this.$root.space.canEdit && 'users,createdDate' || 'users',
        returnSize: false,
      });
      this.isLastAdmin = data?.spacesMemberships?.length < 2 && data?.spacesMemberships?.[0]?.user?.id === this.userToDelete?.id;
    },
  },
};
</script>