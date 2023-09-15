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
    limit: 10,
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
      this.reset();
      this.loadNotifications();
    },
    expanded(newVal, oldVal) {
      if (!oldVal && newVal) {
        this.loadNotifications();
      }
      this.reset();
      this.loadNotifications();
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
        this.$emit('update:notifications-count', this.notifications.length);
        this.$emit('update:unread-count', this.notifications.filter(n => !n.read).length);
      } else {
        this.$emit('update:notifications-count', 0);
        this.$emit('update:unread-count', 0);
      }
    },
  },
  created() {
    document.addEventListener('refresh-notifications', this.loadNotifications);
    document.addEventListener('cometdNotifEvent', this.notificationUpdated);
    this.loadNotifications();
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
    loadNotifications() {
      this.loading = true;
      return this.$notificationService.getNotifications(this.plugins, this.offset, this.limit, this.expanded && 'badge-by-plugin')
        .then((data) => {
          this.notifications = data.notifications || [];
          this.badge = data.badge || 0;
          this.badgeByPlugin = data.badgesByPlugin;
          this.hasMore = this.notifications.length === this.limit;
          return this.$nextTick();
        })
        .finally(() => this.loading = false);
    },
    notificationUpdated(event) {
      if (event && event.detail) {
        this.loadNotifications();
      }
    },
  }
};
</script>
