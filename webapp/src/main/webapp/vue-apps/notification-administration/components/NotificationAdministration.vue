<template>
  <v-app>
    <v-main v-if="notificationSettings">
      <v-card class="pa-5 application-body" flat>
        <notification-administration-contact
          :settings="notificationSettings" />
        <notification-administration-channels
          :settings="notificationSettings" />
        <notification-administration-plugins
          :settings="notificationSettings" />
      </v-card>
    </v-main>
  </v-app>
</template>

<script>
export default {
  data: () => ({
    notificationSettings: null,
    lastRefreshId: 0,
  }),
  created() {
    this.$root.$on('refresh', this.refresh);
    this.refresh();
  },
  methods: {
    refresh() {
      const refreshId = ++this.lastRefreshId;
      return this.$notificationAdministration.getSettings()
        .then(settings => {
          // A response overtaken by a later read holds settings older than the switches
          if (refreshId !== this.lastRefreshId) {
            return;
          }
          if (settings?.channelLabels) {
            Object.keys(settings.channelLabels).forEach(channelId => {
              if (this.$te(`NotificationAdmin.${channelId}.name`)) {
                settings.channelLabels[channelId] = this.$t(`NotificationAdmin.${channelId}.name`);
              }
            });
          }
          this.notificationSettings = settings;
          return this.$nextTick();
        })
        .finally(() => {
          this.$nextTick().then(() => this.$root.$applicationLoaded());
        });
    },
  },
};
</script>

