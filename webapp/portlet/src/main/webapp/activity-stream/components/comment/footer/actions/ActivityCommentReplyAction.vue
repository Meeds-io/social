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
    commentColorClass() {
      return this.hasCommented && 'primary--text' || 'disabled--text';
    },
    commentTextColorClass() {
      return this.hasCommented && 'primary--text' || '';
    },
  },
  created() {
    document.addEventListener('activity-comment-created', (event) => {
      const activityId = event && event.detail && event.detail.activityId;
      const commentId = event && event.detail && event.detail.commentId;
      if (activityId === this.activityId && commentId === this.commentId) {
        this.hasCommented = true;
      }
    });
    this.hasCommented = this.activity && this.activity.hasCommented === 'true';
  },
  methods: {
    openCommentsDrawer() {
      document.dispatchEvent(new CustomEvent('activity-comments-display', {detail: {
        activityId: this.activityId,
        commentId: this.commentId,
        offset: 0,
        limit: 200, // To display all
        displayCommentEditor: true,
      }}));
    },
  },
};
</script>