<template>
  <div v-if="manageNotification" class="border-radius border-color ma-4">
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
      default: false
    }
  },
  computed: {
    label() {
      return this.settings && this.settings.groupsLabels && this.settings.groupsLabels[this.group.groupId];
    },
    manageNotification() {
      return this.group && this.group.pluginInfos && this.group.pluginInfos.length ;
    },
  },
  methods: {
    openNotificationSettingDetail() {
      // TODO
    },
  },
};
</script>

