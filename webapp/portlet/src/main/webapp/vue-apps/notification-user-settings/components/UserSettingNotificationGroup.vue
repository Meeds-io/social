<template>
  <div v-if="manageNotification && isEnabledNotificationGroup" class="border-radius border-color ma-4">
    <v-list-item dense>
      <v-list-item-content>
        <v-list-item-title class="text-color font-weight-bold subtitle-1">
          {{ label }}
        </v-list-item-title>
      </v-list-item-content>
    </v-list-item>

    <user-setting-notification-plugin
      v-for="plugin in group.pluginInfos"
      :plugin="plugin"
      :key="plugin.type"
      :settings="settings"
      :digest-mail-notification-enabled="digestMailNotificationEnabled"
      @edit="$emit('edit', plugin, group)" />
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
    digestMailNotificationEnabled: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    isEnabledNotificationGroup: true,
  }),
  computed: {
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

