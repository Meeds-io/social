<template>
  <v-app>
    <v-main v-if="notificationSettings">
      <v-card class="pa-5 card-border-radius overflow-hidden" flat>
        <h4 class="font-weight-bold my-0">
          {{ $t('NotificationAdmin.title') }}
        </h4>
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
  }),
  created() {
    this.$root.$on('refresh', this.refresh);
    this.refresh();
  },
  methods: {
    refresh() {
      return this.$notificationAdministration.getSettings()
        .then(settings => {
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

