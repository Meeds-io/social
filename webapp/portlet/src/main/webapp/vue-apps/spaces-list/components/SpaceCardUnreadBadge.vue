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
  <v-tooltip
    v-if="hasUnreadBadge"
    :disabled="$root.isMobile"
    top>
    <template #activator="{on, attrs}">
      <v-card
        v-bind="attrs"
        v-on="on"
        :class="$vuetify.rtl && 'l-0' || 'r-0'"
        class="unread-badge border-radius-circle error-color-background position-absolute z-index-two mt-n2 me-n2"
        min-width="16"
        min-height="16"
        width="16"
        height="16"
        flat />
    </template>
    <span>{{ $t('spacesList.button.unreadActivities') }}</span>
  </v-tooltip>
</template>
<script>
export default {
  props: {
    space: {
      type: Object,
      default: null,
    }
  },
  data: () => ({
    unread: null,
  }),
  computed: {
    spaceId() {
      return this.space?.id;
    },
    hasUnreadBadge() {
      return this.unread === null ? !!this.space.unread : this.unread;
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
      if (wsEventName === 'notification.unread.item' && Number(spaceId) === Number(this.spaceId)) {
        this.unread = true;
      }  else if (wsEventName === 'notification.read.item' && Number(spaceId) === Number(this.spaceId)) {
        this.unread = false;
      }  else if (wsEventName === 'notification.read.allItems') {
        this.unread = false;
      }
    }
  }
};
</script>
