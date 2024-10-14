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
  <user-notifications-list
    v-if="hasNotifications"
    :notifications="notifications" />
  <user-notification-empty
    v-else />
</template>
<script>
export default {
  props: {
    plugins: {
      type: Array,
      default: null,
    },
    unreadOnly: {
      type: Boolean,
      default: null,
    },
    expanded: {
      type: Boolean,
      default: null,
    },
  },
  data: () => ({
    loading: false,
    badge: 0,
    badgeByPlugin: null,
    hasMore: false,
    notifications: [],
    offset: 0,
    limit: Math.max(10, Math.round((window.innerHeight - 122) / 90)),
    pageSize: 10,
  }),
  computed: {
    hasNotifications() {
      return this.loading || this.notifications?.length;
    },
  },
  watch: {
    loading: {
      immediate: true,
      handler(newVal, oldVal) {
        if (newVal && !oldVal) {
          this.$root.$emit('notification-loading-start');
        } else if (!newVal && oldVal) {
          window.setTimeout(() => {
            this.$root.$emit('notification-loading-end');
          }, 200);
        }
      }
    },
    plugins() {
      if (!this.loading) {
        this.reset();
        this.loadNotifications();
      }
    },
    unreadOnly() {
      if (!this.loading) {
        this.reset();
        this.loadNotifications();
      }
    },
    expanded(newVal, oldVal) {
      if (!oldVal && newVal) {
        this.loadNotifications(true);
      }
    },
    hasMore() {
      this.$emit('hasMore', this.hasMore);
    },
    badge() {
      this.$emit('badge', this.badge);
    },
    badgeByPlugin() {
      this.$emit('badgeByPlugin', this.badgeByPlugin);
    },
    notifications() {
      if (this.notifications?.length) {
        this.$emit('notificationsCount', this.notifications.length);
        this.$emit('unreadCount', this.notifications.filter(n => !n.read).length);
      } else {
        this.$emit('notificationsCount', 0);
        this.$emit('unreadCount', 0);
      }
    },
  },
  created() {
    document.addEventListener('refresh-notifications', this.refreshNotifications);
    document.addEventListener('cometdNotifEvent', this.notificationUpdated);
    this.$root.$on('notifications-initialized', this.notificationsDisplayed);
    this.loadNotifications();
  },
  beforeDestroy() {
    document.removeEventListener('refresh-notifications', this.refreshNotifications);
    document.removeEventListener('cometdNotifEvent', this.notificationUpdated);
    this.$root.$off('notifications-initialized', this.notificationsDisplayed);
  },
  methods: {
    reset() {
      this.loading = false;
      this.badge = 0;
      this.hasMore = false;
      this.notifications = [];
      this.offset = 0;
      this.limit = 10;
      this.pageSize = 10;
    },
    loadMore() {
      this.limit += this.pageSize;
      this.loadNotifications();
    },
    refreshNotifications() {
      this.loadNotifications();
    },
    loadNotifications(loadBadgesOnly) {
      this.loading = true;
      return this.$notificationService.getNotifications({
        plugins: this.plugins,
        unreadOnly: this.unreadOnly,
        badgesByPlugin: this.expanded,
        includeHidden: false,
        offset: this.offset,
        limit: !loadBadgesOnly && this.limit || 0,
      })
        .then((data) => {
          if (!loadBadgesOnly) {
            this.notifications = data.notifications || [];
            this.hasMore = this.notifications.length === this.limit;
          }
          this.badge = data.badge || 0;
          this.badgeByPlugin = data.badgesByPlugin;
          return this.$nextTick();
        })
        .finally(() => this.loading = false);
    },
    notificationUpdated(event) {
      if (!this.loading && event && event.detail) {
        this.loadNotifications();
      }
    },
    notificationsDisplayed() {
      this.$root.lastLoadedNotificationIndex = this.notifications?.length || 0;
    },
  }
};
</script>
