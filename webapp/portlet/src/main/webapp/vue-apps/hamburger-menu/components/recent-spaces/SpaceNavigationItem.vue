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
  <v-list-item
    v-if="isMobile"
    :class="homeIcon && (homeLink === spaceLink && 'UserPageLinkHome' || 'UserPageLink')"
    class="px-2 spaceItem text-truncate"
    @click="openOrCloseDrawer()">
    <v-list-item-avatar 
      size="28"
      class="me-3 ms-3 tile my-0 spaceAvatar"
      tile>
      <v-img :src="spaceAvatar" />
    </v-list-item-avatar>
    <v-list-item-content>
      <v-list-item-title class="body-2" v-text="spaceDisplayName" />
    </v-list-item-content>
    <v-list-item-icon
      v-if="spaceUnreadCount"
      class="me-2 align-center">
      <v-chip
        v-if="spaceUnreadCount"
        color="error-color-background"
        min-width="22"
        height="22"
        dark>
        {{ spaceUnreadCount }}
      </v-chip>
    </v-list-item-icon>
  </v-list-item>
  <v-list-item
    v-else
    :href="spaceLink"
    :class="homeIcon && (homeLink === spaceLink && 'UserPageLinkHome' || 'UserPageLink')"
    link
    class="px-2 spaceItem"
    @mouseover="showItemActions = true" 
    @mouseleave="showItemActions = false">
    <v-badge
      :value="spaceUnreadCount && !expand"
      color="transparent">
      <template v-slot:badge>
        <v-btn class="error-color-background white--text ms-n5 mt-1" x-small icon>
          {{ spaceUnreadCount }}
        </v-btn>
      </template>
      <v-list-item-avatar 
        size="28"
        class="me-3 ms-2 tile my-0 spaceAvatar"
        tile>
      <v-img :src="spaceAvatar" />
      </v-list-item-avatar>
    </v-badge>
    <v-list-item-content v-if="expand">
      <v-list-item-title class="body-2" v-text="spaceDisplayName" />
    </v-list-item-content>
    <v-list-item-icon
      v-if="toggleArrow"
      :disabled="loading"
      :loading="loading"
      class="me-2 align-center">
      <v-btn
        icon
        @click="openOrCloseDrawer($event)">
        <v-icon
          :id="space.id"
          class="me-0 pa-2 icon-default-color clickable"
          small>
          {{ arrowIcon }} 
        </v-icon>
      </v-btn>
    </v-list-item-icon>
    <v-list-item-icon
      v-if="!toggleArrow && expand && spaceUnreadCount"
      class="me-2 align-center">
      <v-chip
        v-if="spaceUnreadCount"
        color="error-color-background"
        min-width="22"
        height="22"
        dark>
        {{ spaceUnreadCount }}
      </v-chip>
    </v-list-item-icon>
  </v-list-item>
</template>
<script>

export default {
  props: {
    space: {
      type: Object,
      default: null,
    },
    spaceUrl: {
      type: String,
      default: null
    },
    homeLink: {
      type: String,
      default: null,
    },
    homeIcon: {
      type: Boolean,
      default: false,
    },
    openedSpace: {
      type: Object,
      default: null,
    },
    thirdLevel: {
      type: Boolean,
      default: false,
    },
    expand: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    showItemActions: false,
    spaceUnreadItems: null,
    webSocketSpaceUnreadItems: {},
  }),
  computed: {
    spaceId() {
      return this.space?.id;
    },
    spaceLink() {
      return this.spaceUrl;
    },
    spaceAvatar() {
      return this.space?.avatarUrl;
    },
    spaceDisplayName() {
      return this.space?.displayName;
    },
    spaceUnreadCount() {
      return this.spaceUnreadItems && Object.values(this.spaceUnreadItems).reduce((sum, v) => sum += v, 0) || 0;
    },
    toggleArrow() {
      return this.showItemActions && this.expand;
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs';
    },
    arrowIcon() {
      return this.openedSpace?.id === this.space?.id && this.arrowIconLeft || this.arrowIconRight;
    },
    arrowIconLeft() {
      return this.$root.ltr && 'fa-arrow-left' || 'fa-arrow-right';
    },
    arrowIconRight() {
      return this.$root.ltr && 'fa-arrow-right' || 'fa-arrow-left';
    },
  },
  watch: {
    space: {
      immediate: true,
      deep: true,
      handler: function() {
        if (JSON.stringify(this.spaceUnreadItems || {}) !== JSON.stringify(this.space?.unread || {})) {
          this.spaceUnreadItems = this.space?.unread;
        }
      },
    },
  },
  created() {
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
      const applicationName = spaceWebNotificationItem?.applicationName;
      const applicationItemId = spaceWebNotificationItem?.applicationItemId;
      const spaceId = spaceWebNotificationItem?.spaceId;
      const itemRef = `${applicationName}-${applicationItemId}`;
      if (!this.webSocketSpaceUnreadItems[spaceId]) {
        this.webSocketSpaceUnreadItems[spaceId] = {};
      }
      if (spaceId && Number(this.spaceId) === Number(spaceId)) {
        if (!this.spaceUnreadItems) {
          this.space.unread = {};
          this.spaceUnreadItems = this.space.unread;
        }
        if (!this.spaceUnreadItems[applicationName]) {
          this.spaceUnreadItems[applicationName] = 0;
        }
        if (wsEventName === 'notification.unread.item') {
          if (this.webSocketSpaceUnreadItems[spaceId][itemRef] !== true) {
            this.webSocketSpaceUnreadItems[spaceId][itemRef] = true;
            this.spaceUnreadItems[applicationName]++;
          }
        } else if (wsEventName === 'notification.read.item') {
          if (this.spaceUnreadItems[applicationName] > 0) {
            if (this.webSocketSpaceUnreadItems[spaceId][itemRef] !== false) {
              this.webSocketSpaceUnreadItems[spaceId][itemRef] = false;
              this.spaceUnreadItems[applicationName]--;
            }
          }
        } else if (wsEventName === 'notification.read.allItems') {
          this.spaceUnreadItems = null;
          this.webSocketSpaceUnreadItems[spaceId] = {};
        }
        this.refreshUnreadItems();
      }
    },
    refreshUnreadItems() {
      this.spaceUnreadItems = this.spaceUnreadItems && Object.assign({}, this.spaceUnreadItems) || null;
      document.dispatchEvent(new CustomEvent('space-unread-activities-updated', {detail: {
        spaceId: this.spaceId,
        unread: this.spaceUnreadItems,
      }}));
    },
    openOrCloseDrawer(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      this.$root.$emit('change-space-menu', this.space, this.thirdLevel);
    }, 
  },
};
</script>
