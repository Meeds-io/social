<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2026 Meeds Association contact@meeds.io

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
  <user-notification-template
    :notification="notification"
    :avatar-url="avatar"
    :message="message"
    :url="spaceUrl"
    space-avatar>
    <template #actions>
      <div>
        <div class="d-flex justify-start mb-2">
          <v-icon
            class="me-1"
            size="14"
            small>
            fas fa-door-open
          </v-icon>
          <span v-sanitized-html="actionMessage" />
        </div>
        <v-btn
          :href="spaceMembersLink"
          class="pa-1 btn btn-primary"
          link
          small
          outlined>
          {{ $t('Notification.JoinedSpaceByInvitationLinkPlugin.viewMembers') }}
        </v-btn>
      </div>
    </template>
  </user-notification-template>
</template>
<script>
export default {
  props: {
    notification: {
      type: Object,
      default: null,
    },
  },
  computed: {
    avatar() {
      return this.notification?.parameters?.spaceAvatarUrl;
    },
    spaceId() {
      return this.notification?.parameters?.spaceId;
    },
    invitedUserDisplayName() {
      return this.notification?.parameters?.invitedUser;
    },
    message() {
      return this.$t('Notification.title.JoinedSpaceByInvitationLinkPlugin', {
        0: `<a class="user-name font-weight-bold">${this.invitedUserDisplayName}</a>`
      });
    },
    actionMessage() {
      return this.$t('Notification.intranet.message.JoinedSpaceByInvitationLinkPlugin', {
        0: `<strong>${this.invitedUserDisplayName}</strong>`
      });
    },
    spaceUrl() {
      return `/portal/s/${this.spaceId}`;
    },
    spaceMembersLink() {
      return `${this.spaceUrl}/members`;
    }
  }
};
</script>
