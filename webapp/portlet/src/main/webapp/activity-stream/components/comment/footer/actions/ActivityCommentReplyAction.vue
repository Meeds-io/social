<template>
  <v-btn
    :id="`CommentLink${activityId}`"
    :title="$t('UIActivity.label.Comment')"
    :class="commentTextColorClass"
    class="pa-0 mx-2"
    text
    link
    x-small
    @click="openCommentsDrawer">
    <span>
      {{ $t('UIActivity.label.Reply') }}
    </span>
  </v-btn>
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
    hasCommented() {
      return this.hasCommented && 'primary--text' || '';
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
        this.hasCommented = this.comment && this.comment.subComments && this.comment.subComments.filter(tmp => tmp.id !== comment.id).length;
      }
    },
    checkWhetherCommented() {
      this.hasCommented = this.comment && this.comment.subComments && this.comment.subComments.length;
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
  },
};
</script>