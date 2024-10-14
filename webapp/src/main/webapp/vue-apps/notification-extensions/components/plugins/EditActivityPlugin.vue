<template>
  <user-notification-activity-base
    :notification="notification"
    message-key="Notification.intranet.message.one.EditActivityPlugin"
    icon="fa-stream" />
</template>
<script>
export default {
  props: {
    notification: {
      type: Object,
      default: null,
    },
  },
};
</script>