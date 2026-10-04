<template>
  <v-list-item v-if="channelOptions.length" dense>
    <v-list-item-content class="py-1">
      <v-list-item-title class="text-color text-wrap">
        {{ label }}
      </v-list-item-title>
    </v-list-item-content>
    <v-list-item-action class="d-flex flex-row align-center justify-end my-1">
      <div
        v-for="channelId in columns"
        :key="channelId"
        :style="columnStyle"
        class="d-flex justify-center flex-shrink-0">
        <v-switch
          v-if="optionsByChannel[channelId]"
          :input-value="channels[channelId]"
          :aria-label="`${channelLabel(channelId)} - ${label}`"
          :disabled="saving || !optionsByChannel[channelId].channelActive"
          :loading="savingChannel === channelId"
          class="mt-0 pt-0"
          hide-details
          @change="toggle(optionsByChannel[channelId], $event)" />
      </div>
    </v-list-item-action>
  </v-list-item>
</template>

<script>
export default {
  props: {
    plugin: {
      type: Object,
      default: null,
    },
    settings: {
      type: Object,
      default: null,
    },
    columns: {
      type: Array,
      default: () => [],
    },
  },
  data: () => ({
    channels: {},
    saving: false,
    savingChannel: null,
  }),
  computed: {
    label() {
      return this.settings && this.settings.pluginLabels && this.settings.pluginLabels[this.plugin.type];
    },
    channelOptions() {
      return (this.settings?.channelCheckBoxList || [])
        .filter(choice => choice.allowed && choice.pluginId === this.plugin.type);
    },
    optionsByChannel() {
      const options = {};
      this.channelOptions.forEach(option => options[option.channelId] = option);
      return options;
    },
    columnStyle() {
      const width = this.$vuetify.breakpoint.smAndDown ? 64 : 96;
      return `width: ${width}px; min-width: ${width}px;`;
    },
  },
  watch: {
    channelOptions: {
      immediate: true,
      handler() {
        const channels = {};
        this.channelOptions.forEach(option => {
          channels[option.channelId] = !!(option.allowed && option.active && option.channelActive);
        });
        this.channels = channels;
      },
    },
  },
  methods: {
    channelLabel(channelId) {
      return this.settings?.channelLabels?.[channelId];
    },
    toggle(option, value) {
      const channelId = option.channelId;
      const previousValue = this.channels[channelId];
      this.$set(this.channels, channelId, !!value);
      this.saving = true;
      this.savingChannel = channelId;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/notifications/settings/${eXo.env.portal.userName}/plugin/${this.plugin.type}`, {
        method: 'PATCH',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `channels=${channelId}=${!!value}`
      }).then(resp => {
        if (!resp || !resp.ok) {
          throw new Error('Error saving notification settings');
        }
        this.$root.$emit('refresh');
      }).catch(() => {
        this.$set(this.channels, channelId, previousValue);
        this.$root.$emit('alert-message', this.$t('UserSettings.notifications.error.save'), 'error');
      }).finally(() => {
        this.saving = false;
        this.savingChannel = null;
      });
    },
  },
};
</script>
