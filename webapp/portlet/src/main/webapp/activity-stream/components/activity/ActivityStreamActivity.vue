<template>
  <div class="white border-radius">
    <activity-head :activity="activity" />
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