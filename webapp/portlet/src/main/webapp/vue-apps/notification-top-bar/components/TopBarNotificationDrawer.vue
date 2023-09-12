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
  <exo-drawer
    ref="drawer"
    class="notifDrawer"
    body-classes="hide-scroll"
    allow-expand
    right
    @closed="$emit('closed')">
    <template slot="title">
      {{ $t('UIIntranetNotificationsPortlet.title.notifications') }}
    </template>
    <template #titleIcons>
      <v-btn
        :title="$t('UIIntranetNotificationsPortlet.title.NotificationsSetting')"
        :href="settingsLink"
        icon>
        <v-icon size="18" class="notifDrawerSettings">fa-sliders-h</v-icon>
      </v-btn>
    </template>
    <template #content>
      <user-notifications
        :loading.sync="loading"
        :notifications-count.sync="notificationsCount" />
    </template>
    <template v-if="notificationsCount" #footer>
      <div class="notifFooterActions d-flex flex justify-end">
        <v-btn
          :href="allNotificationsLink"
          class="btn me-2">
          <span class="text-none">
            {{ $t('UIIntranetNotificationsPortlet.title.AllNotifications') }}
          </span>
        </v-btn>
        <v-btn
          :loading="markingAllAsRead"
          class="btn primary"
          @click="markAllAsRead()">
          {{ $t('UIIntranetNotificationsPortlet.label.MarkAllAsRead') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () =>({
    loading: false,
    notificationsCount: 0,
    markingAllAsRead: false,
    settingsLink: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/settings`,
    allNotificationsLink: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/allNotifications`,
  }),
  watch: {
    loading() {
      if (this.loading) {
        this.$refs.drawer?.startLoading();
      } else {
        this.$refs.drawer?.endLoading();
      }
    },
  },
  methods: {
    open() {
      this.$refs.drawer.open();
      return this.$notificationService.resetBadge()
        .then(() => this.$root.$emit('notification-badge-updated', 0));
    },
    close() {
      this.$refs.drawer.close();
    },
    markAllAsRead() {
      this.markingAllAsRead = true;
      return this.$notificationService.markAllAsRead()
        .then(() => {
          $('.notifDrawerItems').find('li').each(function() {
            if ($(this).hasClass('unread')) {
              $(this).removeClass('unread').addClass('read');
            }
          });
        })
        .finally(() => {
          this.markingAllAsRead = false;
          document.dispatchEvent(new CustomEvent('refresh-notifications'));
        });
    },
  }
};
</script>
