<template>
  <v-app v-if="displayed">
    <user-setting-notifications-window
      v-if="displayDetails"
      :settings="notificationSettings"
      :digest-mail-notification-enabled="digestMailNotificationEnabled"
      @back="closeDetail" />
    <v-card
      v-else
      class="ma-4 border-radius"
      flat>
      <v-list @click="openNotificationSettingDetail">
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title text-color">
              {{ $t('UserSettings.notifications') }}
            </v-list-item-title>
          </v-list-item-content>
        </v-list-item>

        <template v-if="notificationSettings && notificationSettings.channels">
          <user-setting-notification-channel
            v-for="channel in notificationSettings.channels"
            :key="channel"
            :channel="channel"
            :active="notificationSettings.channelStatus[channel]"
            :settings="notificationSettings" />
        </template>

        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title text-color">
              {{ $t('UserSettings.manageNotifications') }}
            </v-list-item-title>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              small
              icon
              @click="openDetail">
              <v-icon size="24" class="text-sub-title">
                {{ $vuetify.rtl && 'fa-caret-left' || 'fa-caret-right' }}
              </v-icon>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </v-list>
    </v-card>
  </v-app>
</template>

<script>
export default {
  data: () => ({
    id: `Notifications${parseInt(Math.random() * 10000)
      .toString()
      .toString()}`,
    notificationSettings: null,
    displayDetails: false,
    displayed: true,
    digestMailNotificationEnabled: false
  }),
  created() {
    this.$featureService.isFeatureEnabled('digestMailNotification')
      .then(enabled => this.digestMailNotificationEnabled = enabled);
    document.addEventListener('hideSettingsApps', (event) => {
      if (event && event.detail && this.id !== event.detail) {
        this.displayed = false;
      }
    });
    document.addEventListener('showSettingsApps', () => this.displayed = true);
    this.$root.$on('refresh', this.refresh);
    this.refresh();
  },
  methods: {
    refresh() {
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/notifications/settings/${eXo.env.portal.userName}`)
        .then(resp => resp && resp.ok && resp.json())
        .then(settings => {
          this.notificationSettings = settings;
          return this.$nextTick();
        })
        .finally(() => {
          this.$nextTick().then(() => this.$root.$applicationLoaded());
        });
    },
    openDetail() {
      document.dispatchEvent(new CustomEvent('hideSettingsApps', {detail: this.id}));
      this.displayDetails = true;
    },
    closeDetail() {
      document.dispatchEvent(new CustomEvent('showSettingsApps'));
      this.displayDetails = false;
    },
  },
};
</script>

