<template>
  <v-list-item v-if="channelOptions.length" dense>
    <v-list-item-content class="py-1">
      <v-list-item-title class="text-color text-wrap">
        {{ label }}
      </v-list-item-title>
    </v-list-item-content>
    <v-list-item-action class="d-flex flex-row flex-wrap align-center justify-end my-1">
      <v-switch
        v-for="option in channelOptions"
        :key="option.channelId"
        :input-value="channels[option.channelId]"
        :label="channelLabel(option.channelId)"
        :aria-label="channelLabel(option.channelId)"
        :disabled="saving || !option.channelActive"
        class="mt-0 ms-4"
        dense
        inset
        hide-details
        @change="toggle(option, $event)" />
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
  },
  data: () => ({
    channels: {},
    saving: false,
  }),
  computed: {
    label() {
      return this.settings && this.settings.pluginLabels && this.settings.pluginLabels[this.plugin.type];
    },
    channelOptions() {
      return (this.settings?.channelCheckBoxList || [])
        .filter(choice => choice.allowed && choice.pluginId === this.plugin.type)
        .sort((a, b) => a.channelId.localeCompare(b.channelId));
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
      });
    },
  },
};
</script>
