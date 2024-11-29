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
  <div class="px-5">
    <template v-if="$root.isExternalFeatureEnabled">
      <v-list
        class="py-0"
        max-width="100%">
        <v-list-item
          id="InvitePlatformUserToSpaceButton"
          class="pa-0"
          link
          @click="$root.$emit('space-form-invite-member', true)">
          <v-list-item-content class="d-inline">
            <v-list-item-title>{{ $t('SpaceSettings.users.button.inviteInternalMembers') }}</v-list-item-title>
            <v-list-item-subtitle class="text-truncate-3 text-wrap">{{ $t('SpaceSettings.users.button.inviteInternalMembers.description') }}</v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-avatar
            v-if="invitedMembers?.length"
            min-width="fit-content">
            <v-avatar
              class="error-color-background white--text pa-1"
              width="auto"
              height="24"
              min-width="24"
              min-height="24">
              {{ invitedMembersSize }}
            </v-avatar>
          </v-list-item-avatar>
        </v-list-item>
        <v-list-item
          id="InviteUserByEmailToSpaceButton"
          class="pa-0"
          link
          @click="$root.$emit('space-form-invite-email', true)">
          <v-list-item-content class="d-inline">
            <v-list-item-title>{{ $t('SpaceSettings.users.button.inviteByEmail') }}</v-list-item-title>
            <v-list-item-subtitle class="text-truncate-3 text-wrap">{{ $t('SpaceSettings.users.button.inviteByEmail.description') }}</v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-avatar
            v-if="externalInvitedUsers?.length"
            min-width="fit-content">
            <v-avatar
              class="error-color-background white--text pa-1"
              width="auto"
              height="24"
              min-width="24"
              min-height="24">
              {{ externalInvitedUsersSize }}
            </v-avatar>
          </v-list-item-avatar>
        </v-list-item>
      </v-list>
      <space-form-invite-users-drawer
        v-model="invitedMembers" />
      <space-form-invite-email-drawer
        v-model="externalInvitedUsers"
        @update-members="updateMembers" />
    </template>
    <space-form-invite-users-input
      v-else
      v-model="invitedMembers" />
  </div>
</template>
<script>
export default {
  data: () => ({
    invitedMembers: [],
    externalInvitedUsers: [],
  }),
  computed: {
    invitedMembersSize() {
      return this.invitedMembers?.length ? this.invitedMembers.reduce((sum, v) => sum += v?.profile?.membersCount || 1, 0) : 0;
    },
    externalInvitedUsersSize() {
      return this.externalInvitedUsers?.length;
    },
  },
  watch: {
    invitedMembers() {
      this.$emit('invited-members', this.invitedMembers);
    },
    externalInvitedUsers() {
      this.$emit('invited-email', this.externalInvitedUsers);
    },
  },
  methods: {
    updateMembers(invitedMembers) {
      invitedMembers = invitedMembers.filter(m => !this.invitedMembers.find(m2 => m.id === m2.id));
      this.invitedMembers.push(...invitedMembers);
    },
  },
};
</script>