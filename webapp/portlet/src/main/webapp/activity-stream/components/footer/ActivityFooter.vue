<template>
  <div class="actionsDetailsWrapper border-top-color">
    <activity-reactions
      :activity-id="activityId"
      :likers="likes"
      :likers-number="likesCount"
      :comment-number="commentsCount"
      class="activityReactionsContainer" />
    <activity-actions
      :activity="activity"
      :activity-type-extension="activityTypeExtension" />
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
    likesCount() {
      return this.activity && this.activity.likesCount && Number(this.activity.likesCount) || 0;
    },
    commentsCount() {
      return this.activity && this.activity.commentsCount && Number(this.activity.commentsCount) || 0;
    },
    likes() {
      return this.activity && this.activity.likes || [];
    },
  },
};
</script>