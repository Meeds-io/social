<template>
  <div
    v-if="activityLoading"
    :key="activity.id"
    class="white border-radius activity-detail flex d-flex flex-column mb-6 contentBox">
    <v-progress-circular
      color="primary"
      size="32"
      indeterminate
      class="mx-auto my-10" />
  </div>
  <transition v-else-if="activityPinned">
    <activity-pinned
      :key="activity.id"
      :activity="activity"
      :activity-types="activityTypes"
      :activity-actions="activityActions"
      :comment-types="commentTypes"
      :comment-actions="commentActions"
      :is-activity-detail="isActivityDetail"
      class="mb-6 contentBox" />
  </transition>
  <transition v-else>
    <activity-stream-activity
        v-show="!activityDeleted"
        :key="activity.id"
        :activity="activity"
        :activity-types="activityTypes"
        :activity-actions="activityActions"
        :comment-types="commentTypes"
        :comment-actions="commentActions"
        :is-activity-detail="isActivityDetail"
        class="mb-6 contentBox"
        @loaded="$emit('loaded')" />
  </transition>
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
    streamFilter: {
      type: String,
      default: null,
    },
  },
  created() {
    document.addEventListener('activity-pinned', () => this.displayAlert(this.$t('UIActivity.label.SuccessfullyPinned')));
    document.addEventListener('activity-unpinned', () => this.displayAlert(this.$t('UIActivity.label.SuccessfullyUnpinned')));
  },
  computed: {
    activityLoading() {
      return this.activity && this.activity.loading;
    },
    activityDeleted() {
      return this.activity && this.activity.deleted;
    },
    activityPinned() {
      return this.activity && this.activity.pinned && (this.streamFilter === null || this.streamFilter === 'all_stream') && eXo.env.portal.spaceId;
    },
  },
  methods: {
    displayAlert(message, type) {
      document.dispatchEvent(new CustomEvent('notification-alert', {detail: {
        message,
        type: type || 'success',
      }}));
    },
  }
};
</script>