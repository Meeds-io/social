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
  <v-card class="application-body" flat>
    <group-members-management-toolbar
      disabled-users
      :filter="filter"
      @keyword-change="keyword = $event"
      @open-group-members-advanced-filter="openGroupMembersAdvancedFilter" />
    <group-members-list :keyword="keyword" :filter="filter" />
    <group-members-filter-drawer
      v-if="drawer"
      ref="groupMembersAdvancedFilter"
      @closed="handleDrawerClosed"
      @apply-advanced-filter="applyAdvancedFilter" />
    <group-members-membership-drawer />
    <group-management-membership-form-drawer />
  </v-card>
</template>
<script>
export default {
  data() {
    return {
      filter: null,
      keyword: null,
      members: [],
      totalSize: 0,
      exportUsersUrl: null,
      loading: false,
      drawer: false,
      page: 1,
      itemsPerPage: 2,
    };
  },
  methods: {
    async openGroupMembersAdvancedFilter() {
      this.drawer = true;
      await this.$nextTick();
      this.$refs?.groupMembersAdvancedFilter.open(this.filter);
    },
    applyAdvancedFilter(filter) {
      this.filter = filter;
    },
    async handleDrawerClosed() {
      this.drawer = false;
      return await this.$nextTick();
    },
  }
};
</script>

