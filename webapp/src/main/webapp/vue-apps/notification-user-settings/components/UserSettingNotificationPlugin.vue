<template>
  <div v-if="isEnabledNotifications.length">
    <v-divider />

    <v-list-item dense>
      <v-list-item-content class="px-0 pb-0 pt-2 mt-auto mb-2">
        <v-list-item-title class="text-color text-wrap">
          {{ label }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action class="ma-auto">
        <v-btn
          small
          icon
          @click="$emit('edit')">
          <v-icon size="18" class="icon-default-color">fa-edit</v-icon>
        </v-btn>
      </v-list-item-action>
    </v-list-item>
    <v-flex v-if="hasNotificationSettings" class="d-flex flex-wrap">
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
    </v-flex>
    <v-list-item v-else dense>
      <v-list-item-content class="pa-0">
        <v-list-item-subtitle>
          {{ $t('UINotification.label.NoNotifications') }}
        </v-list-item-subtitle>
      </v-list-item-content>
    </v-list-item>
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
      return this.settings && this.settings.pluginLabels && this.settings.pluginLabels[this.plugin.type];
    },
    hasNotificationSettings() {
      return this.enabledNotificationLabels && this.enabledNotificationLabels.length;
    },
    enabledNotifications() {
      return this.settings && this.settings.channelCheckBoxList && this.settings.channelCheckBoxList.filter(choice => choice.active && choice.channelActive && choice.pluginId === this.plugin.type);
    },
    isEnabledNotifications() {
      return this.settings && this.settings.channelCheckBoxList && this.settings.channelCheckBoxList.filter(choice => choice.channelActive && choice.pluginId === this.plugin.type);
    },
    enabledNotificationLabels() {
      return this.enabledNotifications && this.enabledNotifications.map(plugin => this.settings.channelLabels[plugin.channelId]);
    },
  },
};
</script>

