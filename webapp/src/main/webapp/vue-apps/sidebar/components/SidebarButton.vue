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
  <v-btn
    v-if="!$root.sticky"
    :title="$t('menu.spaces.openSidebarTooltip')"
    class="HamburgerNavigationMenuLink border-box-sizing"
    height="56"
    width="69"
    text
    @click="$emit('open-drawer', $event)">
    <v-icon v-show="$root.hidden" size="20">fa-bars</v-icon>
    <div
      v-show="showBadge"
      :class="$vuetify.rtl && 'l-0' || 'r-0'"
      class="hamburger-unread-badge position-absolute">
      <div class="hamburger-unread-badge error-color-background"></div>
    </div>
  </v-btn>
</template>
<script>
export default {
  computed: {
    showBadge() {
      return this.$root.unreadPerSpace
        && this.$root.hidden
        && Object.values(this.$root.unreadPerSpace).reduce((sum, v) => sum += v, 0) > 0;
    },
  },
  mounted() {
    document.addEventListener('notification.unread.item', this.handleUpdatesFromWebSocket);
    document.addEventListener('notification.read.item', this.handleUpdatesFromWebSocket);
    document.addEventListener('notification.read.allItems', this.handleUpdatesFromWebSocket);
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
      let spaceWebNotificationItem = data?.message?.spaceWebNotificationItem || data?.message?.spacewebnotificationitem;
      if (spaceWebNotificationItem?.length) {
        spaceWebNotificationItem = JSON.parse(spaceWebNotificationItem);
      }
      const spaceId = spaceWebNotificationItem?.spaceId;
      if (wsEventName === 'notification.unread.item') {
        if (spaceId && this.$root.unreadPerSpace[spaceId]) {
          this.$root.unreadPerSpace[spaceId]++;
        } else {
          this.$set(this.$root.unreadPerSpace, spaceId, 1);
        }
      }  else if (wsEventName === 'notification.read.item') {
        if (spaceId && this.$root.unreadPerSpace[spaceId] > 0) {
          this.$root.unreadPerSpace[spaceId]--;
        }
      } else if (wsEventName === 'notification.read.allItems') {
        if (spaceId && this.$root.unreadPerSpace[spaceId] > 0) {
          this.$root.unreadPerSpace[spaceId] = 0;
        }
      }
    },
  }
};
</script>
