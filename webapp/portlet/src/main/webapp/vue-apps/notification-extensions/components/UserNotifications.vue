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
  <div v-if="hasNotifications">
    <user-notifications-list
      :notifications="notifications" />
    <div v-if="hasMore" class="d-flex align-center justify-center my-4">
      <v-btn
        :loading="loading"
        class="btn primary"
        outlined
        @click="loadMore">
        {{ $t('button.loadMore') }}
      </v-btn>
    </div>
  </div>
  <user-notification-empty v-else />
</template>
<script>
export default {
  data: () => ({
    loading: false,
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
    loading() {
      this.$emit('update:loading', this.loading);
    },
    notifications() {
      this.$emit('update:notifications-count', this.notifications?.length || 0);
    },
  },
  created() {
    document.addEventListener('refresh-notifications', this.loadNotifications);
    document.addEventListener('cometdNotifEvent', this.notificationUpdated);
    this.loadNotifications();
  },
  methods: {
    loadMore() {
      this.limit += this.pageSize;
      this.loadNotifications();
    },
    loadNotifications() {
      this.loading = true;
      return this.$notificationService.getNotifications(this.offset, this.limit)
        .then((data) => {
          this.notifications = data.notifications || [];
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
