<template>
  <div class="actionsDetailsWrapper mb-0 py-0 pe-4 border-top-color">
    <activity-reactions
      :activity-id="activityId"
      :likers="likers"
      :likers-number="likersCount"
      :comment-number="commentsCount"
      class="activityReactionsContainer" />
    <activity-actions
      :activity="activity"
      :activity-type-extension="activityTypeExtension"
      class="me-1" />
  </div>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    activityTypeExtension: {
      type: Object,
      default: null,
    },
  },
  computed: {
    activityId() {
      return this.activity && this.activity.id;
    },
    likersCount() {
      return this.activity && this.activity.likesCount && Number(this.activity.likesCount) || 0;
    },
    commentsCount() {
      return this.activity && this.activity.commentsCount && Number(this.activity.commentsCount) || 0;
    },
    likers() {
      return this.activity && this.activity.likes || [];
    },
  },
};
</script>