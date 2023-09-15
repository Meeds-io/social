<template>
  <user-notification-activity-base
    :notification="notification"
    message-key="Notification.intranet.message.one.LikePlugin"
    icon="fa-thumbs-up" />
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