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
    :loading="loading > 0"
    class="notifDrawer"
    body-classes="hide-scroll"
    allow-expand
    right
    @closed="$emit('closed')"
    @expand-updated="expanded = $event">
    <template slot="title">
      {{ $t('UIIntranetNotificationsPortlet.title.notifications') }}
    </template>
    <template #titleIcons>
      <v-tooltip bottom>
        <template #activator="{on, bind}">
          <div v-on="on" v-bind="bind">
            <v-btn
              :disabled="markingAsReadDisabled"
              :loading="markingAllAsRead"
              icon
              @click="markAllAsRead">
              <v-icon size="18">fa-envelope-open-text</v-icon>
            </v-btn>
          </div>
        </template>
        <span>{{ markingAsReadDisabled && $t('Notification.label.NoMarkAllAsRead') || $t('Notification.label.MarkAsRead', {0: $t(`Notification.label.types.${groupName}`)}) }}</span>
      </v-tooltip>
      <v-tooltip bottom>
        <template #activator="{on, bind}">
          <v-btn
            :href="settingsLink"
            icon
            v-on="on"
            v-bind="bind"
            @click="openSettings">
            <v-icon size="18" class="notifDrawerSettings">fa-sliders-h</v-icon>
          </v-btn>
        </template>
        <span>{{ $t('UIIntranetNotificationsPortlet.title.NotificationsSetting') }}</span>
      </v-tooltip>
    </template>
    <template #content>
      <div
        :class="expanded && 'pa-4'"
        class="d-flex light-grey-background-color fill-height">
        <div
          class="singlePageApplication pa-0 d-flex fill-height">
          <v-card
            v-if="expanded"
            class="card-border-radius"
            height="fit-content"
            min-width="270"
            width="270"
            max-width="30%"
            flat>
            <user-notification-types
              ref="notificationTypes"
              id="notificationTypes"
              :badge="badge"
              :badge-by-plugin="badgeByPlugin"
              class="flex-grow-0 flex-shrink-0"
              @change="selectType" />
          </v-card>
          <v-expand-x-transition>
            <v-card
              :min-width="separatorWidth"
              :class="expanded && 'me-4'" />
          </v-expand-x-transition>
          <v-card
            :max-height="expanded && '100%' || 'auto'"
            class="d-flex flex-column flex-grow-1 flex-shrink-1 transparent no-border-radius overflow-hidden"
            flat>
            <v-card
              :max-height="expanded && '100%' || 'auto'"
              :class="expanded && 'overflow-x-hidden overflow-y-auto card-border-radius' || 'overflow-x-hidden no-border-radius'"
              :tile="!expanded"
              class="d-flex flex-column flex-grow-1 flex-shrink-1"
              flat
              @scroll="scrollActivated">
              <user-notifications
                ref="notifications"
                id="notificationsList"
                :plugins="notificationPlugins"
                :expanded="expanded"
                :unread-only="unreadOnly"
                class="notifDrawerItems"
                @badge="$emit('update:badge', $event)"
                @hasMore="hasMore = $event"
                @badgeByPlugin="badgeByPlugin = $event"
                @notificationsCount="notificationsCount = $event"
                @unreadCount="hasUnread = $event" />
            </v-card>
            <v-btn
              v-if="expanded && hasMore && $root.initialized"
              :loading="loading > 0"
              class="btn mx-auto mt-4 flex-grow-0 flex-shrink-0"
              outlined
              @click="$refs.notifications.loadMore()">
              {{ $t('button.loadMore') }}
            </v-btn>
          </v-card>
        </div>
      </div>
    </template>
    <template v-if="!expanded && hasMore && $root.initialized" #footer>
      <div class="d-flex align-center justify-center">
        <v-btn
          :loading="loading > 0"
          class="btn"
          outlined
          block
          @click="$refs.notifications.loadMore()">
          {{ $t('button.loadMore') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    badge: {
      type: Number,
      default: () => 0,
    },
  },
  data: () =>({
    loading: 0,
    hasMore: false,
    hasUnread: false,
    expanded: false,
    unreadOnly: false,
    notificationsCount: 0,
    groupName: 'all',
    notificationPlugins: null,
    badgeByPlugin: null,
    separatorWidth: 0,
    markingAllAsRead: false,
    settingsLink: `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/settings#notifications`,
  }),
  computed: {
    markingAsReadDisabled() {
      return !this.hasUnread || this.loading > 0;
    },
  },
  watch: {
    loading() {
      if (this.loading === 0) {
        this.$nextTick().then(() => {
          this.$root.initialized = true;
          this.$root.$emit('notifications-initialized');
        });
      }
    },
    expanded() {
      if (!this.expanded) {
        if (this.notificationPlugins) {
          this.notificationPlugins = null;
          this.unread = false;
        }
        this.separatorWidth = '50%';
        window.setTimeout(() => this.separatorWidth = '0', 200);
      }
    },
  },
  created() {
    this.$root.$on('notification-loading-start', this.incrementLoading);
    this.$root.$on('notification-loading-end', this.decrementLoading);
  },
  beforeDestroy() {
    this.$root.$off('notification-loading-start', this.incrementLoading);
    this.$root.$off('notification-loading-end', this.decrementLoading);
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
    incrementLoading() {
      this.loading++;
    },
    decrementLoading() {
      this.loading--;
    },
    openSettings() {
      document.dispatchEvent(new CustomEvent('showNotificationSettings'));
      this.close();
    },
    scrollActivated() {
      document.dispatchEvent(new CustomEvent('notifications-list-scroll-activated'));
    },
    markAllAsRead() {
      this.markingAllAsRead = true;
      return this.$notificationService.markAllAsRead(this.notificationPlugins)
        .then(() => {
          document.querySelectorAll('.notifDrawerItems li.unread').forEach(el => {
            el.classList.remove('unread');
            el.classList.add('read');
          });
        })
        .finally(() => {
          this.markingAllAsRead = false;
          document.dispatchEvent(new CustomEvent('refresh-notifications'));
        });
    },
    selectType(name, plugins, unread) {
      this.groupName = name;
      this.notificationPlugins = plugins;
      this.unreadOnly = unread;
    }
  },
};
</script>
