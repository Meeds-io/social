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
          :disabled="!optionsByChannel[channelId].channelActive"
          class="mt-0 pt-0"
          hide-details
          @change="toggle(optionsByChannel[channelId], $event)" />
      </div>
    </v-list-item-action>
  </v-list-item>
</template>

<script>
import {getColumnStyle, hasPendingSettingSaves, queueSettingSave} from '../../common/js/NotificationSettingsLayout.js';

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
    savedChannels: {},
    pendingChannels: {},
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
      return getColumnStyle(this.$vuetify.breakpoint.smAndDown);
    },
  },
  watch: {
    channelOptions: {
      immediate: true,
      handler() {
        const channels = {};
        this.channelOptions.forEach(option => {
          // Settings read before a save of this channel settled would revert its switch
          if (this.pendingChannels[option.channelId]) {
            channels[option.channelId] = this.channels[option.channelId];
          } else {
            channels[option.channelId] = !!(option.allowed && option.active && option.channelActive);
            this.savedChannels[option.channelId] = channels[option.channelId];
          }
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
      this.$set(this.channels, channelId, !!value);
      this.pendingChannels[channelId] = (this.pendingChannels[channelId] || 0) + 1;
      return queueSettingSave(() => fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/notifications/settings/${eXo.env.portal.userName}/plugin/${this.plugin.type}`, {
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
      })).then(() => {
        this.savedChannels[channelId] = !!value;
      }).catch(() => {
        // A later switch of the same channel, still queued, holds the value to display
        if (this.pendingChannels[channelId] === 1) {
          this.$set(this.channels, channelId, this.savedChannels[channelId]);
        }
        this.$root.$emit('alert-message', this.$t('UserSettings.notifications.error.save'), 'error');
      }).finally(() => {
        this.pendingChannels[channelId]--;
        // Read once every queued save settled, so that the read reflects all of them
        if (!hasPendingSettingSaves()) {
          this.$root.$emit('refresh');
        }
      });
    },
  },
};
</script>
