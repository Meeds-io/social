<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
  <v-list-item-action
    v-if="spaceUnreadBadge"
    class="my-auto align-center">
    <v-chip
      color="error-color-background"
      min-width="22"
      height="22"
      dark>
      {{ spaceUnreadBadge }}
    </v-chip>
  </v-list-item-action>
</template>
<script>
export default {
  props: {
    spaceId: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    spaceUnreadBadge: 0,
  }),
  created() {
    document.addEventListener('notification.unread.item', this.handleUpdatesFromWebSocket);
    document.addEventListener('notification.read.item', this.handleUpdatesFromWebSocket);
    document.addEventListener('notification.read.allItems', this.handleUpdatesFromWebSocket);
    this.spaceUnreadBadge = this.$root.unreadPerSpace[`${this.spaceId}`];
  },
  beforeDestroy() {
    document.removeEventListener('notification.unread.item', this.handleUpdatesFromWebSocket);
    document.removeEventListener('notification.read.item', this.handleUpdatesFromWebSocket);
    document.removeEventListener('notification.read.allItems', this.handleUpdatesFromWebSocket);
  },
  methods: {
    handleUpdatesFromWebSocket(event) {
      const data = event?.detail;
      const wsEventName = data?.wsEventName || '';
      let spaceWebNotificationItem = data?.message?.spaceWebNotificationItem
        || data?.message?.spacewebnotificationitem;
      if (spaceWebNotificationItem?.length) {
        spaceWebNotificationItem = JSON.parse(spaceWebNotificationItem);
      }
      const spaceId = spaceWebNotificationItem?.spaceId;
      if (wsEventName === 'notification.read.allItems') {
        this.spaceUnreadItems = 0;
      } else if (spaceId && Number(this.spaceId) === Number(spaceId)) {
        if (wsEventName === 'notification.unread.item') {
          this.spaceUnreadBadge++;
        } else if (wsEventName === 'notification.read.item') {
          this.spaceUnreadBadge--;
        }
      }
    },
  },
};
</script>
