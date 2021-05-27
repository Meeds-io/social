<template>
  <div v-sanitized-html="activityContent" class="postContent"></div>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
  },
  computed: {
    activityContent() {
      return this.activity && this.activity.title;
    },
  },
};
</script>