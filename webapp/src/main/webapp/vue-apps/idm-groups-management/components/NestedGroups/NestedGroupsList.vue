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
    :items="mappedGroups"
    :loading="loading"
    :items-per-page="itemsPerPage"
    :page.sync="page"
    :server-items-length="totalSize"
    :footer-props="footerProps"
    :hide-default-footer="isEmpty"
    @update:items-per-page="handleItemsPerPageChange">
    <template #[`item.usersCount`]="{ item }">
      <group-members-count :group="item" />
    </template>
    <template #no-data>
      <div class="d-flex flex-column align-center justify-center py-8">
        {{ $t('groupsManagement.nestedGroups.emptyTitle') }}
      </div>
    </template>
  </v-data-table>
</template>

<script>
export default {
  data: () => ({
    nestedGroups: [],
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
          text: this.$t('GroupsManagement.name'),
          value: 'label',
          sortable: false,
        },
        {
          text: this.$t('GroupsManagement.description'),
          value: 'description',
          align: 'center',
          sortable: false,
        },
        {
          text: this.$t('groupsManagement.users'),
          value: 'usersCount',
          align: 'center',
          sortable: false,
          width: '100px',
        },
      ];
    },
    isEmpty() {
      return !this.nestedGroups?.length;
    },
    mappedGroups() {
      let groups = this.nestedGroups.map(group => ({
        ...group,
        description: group?.description || '-',
      }));
      if (this.keyword && this.keyword.trim() !== '') {
        const searchKeyword = this.keyword.toLowerCase();
        groups = groups.filter(group => {
          const matchesLabel = group?.label?.toLowerCase()?.includes(searchKeyword);
          const matchesDescription = group?.description?.toLowerCase()?.includes(searchKeyword);
          return matchesLabel || matchesDescription;
        });
      }
      return groups;
    },
  },
  watch: {
    group() {
      this.resetPagination();
      this.getNestedGroups();
    },
    page() {
      this.getNestedGroups();
    },
  },
  created() {
    this.getNestedGroups();
    this.$root.$on('refreshGroup', this.refreshNestedGroups);
  },
  beforeDestroy() {
    this.$root.$off('refreshGroup', this.refreshNestedGroups);
  },
  methods: {
    resetPagination() {
      this.page = 1;
    },
    handleItemsPerPageChange(value) {
      this.itemsPerPage = value;
      this.resetPagination();
      this.getNestedGroups();
    },
    async getNestedGroups() {
      if (!this.group?.id) {
        return;
      }
      this.loading = true;
      try {
        const limit = this.itemsPerPage;
        const offset = (this.page - 1) * limit;
        const data = await this.$groupService.getNestedGroups(this.group.id, limit, offset);
        this.nestedGroups = data.entities;
        this.totalSize = data.size;
      } finally {
        this.loading = false;
      }
    },
    refreshNestedGroups(group, parentGroup) {
      if (parentGroup?.id === this.group.id) {
        this.getNestedGroups();
      }
    }
  },
};
</script>
