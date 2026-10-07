<template>
  <div v-if="hasPlugins" class="my-4">
    <v-list-item dense>
      <v-icon
        v-if="icon"
        size="20"
        class="icon-default-color me-3">
        {{ icon }}
      </v-icon>
      <v-list-item-content>
        <v-list-item-title class="text-title">
          {{ label }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action class="d-flex flex-row align-center justify-end my-1">
        <div
          v-for="channelId in columns"
          :key="channelId"
          :style="columnStyle"
          class="text-center text-truncate flex-shrink-0">
          {{ settings.channelLabels && settings.channelLabels[channelId] }}
        </div>
      </v-list-item-action>
    </v-list-item>

    <template v-for="(plugin, index) in group.pluginInfos">
      <v-divider v-if="index > 0" :key="`divider-${plugin.type}`" />
      <notification-administration-plugin
        :plugin="plugin"
        :key="plugin.type"
        :settings="settings"
        :columns="columns" />
    </template>
  </div>
</template>

<script>
import {getChannelColumns, getColumnStyle, getGroupIcon} from '../../../common/js/NotificationSettingsLayout.js';

export default {
  props: {
    group: {
      type: Object,
      default: null,
    },
    settings: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    extensions: [],
  }),
  computed: {
    hasPlugins() {
      return this.group?.pluginInfos?.length;
    },
    label() {
      return this.settings && this.settings.groupsLabels && this.settings.groupsLabels[this.group.groupId];
    },
    icon() {
      return getGroupIcon(this.settings, this.group, this.extensions);
    },
    columns() {
      return getChannelColumns(this.settings, this.group);
    },
    columnStyle() {
      return getColumnStyle(this.$vuetify.breakpoint.smAndDown);
    },
  },
  created() {
    document.addEventListener('extension-WebNotification-notification-group-extension-updated', this.refreshExtensions);
    this.refreshExtensions();
  },
  beforeDestroy() {
    document.removeEventListener('extension-WebNotification-notification-group-extension-updated', this.refreshExtensions);
  },
  methods: {
    refreshExtensions() {
      this.extensions = extensionRegistry.loadExtensions('WebNotification', 'notification-group-extension') || [];
    },
  },
};
</script>
