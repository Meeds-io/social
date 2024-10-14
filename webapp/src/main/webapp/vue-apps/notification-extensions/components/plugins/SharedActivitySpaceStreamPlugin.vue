<template>
  <user-notification-activity-base
    :notification="notification"
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