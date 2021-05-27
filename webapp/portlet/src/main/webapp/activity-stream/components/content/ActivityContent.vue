<template>
  <div class="pa-4 uiActivityStreamWrapper">
    <activity-content-message :activity="activity" />
  </div>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
  },
};
</script>