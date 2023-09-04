<template>
  <v-card flat>
    <div class="pt-8 subtitle-1">{{ $t('NotificationAdmin.allowedNotificationChannels.title') }}</div>
    <notification-administration-plugin-group
      v-for="group in settings.groups"
      :settings="settings"
      :key="group.groupId"
      :group="group"
      @edit="openDrawer" />
    <notification-administration-plugin-drawer
      ref="drawer"
      :settings="settings" />
  </v-card>
</template>

<script>
export default {
  props: {
    settings: {
      type: Object,
      default: null,
    },
  },
  methods: {
    openDrawer(plugin, group) {
      this.$nextTick(() => this.$refs.drawer.open(plugin, group));
    },
  },
};
</script>