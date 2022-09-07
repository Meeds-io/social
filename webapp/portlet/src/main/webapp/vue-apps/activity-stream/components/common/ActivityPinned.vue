<template>
  <div class="pinnedActivity">
    <div
      class="my-auto flex-grow-1 py-2 ps-4 pe-1">
      <v-icon
        color="grey"
        :size="16">
        fa-thumbtack
      </v-icon>
      <span class="ms-2">{{ $t('UIActivity.label.PinnedActivity') }}</span>
    </div>
    <activity-stream-activity
      :key="activity.id"
      :activity="activity"
      :activity-types="activityTypes"
      :activity-actions="activityActions"
      :comment-types="commentTypes"
      :comment-actions="commentActions"
      :is-activity-detail="isActivityDetail"
      @loaded="$emit('loaded')" />
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
    activityActions: {
      type: Object,
      default: null,
    },
    commentTypes: {
      type: Object,
      default: null,
    },
    commentActions: {
      type: Object,
      default: null,
    },
    isActivityDetail: {
      type: Boolean,
      default: false,
    },
    isActivityShared: {
      type: Boolean,
      default: false,
    },
    hideFooter: {
      type: Boolean,
      default: false,
    },
    hideMenu: {
      type: Boolean,
      default: false,
    },
  },
};
</script>
