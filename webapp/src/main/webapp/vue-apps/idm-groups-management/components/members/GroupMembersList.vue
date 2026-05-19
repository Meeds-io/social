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
  <div id="groupMembersManagement" class="d-flex flex-column fill-height">
    <v-data-table
      class="elevation-0 border-radius"
      item-key="id"
      :headers="headers"
      :items="mappedMembers"
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
          {{ $t('UsersManagement.lastConnection.neverConnected') }}
        </div>
      </template>
      <template #[`item.userType`]="{ item }">
        <v-icon
          size="18"
          class="d-flex justify-center"
          :title="item.userRoleIconTitle">
          {{ item.userRoleIcon }}
        </v-icon>
      </template>
      <template #[`item.actions`]="{ item }">
        <group-members-action-menu :member="item" />
      </template>

      <template #no-data>
        <div class="d-flex flex-column align-center justify-center py-8">
          {{ $t('groupsManagement.members.emptyTitle') }}
        </div>
      </template>
    </v-data-table>
  </div>
</template>

<script>
export default {
  data: () => ({
    members: [],
    loading: false,
    totalSize: 0,
    page: 1,
    itemsPerPage: 20,
    footerProps: {
      itemsPerPageOptions: [20, 50, 100],
    },
    fullDateFormat: {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    },
  }),
  props: {
    keyword: {
      type: String,
      default: '',
    },
    filter: {
      type: Object,
      default: null,
    },
  },
  computed: {
    group() {
      return this.$root?.selectedGroup;
    },
    headers() {
      return [
        {
          text: this.$t('groupsManagement.members.fullName'),
          value: 'fullname',
          sortable: false,
        },
        {
          text: this.$t('groupsManagement.members.email'),
          value: 'email',
          sortable: false,
        },
        {
          text: this.$t('groupsManagement.members.lastConnection'),
          value: 'lastLoginTime',
          sortable: false,
        },
        {
          text: this.$t('groupsManagement.members.userType'),
          value: 'userType',
          sortable: false,
          align: 'center'
        },
        {
          text: this.$t('groupsManagement.members.actions'),
          value: 'actions',
          align: 'center',
          sortable: false,
          width: '100px',
        },
      ];
    },
    mappedMembers() {
      return this.members.map(member => ({
        ...member,
        lastLoginTime: member?.lastLoginTime
          ? Number(member.lastLoginTime)
          : null,
        userRoleIcon: this.getIcon(member),
        userRoleIconTitle: this.getIconTitle(member),
      }));
    },
    isEmpty() {
      return !this.members.length;
    },
  },
  watch: {
    keyword() {
      this.resetPagination();
      this.searchGroupMembers();
    },
    filter: {
      deep: true,
      handler() {
        this.resetPagination();
        this.searchGroupMembers();
      },
    },
    group() {
      this.resetPagination();
      this.searchGroupMembers();
    },
    page() {
      this.searchGroupMembers();
    },
  },
  created() {
    this.searchGroupMembers();
    this.$root.$on('refresh-group-members', this.refreshMembers);
  },
  beforeDestroy() {
    this.$root.$off('refresh-group-members', this.refreshMembers);
  },
  methods: {
    resetPagination() {
      this.page = 1;
    },
    handleItemsPerPageChange(value) {
      this.itemsPerPage = value;
      this.resetPagination();
      this.searchGroupMembers();
    },
    async searchGroupMembers() {
      if (!this.group?.id) {
        return;
      }
      this.loading = true;
      try {
        const limit = this.itemsPerPage;
        const offset = (this.page - 1) * limit;
        const data =
            await this.$groupMembersService.getGroupMembers(
              this.group.id,
              this.keyword,
              this.filter,
              offset,
              limit
            ) || {};
        this.members = data?.users || data?.entities || [];
        this.totalSize = data?.size || 0;
      } finally {
        this.loading = false;
      }
    },
    refreshMembers() {
      this.page = 1;
      this.searchGroupMembers();
    },
    getIcon(member) {
      if (member?.isManager) {
        return 'fa-user-cog';
      }
      if (member?.isPublisher) {
        return 'fa-user-tag';
      }
      if (member?.isRedactor) {
        return 'fa-user-edit';
      }
      return 'fa-user';
    },
    getIconTitle(member) {
      if (member?.isManager) {
        return this.$t('groupsManagement.members.adminRole');
      }
      if (member?.isPublisher) {
        return this.$t('groupsManagement.members.publisherRole');
      }
      if (member?.isRedactor) {
        return this.$t('groupsManagement.members.redactorRole');
      }
      return this.$t('groupsManagement.members.memberRole');
    },
  },
};
</script>
