<template>
  <div>
    <div class="pt-8 text-title">{{ $t('NotificationAdmin.allowedNotificationChannels.title') }}</div>
    <notification-administration-plugin-group
      v-for="group in settings.groups"
      :settings="settings"
      :key="group.groupId"
      :group="group" />
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
};
</script>
