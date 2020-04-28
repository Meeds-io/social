<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
contact@meeds.io
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
    ref="overviewDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template slot="title">
      {{ title }}
    </template>
    <template slot="content">
      <v-layout column class="ma-3">
        <template v-if="users && users.length" class="ma-0 border-box-sizing">
          <people-overview-people-list
            :users="users"
            :filter="filter"
            @refresh="refresh()" />
        </template>
        <template v-else-if="!loadingUsers">
          <span class="ma-auto">{{ $t('peopleOverview.label.noResults') }}</span>
        </template>
        <v-card-actions class="flex-grow-1 justify-center my-2">
          <v-spacer />
          <v-btn
            v-if="canShowMore"
            :loading="loadingUsers"
            :disabled="loadingUsers"
            class="loadMoreButton ma-auto btn"
            @click="loadNextPage">
            {{ $t('peopleOverview.label.showMore') }}
          </v-btn>
          <v-spacer />
        </v-card-actions>
      </v-layout>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data: () => ({
    title: null,
    filter: null,
    fieldsToRetrieve: 'all,connectionsInCommonCount',
    loadingUsers: false,
    offset: 0,
    pageSize: 20,
    users: [],
  }),
  computed:{
    canShowMore() {
      return this.loadingUsers || this.users.length >= this.limit;
    },
    searchUsersMethod() {
      return this.filter === 'invitations' ? this.$userService.getInvitations : this.$userService.getPending;
    },
  },
  methods: {
    refresh() {
      this.$emit('refresh');
      this.searchUsers();
    },
    searchUsers() {
      this.users = [];
      this.loadingUsers = true;
      return this.searchUsersMethod(this.offset, this.limit, this.fieldsToRetrieve)
        .then(data => {
          this.users = data && data.users || [];
          return this.$nextTick();
        })
        .finally(() => this.loadingUsers = false);
    },
    loadNextPage() {
      this.limit += this.pageSize;
      this.searchUsers();
    },
    open(filter, title) {
      this.title = title;
      this.limit = this.pageSize;
      this.filter = filter;
      this.$nextTick().then(() => {
        this.searchUsers();
        this.$refs.overviewDrawer.open();
      });
    },
  }
};
</script>