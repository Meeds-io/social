<template>
  <div v-if="manageNotification && isEnabledNotificationGroup" class="mb-4">
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
          class="text-center text-caption text-truncate flex-shrink-0">
          {{ settings.channelLabels && settings.channelLabels[channelId] }}
        </div>
      </v-list-item-action>
    </v-list-item>

    <template v-for="(plugin, index) in group.pluginInfos">
      <v-divider v-if="index > 0" :key="`divider-${plugin.type}`" />
      <user-setting-notification-plugin
        :plugin="plugin"
        :key="plugin.type"
        :settings="settings"
        :columns="columns" />
    </template>
  </div>
</template>

<script>
// Left to right; a channel not listed here goes leftmost
const CHANNELS_ORDER = ['SPACE_WEB_CHANNEL', 'WEB_CHANNEL', 'MAIL_CHANNEL'];

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
    isEnabledNotificationGroup: true,
    extensions: [],
  }),
  computed: {
    icon() {
      const pluginTypes = (this.group?.pluginInfos || []).map(plugin => plugin.type);
      const extension = this.extensions.find(ext => ext.plugins?.some(type => pluginTypes.includes(type)))
        || this.extensions.find(ext => ext.name === this.group?.groupId);
      return extension?.icon;
    },
    columns() {
      const pluginTypes = (this.group?.pluginInfos || []).map(plugin => plugin.type);
      const allowedChannels = new Set((this.settings?.channelCheckBoxList || [])
        .filter(choice => choice.allowed && pluginTypes.includes(choice.pluginId))
        .map(choice => choice.channelId));
      return (this.settings?.channels || [])
        .filter(channelId => allowedChannels.has(channelId))
        .map((channelId, index) => ({channelId, index, rank: CHANNELS_ORDER.indexOf(channelId) + 1}))
        .sort((a, b) => a.rank - b.rank || a.index - b.index)
        .map(item => item.channelId);
    },
    columnStyle() {
      const width = this.$vuetify.breakpoint.smAndDown ? 64 : 96;
      return `width: ${width}px; min-width: ${width}px;`;
    },
    label() {
      return this.settings && this.settings.groupsLabels && this.settings.groupsLabels[this.group.groupId];
    },
    manageNotification() {
      return this.group && this.group.pluginInfos && this.group.pluginInfos.length ;
    },
  },
  created() {
    document.addEventListener('extension-WebNotification-notification-group-extension-updated', this.refreshExtensions);
    this.refreshExtensions();
    this.init();
  },
  beforeDestroy() {
    document.removeEventListener('extension-WebNotification-notification-group-extension-updated', this.refreshExtensions);
  },
  methods: {
    refreshExtensions() {
      this.extensions = extensionRegistry.loadExtensions('WebNotification', 'notification-group-extension') || [];
    },
    init() {
      const listPlugins = [];
      this.group?.pluginInfos?.forEach(plugin => {
        if (this.settings && this.settings.channelCheckBoxList && this.settings.channelCheckBoxList.filter(choice => choice.channelActive && choice.pluginId === plugin.type).length) {
          listPlugins.push(plugin);
        }
      });
      this.isEnabledNotificationGroup = listPlugins && listPlugins.length;
    }
  }
};
</script>

