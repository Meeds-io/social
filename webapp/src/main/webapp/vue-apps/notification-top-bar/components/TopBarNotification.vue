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
  <v-app id="NotificationPopoverPortlet">
    <v-flex>
      <v-layout>
        <v-btn
          :title="tooltip"
          :loading="loading"
          icon
          @click="openDrawer">
          <v-badge
            :value="badge > 0"
            :content="badgeContent"
            flat
            color="var(--allPagesBadgePrimaryColor, #d32a2a)"
            overlap>
            <v-icon class="icon-default-color" size="20">fa-bell</v-icon>
          </v-badge>
        </v-btn>
      </v-layout>
    </v-flex>
    <top-bar-notification-drawer
      v-if="open"
      ref="drawer"
      :badge.sync="badge"
      @closed="open = false" />
  </v-app>
</template>
<script>
export default {
  data: () => ({
    badge: 0,
    open: false,
    loading: false,
  }),
  computed: {
    tooltip() {
      return `${this.$t('UIIntranetNotificationsPortlet.title.notifications')} ${this.$t('UIIntranetNotificationsPortlet.title.notifications.shortcut')}`;
    },
    badgeContent() {
      return this.badge > 99 && '99+' || this.badge;
    },
  },
  watch: {
    open() {
      if (this.open) {
        this.$nextTick().then(() => this.$refs.drawer.open());
      }
    },
  },
  created() {
    document.addEventListener('cometdNotifEvent', this.updateBadgeByEvent);
    document.addEventListener('notifications-drawer-open', this.openDrawer);
    this.$root.$on('notification-badge-updated', this.updateBadge);
    this.badge = this.$root.badge;
  },
  beforeDestroy() {
    document.removeEventListener('cometdNotifEvent', this.updateBadgeByEvent);
    document.removeEventListener('notifications-drawer-open', this.openDrawer);
    this.$root.$off('notification-badge-updated', this.updateBadge);
  },
  mounted() {
    this.$root.$applicationLoaded();
  },
  methods: {
    openDrawer() {
      this.$root.initialized = false;
      this.loading = true;
      this.$root.lastLoadedNotificationIndex = 0;
      window.require(['SHARED/notificationExtensions'], () => {
        Promise.resolve(this.$utils.includeExtensions('NotificationExtension'))
          .then(() => {
            this.open = true;
            this.loading = false;
          });
      });
    },
    updateBadgeByEvent(event) {
      this.updateBadge(event?.detail?.data?.numberOnBadge || 0);
    },
    updateBadge(badge) {
      this.badge = badge;
    },
  },
};
</script>