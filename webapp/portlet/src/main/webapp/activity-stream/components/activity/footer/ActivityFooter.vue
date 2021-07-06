<template>
  <div class="actionsDetailsWrapper mb-0 py-0 pe-4 no-border-bottom">
    <activity-reactions
      :activity-id="activityId"
      :activity="activity"
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
  data: () => ({
    likersCount: 0,
    commentsCount: 0,
    kudosCount: 0,
    likers: [],
  }),
  computed: {
    activityId() {
      return this.activity && this.activity.id;
    },
  },
  created() {
    this.$root.$on('activity-comment-created', this.checkCommentsSize);
    this.$root.$on('activity-comment-updated', this.checkCommentsSize);
    this.$root.$on('activity-comment-deleted', this.checkCommentsSize);
    this.$root.$on('activity-comments-retrieved', this.checkCommentsSize);
    this.$root.$on('activity-liked', this.checkLikesSize);
    this.checkCommentsSize();
    this.checkLikesSize();
  },
  beforeDestroy() {
    this.$root.$off('activity-comment-created', this.checkCommentsSize);
    this.$root.$off('activity-comment-updated', this.checkCommentsSize);
    this.$root.$off('activity-comment-deleted', this.checkCommentsSize);
    this.$root.$off('activity-comments-retrieved', this.checkCommentsSize);
    this.$root.$off('activity-liked', this.checkLikesSize);
  },
  methods: {
    checkLikesSize() {
      this.likers = this.activity.likes || [];
      this.likersCount = this.activity.likesCount && Number(this.activity.likesCount);
    },
    checkCommentsSize() {
      this.commentsCount = this.activity.commentsSize;
    },
  },
};
</script>