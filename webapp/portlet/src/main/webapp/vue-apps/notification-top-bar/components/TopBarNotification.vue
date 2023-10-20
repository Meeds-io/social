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
          @click="openDrawer">
          <v-badge
            :value="badge > 0"
            :content="badge"
            flat
            color="var(--allPagesBadgePrimaryColor, #d32a2a)"
            overlap>
            <v-icon class="icon-default-color" size="22">fa-bell</v-icon>
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
  }),
  watch: {
    open() {
      if (this.open) {
        this.$nextTick().then(() => this.$refs.drawer.open());
      }
    },
  },
  created() {
    document.addEventListener('cometdNotifEvent', this.updateBadgeByEvent);
    this.$root.$on('notification-badge-updated', this.updateBadge);
    this.badge = this.$root.badge;
  },
  beforeDestroy() {
    document.removeEventListener('cometdNotifEvent', this.updateBadgeByEvent);
    this.$root.$off('notification-badge-updated', this.updateBadge);
  },
  mounted() {
    this.$root.$applicationLoaded();
  },
  methods: {
    openDrawer() {
      this.$root.initialized = false;
      this.$root.lastLoadedNotificationIndex = 0;
      this.open = true;
    },
    updateBadgeByEvent(event) {
      this.updateBadge(event?.detail?.data?.numberOnBadge || 0);
      this.sendDesktopNotification(event?.detail?.data);
    },
    updateBadge(badge) {
      this.badge = badge;
    },
    sendDesktopNotification(data) {
      if (window.Notification) {
        if (window.Notification.permission === 'granted') {
          this.handleNotification(data);

        } else {
          window.Notification.requestPermission();
        }
      }
    },
    handleNotification(eventData) {
      this.$notificationService.getNotification(eventData?.id)
        .then((notificationData) => {
          const accepted = notificationData?.parameters?.status === 'accepted';

          const profileFullName = notificationData?.from?.fullname || notificationData?.parameters?.sender;
          const spaceDisplayName = notificationData?.space?.displayName  || '';
          
          switch (notificationData.plugin) {
          case 'ActivityCommentPlugin':
          case 'ActivityReplyToCommentPlugin':
          case 'LikeCommentPlugin':
          case 'LikePlugin': {
            this.getPosterIdentities(notificationData);
            break;
          }

          case 'SpaceInvitationPlugin': {
            const messageKey = accepted && 'Notification.intranet.message.accept.SpaceInvitationPlugin' || 'Notification.intranet.message.SpaceInvitationPlugin';
            const message = this.$t(messageKey, accepted && {
              0: spaceDisplayName,
            } || {
              0: profileFullName,
              1: spaceDisplayName,
            });
            new Notification(eXo.env.portal.companyName, {
              body: message
            });
            break;
          }

          case 'RelationshipReceivedRequestPlugin': {
            const messageKey = accepted && `Notification.intranet.message.accept.${notificationData?.plugin}` || `Notification.intranet.message.${notificationData?.plugin}`;
            const message = this.$t(messageKey, {
              0: profileFullName,
            });
            new Notification(message);
            break;
          }

          case 'RequestJoinSpacePlugin':
          case 'ActivityMentionPlugin':
          case 'EditActivityPlugin':
          case 'EditCommentPlugin':
          case 'PostActivityPlugin':
          case 'PostActivitySpaceStreamPlugin':
          case 'SharedActivitySpaceStreamPlugin': {
            const messageKey =  `Notification.intranet.message.${notificationData?.plugin}`;
            const message= this.$t(messageKey, {
              0: profileFullName,
              1: spaceDisplayName,
            });
            new Notification(eXo.env.portal.companyName, {
              body: message
            });
            break;
          }
          case 'NewUser': {
            const messageKey = 'Notification.intranet.message.NewUserPlugin';
            const message= this.$t(messageKey, {
              0: profileFullName,
              1: eXo.env.portal.companyName,
            });
            new Notification(message);
            break;
          }}});
    },

    getPosterIdentities(notificationData) {
      const posterUsernames = notificationData?.parameters?.poster
          && notificationData.parameters.poster.split(',')
          || [notificationData.parameters.poster];
      let posterIdentities = [];

      if (posterUsernames?.length && posterUsernames?.length > 1) {
        return Promise.all(posterUsernames.slice(0, 2).map(u => this.$identityService.getIdentityByProviderIdAndRemoteId('organization', u)))
          .then(identities => {
            posterIdentities = identities.map(i => i?.profile).filter(p => !!p);
            this.constructPosterIdentitiesNotification(posterIdentities, posterUsernames, notificationData?.plugin);
          });
      } else {
        posterIdentities = notificationData.from && [notificationData.from] || [];
        this.constructPosterIdentitiesNotification(posterIdentities, posterUsernames, notificationData?.plugin);
      }
    },

    constructPosterIdentitiesNotification(posterIdentities, posterUsernames, pluginId) {
      let message;

      if (!posterIdentities?.length) {
        message = `Notification.intranet.message.one.${pluginId}`;
      } else if (posterUsernames.length < 2) {
        message = this.$t(`Notification.intranet.message.one.${pluginId}`, {
          0: posterIdentities[0].fullname,
        });
      } else if (posterUsernames.length < 3) {
        message = this.$t(`Notification.intranet.message.two.${pluginId}`, {
          0: posterIdentities[0].fullname,
          1: posterIdentities[1].fullname,
        });
      } else {
        message = this.$t(`Notification.intranet.message.more.${pluginId}`, {
          0: posterIdentities[0].fullname,
          1: posterIdentities[1].fullname,
          2: posterUsernames.length - 2,
        });
      }

      new Notification(eXo.env.portal.companyName, {
        body: message
      });
    },
  },
};
</script>