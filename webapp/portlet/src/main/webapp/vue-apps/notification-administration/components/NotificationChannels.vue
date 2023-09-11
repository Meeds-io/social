<template>
  <v-card flat>
    <div class="pt-8 subtitle-1">{{ $t('NotificationAdmin.allowedNotifications.title') }}</div>
    <div class="caption">{{ $t('NotificationAdmin.allowedNotifications.subtitle') }}</div>
    <v-switch
      v-for="channelId in channelIds"
      v-model="channelStatus[channelId]"
      :key="channelId"
      hide-details
      @change="saveChannelStatus(channelId, $event)">
      <template #label>
        <span class="text-color">{{ channelLabels[channelId] }}</span>
      </template>
    </v-switch>
  </v-card>
</template>
<script>
export default {
  props: {
    settings: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    saving: false,
  }),
  computed: {
    channelIds() {
      return Object.keys(this.settings.channelStatus);
    },
    channelLabels() {
      const channelLabels = Object.assign({}, this.channelStatus);
      Object.keys(this.channelStatus).forEach(channelId => channelLabels[channelId] = this.$t(`NotificationAdmin.${channelId}.title`));
      return channelLabels;
    },
    channelStatus() {
      return this.settings.channelStatus;
    },
  },
  methods: {
    saveChannelStatus(channelId, status) {
      this.saving = true;
      return this.$notificationAdministration.saveChannelStatus(channelId, status)
        .then(() => this.$root.$emit('refresh'))
        .finally(() => this.saving = false);
    },
  },
};
</script>