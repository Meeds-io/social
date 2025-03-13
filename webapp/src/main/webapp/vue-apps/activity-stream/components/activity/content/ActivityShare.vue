<template>
  <div
    v-if="originalActivity && activityTypes"
    class="border-color border-radius my-3">
    <activity-stream-activity
      :activity="originalActivity"
      :activity-types="activityTypes"
      class="px-4"
      hide-footer
      hide-menu
      :is-activity-detail="isActivityDetail"
      is-activity-shared />
  </div>
</template>

<script>
  export default {
    props: {
      activity: {
        type: Object,
        default: null,
      },
      isActivityDetail: {
        type: Boolean,
        default: false,
      },
      activityTypes: {
        type: Object,
        default: null,
      },
    },
    computed: {
      originalActivity () {
        return this.activity && this.activity.originalActivity && Object.assign({
          parentActivity: this.activity,
        }, this.activity.originalActivity);
      },
    },
  };
</script>