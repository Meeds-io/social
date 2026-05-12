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
  <application-toolbar
    id="groupMembersManagementToolbar"
    :right-text-filter="status === 'ENABLED' && {
      minCharacters: 1,
      placeholder: $t('groupsManagement.members.filterBy'),
      tooltip: $t('groupsManagement.members.filterBy'),
    }"
    :right-filter-button="{
      hide: false,
      text: $t('groupsManagement.members.filterBy')
    }"
    :filters-count="filtersCount"
    compact
    @filter-button-click="$emit('open-group-members-advanced-filter')"
    @filter-text-input-end-typing="$emit('keyword-change', $event)">
    <template #left>
      <div class="d-flex position-absolute zindex-1 mt-n1 t-0">
        <v-btn
          :width="$root.isMobile && 36 || 'auto'"
          :class="$root.isMobile && 'border-box-sizing'"
          class="btn btn-primary"
          @click="$root.$emit('addNewMembership', $root.selectedGroup)">
          <span class="d-none d-sm-inline">
            {{ $t('GroupsManagement.addGroup') }}
          </span>
        </v-btn>
        <v-btn
          v-if="!$root.isMobile"
          class="ms-2"
          color="primary"
          elevation="0"
          outlined
          :loading="loading"
          @click="$emit('export-users')">
          <v-icon class="me-2" size="18">fa-file-excel</v-icon>
          {{ $t('UsersManagement.selection.export') }}
        </v-btn>
      </div>
    </template>
  </application-toolbar>
</template>
<script>
export default {
  props: {
    loading: {
      type: Boolean,
      default: () => false,
    },
    filter: {
      type: Object,
      default: () => ({}),
    },
  },
  data: () => ({
    keyword: null,
  }),
  computed: {
    status() {
      return this.filter?.status || 'ENABLED';
    },
    filtersCount() {
      return (this.status !== 'ENABLED' ? 1 : 0)
          + (this.filter?.type ? 1 : 0)
          + (this.filter?.connectionStatus ? 1 : 0)
          + (this.filter?.enrollmentStatus ? 1 : 0);
    },
  },
};
</script>
