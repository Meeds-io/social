<template>
  <div class="d-inline-flex pe-1">
    <v-btn
      :id="`CommentLink${activityId}`"
      :title="$t('UIActivity.label.Comment')"
      :class="commentTextColorClass"
      class="pa-0 me-0"
      text
      link
      x-small
      @click="openCommentsDrawer">
      <span>
        {{ $t('UIActivity.label.Reply') }}
      </span>
    </v-btn>
    <v-btn
      v-if="subCommentsSize"
      :id="`RepliesListLink${commentId}`"
      :title="$t('UIActivity.label.ViewAllReplies', {0: subCommentsSize})"
      class="primary--text font-weight-bold"
      x-small
      icon
      @click="openReplies">
      ({{ subCommentsSize }})
    </v-btn>
  </div>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    comment: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    hasCommented: false,
  }),
  computed: {
    activityId() {
      return this.comment && this.comment.activityId;
    },
    commentId() {
      return this.comment && (this.comment.parentCommentId || this.comment.id) || '';
    },
    commentTextColorClass() {
      return this.hasCommented && 'primary--text' || '';
    },
    subCommentsSize() {
      return this.comment && this.comment.subCommentsSize || 0;
    },
  },
  watch: {
    comment() {
      this.checkWhetherCommented();
    },
  },
  created() {
    this.$root.$on('activity-comment-created', this.handleCommentCreated);
    this.$root.$on('activity-comment-deleted', this.handleCommentDeleted);
    this.checkWhetherCommented();
  },
  beforeDestroy() {
    this.$root.$off('activity-comment-created', this.handleCommentCreated);
    this.$root.$off('activity-comment-deleted', this.handleCommentDeleted);
  },
  methods: {
    handleCommentCreated(comment) {
      if (comment.activityId === this.activityId && this.comment.id === comment.parentCommentId) {
        this.hasCommented = true;
      }
    },
    handleCommentDeleted(comment) {
      if (comment.activityId === this.activityId && this.comment.id === comment.parentCommentId) {
        this.checkWhetherCommented();
      }
    },
    checkWhetherCommented() {
      this.hasCommented = this.comment && this.comment.hasCommented === 'true';
    },
    openCommentsDrawer() {
      document.dispatchEvent(new CustomEvent('activity-comments-display', {detail: {
        activity: this.activity,
        commentId: this.commentId,
        newComment: true,
        offset: 0,
        limit: 200, // To display all
      }}));
    },
    openReplies() {
      document.dispatchEvent(new CustomEvent('activity-comments-display', {detail: {
        activity: this.activity,
        commentId: this.commentId,
        highlightRepliesCommentId: this.commentId,
        offset: 0,
        limit: 200, // To display all
      }}));
    },
  },
};
</script>