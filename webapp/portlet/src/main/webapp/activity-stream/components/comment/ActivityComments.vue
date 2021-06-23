<template>
  <v-list v-if="comments" class="pb-0 pt-5 border-top-color">
    <activity-comment
      v-for="comment in commentsToDisplay"
      :key="comment.id"
      :comment="comment"
      :sub-comments="comment.subComments"
      :display-reply="commentEditorDisplay && selectedCommentIdToReply === comment.id"
      :allow-edit="allowEdit"
      :editor-options="lastEditorOptions"
      :comment-actions="commentActions" />
    <activity-comment-rich-text
      v-if="displayCommentEditor"
      ref="commentRichEditor"
      key="commentRichEditor"
      class="me-4 ms-14 mb-6"
      :activity-id="activityId"
      :options="lastEditorOptions"
      :label="$t('UIActivity.label.Comment')" />
  </v-list>
</template>

<script>
export default {
  props: {
    activityId: {
      type: String,
      default: null,
    },
    comments: {
      type: String,
      default: null,
    },
    commentActions: {
      type: Object,
      default: null,
    },
    commentEditorDisplay: {
      type: Boolean,
      default: false,
    },
    selectedCommentIdToReply: {
      type: String,
      default: null,
    },
    allowEdit: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    lastEditorOptions: null,
  }),
  computed: {
    displayCommentEditor() {
      return this.commentEditorDisplay && !this.selectedCommentIdToReply;
    },
    commentsToDisplay() {
      const commentsToDisplay = {};
      this.comments.forEach(comment => {
        if (!comment.parentCommentId) {
          comment.subComments = [];
          commentsToDisplay[comment.id] = comment;
        }
      });
      this.comments.forEach(comment => {
        if (comment.parentCommentId) {
          const parentComment = commentsToDisplay[comment.parentCommentId];
          const subComments = parentComment && parentComment.subComments;
          if (subComments && !subComments.find(subComment => subComment.id === comment.id)) {
            subComments.push(comment);
          }
        }
      });
      return Object.values(commentsToDisplay);
    },
  },
  created() {
    if (this.allowEdit) {
      document.addEventListener('activity-comment-editor-updated', this.updateEditorOptions);
    }
    document.addEventListener('activity-comment-created', this.addComment);
    document.addEventListener('activity-comment-deleted', this.deleteComment);
    document.addEventListener('activity-comment-updated', this.updateComment);
  },
  beforeDestroy() {
    if (this.allowEdit) {
      document.removeEventListener('activity-comment-editor-updated', this.updateEditorOptions);
    }
    document.removeEventListener('activity-comment-created', this.addComment);
    document.removeEventListener('activity-comment-deleted', this.deleteComment);
    document.removeEventListener('activity-comment-updated', this.updateComment);
  },
  methods: {
    updateEditorOptions(event) {
      const lastEditorOptions = event && event.detail;
      if (lastEditorOptions && lastEditorOptions.activityId === this.activityId) {
        this.lastEditorOptions = lastEditorOptions;
      }
    },
    deleteComment(event) {
      const params = event && event.detail;
      if (params && params.activityId === this.activityId && params.commentId) {
        const commentIndex = this.comments.findIndex(comment => comment.id === params.commentId);
        if (commentIndex >= 0) {
          const comment = this.comments[commentIndex];
          this.comments.splice(commentIndex, 1);
          this.$emit('comment-deleted', comment);
        }
      }
    },
    addComment(event) {
      const activityId = event && event.detail && event.detail.activityId;
      const comment = event && event.detail && event.detail.comment;
      if (activityId === this.activityId) {
        comment.highlight = true;
        this.comments.push(comment);
        window.setTimeout(() => {
          comment.highlight = false;
        }, 5000);
        this.$emit('comment-created', comment);
      }
      // Delete preserved comment or reply content
      // When a new comment is added in any activity
      this.lastEditorOptions = null;
    },
    updateComment(event) {
      const activityId = event && event.detail && event.detail.activityId;
      const comment = event && event.detail && event.detail.comment;
      if (activityId === this.activityId) {
        const commentIndex = this.comments.findIndex(tmp => tmp.id === comment.id);
        if (commentIndex >= 0) {
          comment.highlight = true;
          this.comments.splice(commentIndex, 1, comment);
          this.$emit('comment-updated', comment);
          window.setTimeout(() => {
            comment.highlight = false;
          }, 5000);
        }
      }
      // Delete preserved comment or reply content
      // When a comment is added or updated in any activity
      this.lastEditorOptions = null;
    },
  },
};
</script>