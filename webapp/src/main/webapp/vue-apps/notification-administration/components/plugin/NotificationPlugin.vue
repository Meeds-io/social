<template>
  <div>
    <v-divider />

    <v-list-item dense>
      <v-list-item-content class="px-0 pb-0 pt-2 mt-auto mb-2">
        <v-list-item-title class="text-color text-wrap">
          {{ label }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action class="ma-auto">
        <v-btn icon @click="$emit('edit')">
          <v-icon>
            fa-edit
          </v-icon>
        </v-btn>
      </v-list-item-action>
    </v-list-item>
    <v-flex class="d-flex flex-wrap">
      <template v-if="enabledNotificationLabels && enabledNotificationLabels.length">
        <v-chip
          v-for="enabledNotificationLabel in enabledNotificationLabels"
          :key="enabledNotificationLabel"
          class="ma-2"
          color="primary">
          <span class="text-truncate">
            {{ enabledNotificationLabel }}
          </span>
        </v-chip>
      </template>
      <v-chip
        v-else
        class="ma-2">
        <span class="text-truncate">
          {{ $t('UINotification.label.NoNotifications') }}
        </span>
      </v-chip>
    </v-flex>
  </div>
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
  computed: {
    label() {
      const pluginId = this.plugin.type;
      return this.$te(`NotificationAdmin.${pluginId}`) && this.$t(`NotificationAdmin.${pluginId}`) || this.settings?.pluginLabels[pluginId];
    },
    enabledNotifications() {
      return this.settings && this.settings.channelCheckBoxList && this.settings.channelCheckBoxList.filter(choice => choice.channelActive && choice.pluginId === this.plugin.type);
    },
    enabledNotificationLabels() {
      return this.enabledNotifications && this.enabledNotifications.map(plugin => this.settings.channelLabels[plugin.channelId]);
    },
  },
};
</script>

