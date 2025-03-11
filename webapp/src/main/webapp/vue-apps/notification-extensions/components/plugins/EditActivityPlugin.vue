<template>
  <user-notification-activity-base
    icon="fa-stream"
    message-key="Notification.intranet.message.one.EditActivityPlugin"
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