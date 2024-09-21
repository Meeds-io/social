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
  <application-toolbar
    id="spaceSettingUsersListToolbar"
    :right-text-filter="hasUsers && {
      minCharacters: 3,
      placeholder: $t('SpaceSettings.users.filterByName'),
      tooltip: $t('SpaceSettings.users.filterByName')
    }"
    no-text-truncate
    compact
    @filter-text-input-end-typing="query = $event"
    @loading="$emit('loading', $event)">
    <template #left>
      <div class="d-flex align-center">
        <space-invite-buttons-group
          v-if="role === 'member'" />
        <v-btn
          v-else
          :id="`${role}AddUser`"
          :title="$t('SpaceSettings.users.add')"
          color="primary"
          elevation="0"
          @click="$emit('add')">
          <v-icon
            color="while"
            class="me-2"
            size="18">
            fa-plus
          </v-icon>
          {{ $t('SpaceSettings.users.add') }}
        </v-btn>
      </div>
    </template>
  </application-toolbar>
</template>
<script>
export default {
  props: {
    hasUsers: {
      type: Boolean,
      default: null,
    },
    role: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    query: null,
  }),
  watch: {
    query() {
      this.$emit('query', this.query);
    },
  },
};
</script>

