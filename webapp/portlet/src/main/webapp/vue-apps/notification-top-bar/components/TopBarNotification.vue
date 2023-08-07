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
          icon
          class="text-xs-center"
          :title="notificationIconTooltip"
          @click="openDrawer()">
          <v-badge
            :value="badge > 0"
            :content="badge"
            flat
            color="var(--allPagesBadgePrimaryColor, #d32a2a)"
            overlap>
            <v-icon class="icon-default-color" size="22">fa-bell</v-icon>
          </v-badge>
        </v-btn>
        <exo-drawer
          ref="drawerNotificationDrawer"
          class="notifDrawer"
          body-classes="hide-scroll"
          right
          @closed="closeDrawer">
          <template slot="title">
            {{ $t('UIIntranetNotificationsPortlet.title.notifications') }}
          </template>
          <template slot="titleIcons">
            <v-btn
              :title="$t('UIIntranetNotificationsPortlet.title.NotificationsSetting')"
              :href="settingsLink"
              icon>
              <v-icon class="uiSettingsIcon notifDrawerSettings" />
            </v-btn>
          </template>
          <template v-if="notificationsSize" slot="content">
            <div class="notifDrawerItems">
              <top-bar-notification-item
                v-for="(notif, i) in notifications"
                :key="i"
                :id="'notifItem-'+i"
                :notif="notif" />
            </div>
          </template>
          <template v-else-if="!loading" slot="content">
            <div class="noNoticationWrapper">
              <div class="noNotificationsContent">
                <i class="uiNoNotifIcon"></i>
                <p>{{ $t('UIIntranetNotificationsPortlet.label.NoNotifications') }}</p>
              </div>
            </div>
          </template>
          <template v-if="notificationsSize" slot="footer">
            <v-row class="notifFooterActions mx-0">
              <v-card 
                flat
                tile 
                class="d-flex flex justify-end mx-2">
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
              </v-card>
            </v-row>
          </template>
        </exo-drawer>
      </v-layout>
    </v-flex>
  </v-app>
</template>
<script>
export default {
  data () {
    return {
      loading: false,
      drawerNotification: false,
      notifications: [],
      badge: 0,
      notificationsSize: 0,
      settingsLink: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/settings`,
      allNotificationsLink: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/allNotifications`,
      notificationIconTooltip: this.$t('UIIntranetNotificationsPortlet.label.tooltip'),
    };
  },
  watch: {
    badge() {
      return this.badge;
    },
    loading() {
      if (!this.loading && this.$refs.drawerNotificationDrawer) {
        this.$refs.drawerNotificationDrawer.endLoading();
      }
    },
  },
  created() {
    document.addEventListener('cometdNotifEvent', this.notificationUpdated);
    this.getNotifications();
  },
  mounted() {
    if (this.$refs.drawerNotificationDrawer) {
      if (this.loading) {
        this.$refs.drawerNotificationDrawer.startLoading();
      } else {
        this.$refs.drawerNotificationDrawer.endLoading();
      }
    }
    this.openDrawer();
    this.$root.$applicationLoaded();
  },
  methods: {
    getNotifications() {
      this.loading = true;
      return this.$notificationService.getNotifications()
        .then((data) => {
          this.notifications = data.notifications;
          this.badge = data.badge;
          this.notificationsSize = this.notifications.length;
          return this.$nextTick();
        })
        .finally(() => this.loading = false);
    },
    markAllAsRead() {
      return this.$notificationService.updateNotification(null, 'markAllAsRead')
        .then(() => {
          $('.notifDrawerItems').find('li').each(function() {
            if ($(this).hasClass('unread')) {
              $(this).removeClass('unread').addClass('read');
            }
          });
        })
        .finally(() => this.$root.$emit('application-loaded'));
    },
    openDrawer() {
      this.$refs.drawerNotificationDrawer.open();
      return this.$notificationService.updateNotification(null, 'resetNew')
        .then(() => this.badge = 0);
    },
    closeDrawer() {
      this.$refs.drawerNotificationDrawer.close();
      this.$nextTick().then(() => {
        this.$root.$emit('application-loaded');
        this.badge = 0;
      });
    },
    navigateTo(pagelink) {
      location.href=`${ eXo.env.portal.context }/${ eXo.env.portal.portalName }/${ pagelink }` ;
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
