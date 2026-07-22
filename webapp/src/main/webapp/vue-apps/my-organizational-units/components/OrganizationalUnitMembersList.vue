<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
  <v-data-table
    class="elevation-0 border-radius"
    item-key="id"
    :headers="headers"
    :items="users"
    :loading="loading"
    :items-per-page="itemsPerPage"
    :page.sync="page"
    :server-items-length="totalSize"
    :footer-props="footerProps"
    :hide-default-footer="isEmpty"
    @update:items-per-page="handleItemsPerPageChange">
    <template #[`item.lastLoginTime`]="{ item }">
      <div v-if="item.lastLoginTime">
        <date-format
          :value="item.lastLoginTime"
          :format="fullDateFormat"
          class="grey--text me-1" />
      </div>
      <div v-else class="grey--text">
        {{ $t('organizationalUnitMembers.neverConnected') }}
      </div>
    </template>
    <template #[`item.actions`]>
      <v-btn icon disabled>
        <v-icon size="18">fa-ellipsis-v</v-icon>
      </v-btn>
    </template>
    <template #no-data>
      <div class="d-flex flex-column align-center justify-center py-8">
        {{ $t('organizationalUnitMembers.emptyTitle') }}
      </div>
    </template>
  </v-data-table>
</template>
<script>
export default {
  props: {
    groupId: {
      type: String,
      default: null,
    },
    keyword: {
      type: String,
      default: '',
    },
  },
  data: () => ({
    users: [],
    loading: false,
    totalSize: 0,
    page: 1,
    itemsPerPage: 20,
    footerProps: {
      itemsPerPageOptions: [20, 50, 100],
    },
    fullDateFormat: {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    },
  }),
  computed: {
    headers() {
      return [
        {
          text: this.$t('organizationalUnitMembers.fullName'),
          value: 'fullname',
          sortable: false,
        },
        {
          text: this.$t('organizationalUnitMembers.email'),
          value: 'email',
          align: 'center',
          sortable: false,
        },
        {
          text: this.$t('organizationalUnitMembers.lastConnection'),
          value: 'lastLoginTime',
          align: 'center',
          sortable: false,
        },
        {
          text: this.$t('organizationalUnitMembers.actions'),
          value: 'actions',
          align: 'center',
          sortable: false,
          width: '100px',
        },
      ];
    },
    isEmpty() {
      return !this.users.length;
    },
  },
  watch: {
    keyword() {
      this.resetPagination();
      this.searchUsers();
    },
    groupId() {
      this.resetPagination();
      this.searchUsers();
    },
    page() {
      this.searchUsers();
    },
  },
  created() {
    this.searchUsers();
  },
  methods: {
    resetPagination() {
      this.page = 1;
    },
    handleItemsPerPageChange(value) {
      this.itemsPerPage = value;
      this.resetPagination();
      this.searchUsers();
    },
    async searchUsers() {
      if (!this.groupId) {
        return;
      }
      this.loading = true;
      try {
        const limit = this.itemsPerPage;
        const offset = (this.page - 1) * limit;
        const data = await this.$organizationalUnitMembersService.getOrganizationalUnitMembers(
          this.groupId,
          this.keyword,
          offset,
          limit
        ) || {};
        this.users = data?.users || [];
        this.totalSize = data?.size || 0;
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>
