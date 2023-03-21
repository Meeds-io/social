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
  <ripple-hover-button
    class="HamburgerNavigationMenuLink flex full-height pa-0 border-box-sizing"
    text
    @ripple-hover="$emit('open-drawer', $event)">
    <div class="px-5 py-3">
      <v-icon size="24">fa-bars</v-icon>
    </div>
    <div
      v-show="showBadge"
      class="hamburger-unread-badge position-absolute"
      absolute
      icon
      height="16"
      width="16"
      text>
      <div class="hamburger-unread-badge error-color-background"></div>
    </div>
  </ripple-hover-button>
</template>
<script>
export default {
  props: {
    unreadPerSpace: {
      type: Object,
      default: () => ({})
    }
  },
  data: () => ({
    unread: {},
  }),
  computed: {
    showBadge() {
      return this.unread && Object.values(this.unread).reduce((sum, v) => sum += v, 0) > 0;
    }
  },
  watch: {
    unreadPerSpace() {
      this.initUnread();
    },
  },
  created() {
    this.initUnread();
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
        if (spaceId && this.unread && this.unread[spaceId]) {
          this.unread[spaceId] ++;
        } else {
          this.$set(this.unread, spaceId, 1);
        }
      }  else if (wsEventName === 'notification.read.item') {
        if (spaceId && this.unread && this.unread[spaceId] > 0) {
          this.unread[spaceId] --;
        }
      } else if (wsEventName === 'notification.read.allItems') {
        if (spaceId && this.unread && this.unread[spaceId] > 0) {
          this.unread[spaceId] = 0;
        }
      }
    },
    initUnread() {
      this.unread = this.unreadPerSpace && Object.assign({}, this.unreadPerSpace) || {};
    },
  }
};
</script>
