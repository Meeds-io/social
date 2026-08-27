<template>
  <v-card flat>
    <div class="pt-8 text-title">{{ $t('NotificationAdmin.allowedNotifications.title') }}</div>
    <div class="text-subtitle">{{ $t('NotificationAdmin.allowedNotifications.subtitle') }}</div>
    <v-switch
      v-model="digestAllowed"
      hide-details
      @change="saveDigestAllowed($event)">
      <template #label>
        <span class="text-color">{{ $t('NotificationAdmin.allowDigest.title') }}</span>
      </template>
    </v-switch>
    <div v-for="channelId in channelIds" :key="channelId">
      <v-switch
        v-model="channelStatus[channelId]"
        hide-details
        @change="saveChannelStatus(channelId, $event)">
        <template #label>
          <span class="text-color">{{ channelLabels[channelId] }}</span>
        </template>
      </v-switch>
      <div v-if="channelStatus[channelId]" class="ms-8">
        <v-switch
          v-model="channelDefaultValue[channelId]"
          @change="saveChannelDefault(channelId, $event)">
          hide-details>
          <template #label>
            <span class="text-color">{{ $t('NotificationAdmin.activateForAll') }}</span>
          </template>
        </v-switch>
      </div>
    </div>
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
    digestAllowed: false,
  }),
  created() {
    this.$notificationAdministration.getDigestSettings()
      .then(digestSettings => this.digestAllowed = digestSettings?.digestAllowed || false);
  },
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
    channelDefaultValue() {
      return this.settings.channelDefaultValue;
    },
  },
  methods: {
    saveDigestAllowed(allowed) {
      this.saving = true;
      return this.$notificationAdministration.saveDigestAllowed(!!allowed)
        .catch(() => {
          this.digestAllowed = !allowed;
          this.$root.$emit('alert-message', this.$t('NotificationAdmin.allowDigest.savingError'), 'error');
        })
        .finally(() => this.saving = false);
    },
    saveChannelStatus(channelId, status) {
      this.saving = true;
      return this.$notificationAdministration.saveChannelStatus(channelId, status)
        .then(() => this.$root.$emit('refresh'))
        .finally(() => this.saving = false);
    },
    saveChannelDefault(channelId, status) {
      this.saving = true;
      return this.$notificationAdministration.saveChannelDefaultValue(channelId, status)
        .then(() => this.$root.$emit('refresh'))
        .finally(() => this.saving = false);
    },
  },
};
</script>
