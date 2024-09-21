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
  <div class="d-flex align-center">
    <space-invite-button />
    <v-badge
      v-if="pendingCount"
      color="white pa-0"
      offset-x="24"
      offset-y="20"
      top
      @click="$root.$emit('space-settings-users-pending-list-open')">
      <span slot="badge">
        <v-avatar
          v-show="pendingCount"
          color="secondary"
          min-height="16"
          min-width="16"
          height="auto"
          width="auto"
          class="pa-1 aspect-ratio-1 white--text content-box-sizing clickable"
          @click="$root.$emit('space-settings-users-pending-list-open')">
          {{ pendingCount }}
        </v-avatar>
      </span>
      <v-btn
        :title="$t('SpaceSettings.users.pendingMembers')"
        class="ms-2"
        icon
        @click="$root.$emit('space-settings-users-pending-list-open')">
        <v-icon size="22">
          fa-history
        </v-icon>
      </v-btn>
    </v-badge>
    <space-invite-pending-drawer
      ref="usersPendingManagementDrawer" />
    <space-invite-input-users-drawer
      ref="usersInvitationDrawer" />
    <space-invite-input-email-drawer
      v-if="$root.isExternalFeatureEnabled"
      ref="emailInvitationDrawer" />
  </div>
</template>
<script>
export default {
  computed: {
    pendingCount() {
      return (this.$root?.space?.pendingUsersCount || 0)
        + (this.$root?.space?.invitedUsersCount || 0)
        + (this.$root?.externalInvitations?.length || 0);
    },
  },
};
</script>

