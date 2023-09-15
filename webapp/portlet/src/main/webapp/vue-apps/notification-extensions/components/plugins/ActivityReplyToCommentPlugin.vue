<template>
  <user-notification-activity-base
    :notification="notification"
    message-key="Notification.intranet.message.one.ActivityReplyToCommentPlugin"
    icon="fa-comment"
    reply />
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