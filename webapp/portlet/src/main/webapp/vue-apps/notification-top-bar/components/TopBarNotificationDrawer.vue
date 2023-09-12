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
    right>
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
      <div v-if="notifications.length" class="notifDrawerItems">
        <user-notification
          v-for="(notification, i) in notifications"
          :key="notification.id"
          :id="'notifItem-'+i"
          :notification="notification" />
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
      <div v-else-if="!loading" class="noNoticationWrapper">
        <div class="noNotificationsContent">
          <i class="uiNoNotifIcon"></i>
          <p>{{ $t('UIIntranetNotificationsPortlet.label.NoNotifications') }}</p>
        </div>
      </div>
    </template>
    <template v-if="notifications.length" #footer>
      <div class="notifFooterActions d-flex flex justify-end">
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
    markingAllAsRead: false,
    hasMore: false,
    notifications: [],
    offset: 0,
    limit: 10,
    pageSize: 10,
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
    this.$root.$on('refresh-notifications', this.loadNotifications);
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
    loadMore() {
      this.limit += this.pageSize;
      this.loadNotifications();
    },
    loadNotifications() {
      this.loading = true;
      return this.$notificationService.getNotifications(this.offset, this.limit)
        .then((data) => {
          this.notifications = data.notifications || [];
          this.badge = data.badge;
          this.hasMore = this.notifications.length === this.limit;
          return this.$nextTick();
        })
        .finally(() => this.loading = false);
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
          return this.loadNotifications();
        });
    },
    notificationUpdated(event) {
      if (event && event.detail) {
        this.badge = event.detail.data.numberOnBadge;
        this.loadNotifications();
      }
    },
  }
};
</script>
