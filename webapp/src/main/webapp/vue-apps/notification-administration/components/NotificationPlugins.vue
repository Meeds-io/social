<template>
  <div>
    <div class="pt-8 text-title">
      {{ $t('NotificationAdmin.allowedNotificationChannels.title') }}
    </div>
    <notification-administration-plugin-group
      v-for="group in settings.groups"
      :key="group.groupId"
      :group="group"
      :settings="settings"
      @edit="openDrawer" />
    <notification-administration-plugin-drawer
      ref="drawer"
      :settings="settings" />
  </div>
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
      openDrawer (plugin, group) {
        this.$nextTick(() => this.$refs.drawer.open(plugin, group));
      },
    },
  };
</script>