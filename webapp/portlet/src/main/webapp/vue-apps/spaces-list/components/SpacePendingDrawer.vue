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
    id="spacePendingUsersDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="loading"
    :right="!$vuetify.rtl"
    allow-expand
    no-x-scroll>
    <template #title>
      {{ $t('spacesList.pending.drawer.title') }}
    </template>
    <template v-if="drawer" #content>
      <div v-if="memberships?.length" class="pa-5">
        <span class="mb-2">
          {{ $t('spacesList.label.pendingRequests.description') }}
        </span>
        <spaces-role-list
          :memberships="memberships"
          approve-button
          display-date
          @approve="acceptUserRequest"
          @remove="denyUserRequest" />
      </div>
      <div v-else-if="!loading" class="d-flex flex-column align-center justify-center py-8">
        <v-icon size="54" color="secondary">fa-history</v-icon>
        <span class="my-4">{{ $t('spacesList.pending.placeholder.emptyRequests') }}</span>
        <v-btn
          class="btn"
          elevation="0"
          @click="close">
          {{ $t('spacesList.pending.placeholder.close') }}
        </v-btn>
      </div>
    </template>
    <template v-if="hasMore" #footer>
      <div class="d-flex">
        <v-btn
          :loading="loading"
          class="btn me-2"
          block
          @click="loadMore">
          <template>
            {{ $t('spacesList.pending.loadMore') }}
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
    filterType: 'requests',
    pageSize: 0,
    limit: 0,
    memberSpaces: null,
    visitedSpaces: null,
    activeSpaces: null,
    hasMore: false,
    memberships: null,
    page: 0,
    size: 0,
    pendingUsersCount: 0,
  }),
  watch: {
    filterType() {
      this.reset();
      this.refreshMemberships();
    },
  },
  created() {
    this.$root.$on('space-list-pending-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('space-list-pending-open', this.open);
  },
  methods: {
    open(pendingUsersCount, filterType) {
      this.filterType = filterType || 'requests';
      this.pendingUsersCount = pendingUsersCount;
      this.reset();
      this.getSpaceMemberships();
      this.$refs.drawer.open();
    },
    reset() {
      this.pageSize = parseInt((window.innerHeight - 180) / 56);
      this.page = 0;
      this.size = 0;
      this.memberships = [];
      this.loading = false;
      this.hasMore = false;
    },
    refreshMemberships() {
      if (this.drawer) {
        this.getSpaceMemberships(true);
      }
    },
    loadMore() {
      this.page++;
      this.getSpaceMemberships();
    },
    async getSpaceMemberships(reset) {
      this.loading = true;
      try {
        const data = await this.$spaceService.getSpaces(
          null,
          reset ? 0 : this.page * this.pageSize,
          reset ? (this.page + 1) * this.pageSize + 1 : this.pageSize + 1,
          this.filterType);
        const memberships = data?.spaces?.filter?.(space => space?.pending?.length)?.
          flatMap?.(space => space.pending.map(user => ({
            user,
            space,
          })));
        if (memberships?.length) {
          if (reset) {
            this.memberships = memberships.slice(0, this.pageSize);
          } else {
            this.memberships.push(...memberships.slice(0, this.pageSize));
          }
          this.hasMore = memberships.length > this.pageSize;
        } else {
          if (reset) {
            this.memberships = [];
          }
          this.hasMore = false;
        }
      } finally {
        this.loading = false;
      }
    },
    close() {
      this.$refs.drawer.close();
    },
    async acceptUserRequest(membership) {
      this.loading = true;
      try {
        await this.$spaceService.acceptUserRequest(membership.space.id, membership.user.username);
        this.memberships.splice(this.memberships.indexOf(membership), 1);
        this.$root.$emit('alert-message', this.$t('spacesList.pending.userAddedAsSpaceMember'), 'success');
        this.$root.$emit('space-list-pending-updated');
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('spacesList.pending.error.unknownErrorWhenSavingRoles'), 'error');
      } finally {
        this.loading = false;
      }
    },
    async denyUserRequest(membership) {
      this.loading = true;
      try {
        await this.$spaceService.refuseUserRequest(membership.space.id, membership.user.username);
        this.memberships.splice(this.memberships.indexOf(membership), 1);
        this.$root.$emit('alert-message', this.$t('spacesList.pending.userRequestDenied'), 'success');
        this.$root.$emit('space-list-pending-updated');
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('spacesList.pending.error.unknownErrorWhenSavingRoles'), 'error');
      } finally {
        this.loading = false;
      }
    },
  }
};
</script>
