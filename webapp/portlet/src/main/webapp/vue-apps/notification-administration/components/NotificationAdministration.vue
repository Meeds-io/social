<template>
  <v-app>
    <widget-wrapper v-if="notificationSettings">
      <template #title>
        {{ $t('NotificationAdmin.title') }}
      </template>
      <template>
        <notification-administration-contact
          :settings="notificationSettings" />
        <notification-administration-channels
          :settings="notificationSettings" />
        <notification-administration-plugins
          :settings="notificationSettings" />
      </template>
    </widget-wrapper>
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

