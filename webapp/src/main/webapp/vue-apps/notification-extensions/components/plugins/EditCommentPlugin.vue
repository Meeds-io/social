<template>
  <user-notification-activity-base
    icon="fa-comment"
    message-key="Notification.intranet.message.one.EditCommentPlugin"
    :notification="notification" />
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