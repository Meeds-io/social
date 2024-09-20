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
      <v-btn
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
      <v-badge
        v-if="pendingButton && pendingCount"
        top
        color="white pa-0"
        class="mailBadge"
        offset-x="8"
        offset-y="12">
        <span slot="badge">
          <v-avatar
            v-show="pendingCount"
            color="secondary"
            height="20"
            min-width="20"
            width="auto"
            dark>
            {{ pendingCount }}
          </v-avatar>
        </span>
        <v-btn
          :title="$t('SpaceSettings.users.pendingMembers')"
          class="ms-2"
          height="35"
          width="35"
          icon
          @click="$root.$emit('space-settings-users-pending-list-open')">
          <v-icon size="22">
            fa-history
          </v-icon>
          <template #badge>
          </template>
        </v-btn>
      </v-badge>
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
    pendingButton: {
      type: Boolean,
      default: null,
    },
  },
  data: () => ({
    query: null,
  }),
  computed: {
    pendingCount() {
      return (this.$root?.space?.pendingUsersCount || 0)
        + (this.$root?.space?.invitedUsersCount || 0)
        + (this.$root?.externalInvitations?.length || 0);
    },
  },
  watch: {
    query() {
      this.$emit('query', this.query);
    },
  },
};
</script>

