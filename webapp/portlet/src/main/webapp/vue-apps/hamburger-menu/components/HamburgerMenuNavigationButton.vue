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
  <a
    class="HamburgerNavigationMenuLink flex border-box-sizing"
    @click="$emit('open-drawer')">
    <div class="px-5 py-3">
      <v-icon size="24">fa-bars</v-icon>
    </div>
    <v-btn
      v-show="showBadge"
      class="hamburger-unread-badge"
      absolute
      icon
      height="16"
      width="16"
      text>
      <div class="hamburger-unread-badge error-color-background"></div>
    </v-btn>
  </a>
</template>
<script>
export default {
  props: {
    unreadPerSpace: {
      type: Array,
      default: () => ([])
    }
  },
  computed: {
    showBadge() {
      return this.unreadPerSpace?.length;
    }
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
      let unreadSpaceItem = {};
      if (spaceId && this.unreadPerSpace?.length) {
        unreadSpaceItem = this.unreadPerSpace.find(item => Number(item.spaceId) === Number(spaceId));
      }
      if (wsEventName === 'notification.unread.item') {
        if (unreadSpaceItem?.spaceId ) {
          unreadSpaceItem.unredItem ++;
        } else {
          this.unreadPerSpace.push({
            'spaceId': spaceId, 
            'unredItem': 1
          });
        }
      } else if (wsEventName === 'notification.read.item') {
        if (unreadSpaceItem?.spaceId ) {
          unreadSpaceItem.unredItem --;
          if (unreadSpaceItem.unredItem <= 0 ) {
            this.unreadPerSpace.splice(this.unreadPerSpace.indexOf(unreadSpaceItem), 1);
          }
        }  
      }  else if (wsEventName === 'notification.read.allItems') {
        if (unreadSpaceItem?.spaceId ) {
          this.unreadPerSpace.splice(this.unreadPerSpace.indexOf(unreadSpaceItem), 1);
        }
      } 
    },
  }
};
</script>
