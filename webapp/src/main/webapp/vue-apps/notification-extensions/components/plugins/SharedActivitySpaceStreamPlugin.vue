<template>
  <user-notification-activity-base
    icon="fa-stream"
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