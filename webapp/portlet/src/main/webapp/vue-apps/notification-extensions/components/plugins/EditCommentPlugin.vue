<template>
  <user-notification-activity-base
    :notification="notification"
    message-key="Notification.intranet.message.one.EditCommentPlugin"
    icon="fa-comment" />
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