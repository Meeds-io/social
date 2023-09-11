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
    right>
    <template slot="title">
      {{ $t('UIIntranetNotificationsPortlet.title.notifications') }}
    </template>
    <template #titleIcons>
      <v-btn
        :title="$t('UIIntranetNotificationsPortlet.title.NotificationsSetting')"
        :href="settingsLink"
        icon>
        <v-icon class="uiSettingsIcon notifDrawerSettings" />
      </v-btn>
    </template>
    <template #content>
      <div v-if="notifications.length" class="notifDrawerItems">
        <user-notification
          v-for="(notification, i) in notifications"
          :key="notification.id"
          :id="'notifItem-'+i"
          :notification="notification" />
      </div>
      <div v-else-if="!loading" class="noNoticationWrapper">
        <div class="noNotificationsContent">
          <i class="uiNoNotifIcon"></i>
          <p>{{ $t('UIIntranetNotificationsPortlet.label.NoNotifications') }}</p>
        </div>
      </div>
    </template>
    <template v-if="notifications.length" #footer>
      <div class="notifFooterActions d-flex flex justify-end pa-2">
        <v-btn 
          text
          small
          class="text-uppercase caption markAllAsRead"
          color="primary"
          @click="markAllAsRead()">
          {{ $t('UIIntranetNotificationsPortlet.label.MarkAllAsRead') }}
        </v-btn>
        <v-btn 
          :href="allNotificationsLink"
          class="text-uppercase caption primary--text seeAllNotif"
          outlined
          small>
          {{ $t('UIIntranetNotificationsPortlet.label.seeAll') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () =>({
    loading: false,
    notifications: [],
    badge: 0,
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
  created() {
    document.addEventListener('cometdNotifEvent', this.notificationUpdated);
  },
  mounted() {
    this.open();
  },
  methods: {
    open() {
      this.$refs.drawer.open();
      return this.$notificationService.resetBadge()
        .then(() => this.badge = 0);
    },
    close() {
      this.$refs.drawer.close();
    },
    getNotifications() {
      this.loading = true;
      return this.$notificationService.getNotifications()
        .then((data) => {
          this.notifications = data.notifications || [];
          this.badge = data.badge;
          return this.$nextTick();
        })
        .finally(() => this.loading = false);
    },
    markAllAsRead() {
      return this.$notificationService.markAllAsRead()
        .then(() => {
          $('.notifDrawerItems').find('li').each(function() {
            if ($(this).hasClass('unread')) {
              $(this).removeClass('unread').addClass('read');
            }
          });
        })
        .finally(() => this.$root.$emit('application-loaded'));
    },
    notificationUpdated(event) {
      if (event && event.detail) {
        this.badge = event.detail.data.numberOnBadge;
        this.getNotifications();
      }
    },
  }
};
</script>
