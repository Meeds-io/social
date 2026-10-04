<template>
  <div v-if="manageNotification && isEnabledNotificationGroup" class="mb-4">
    <v-list-item dense>
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
  }),
  computed: {
    columns() {
      const pluginTypes = (this.group?.pluginInfos || []).map(plugin => plugin.type);
      const allowedChannels = new Set((this.settings?.channelCheckBoxList || [])
        .filter(choice => choice.allowed && pluginTypes.includes(choice.pluginId))
        .map(choice => choice.channelId));
      return (this.settings?.channels || []).filter(channelId => allowedChannels.has(channelId));
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
    this.init();
  },
  methods: {
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

