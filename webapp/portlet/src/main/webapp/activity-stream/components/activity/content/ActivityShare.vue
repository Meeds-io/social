<template>
  <div v-if="originalActivity && activityTypes" class="border-color border-radius ms-4 me-5 my-3">
    <activity-stream-activity
      :activity="originalActivity"
      :activity-types="activityTypes"
      hide-footer
      hide-menu />
  </div>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    activityTypes: {
      type: Object,
      default: null,
    },
  },
  computed: {
    originalActivity() {
      return this.activity && this.activity.originalActivity;
    },
  },
  data: () => ({
    activityTypes: null,
  }),
};
</script>