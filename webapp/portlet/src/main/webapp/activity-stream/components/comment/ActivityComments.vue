<template>
  <v-list v-if="comments" class="pb-0 pt-5 border-top-color">
    <activity-comment-body
      v-for="comment in commentsToDisplay"
      :key="comment.id"
      :comment="comment"
      :sub-comments="comment.subComments"
      :new-reply-editor="newCommentEditor && selectedCommentIdToReply === comment.id"
      :editor-options="replyLastEditorOptions"
      :comment-actions="commentActions"
      :comment-editing="commentEditing" />
    <activity-comment-rich-text
      v-if="newCommentEditor && !selectedCommentIdToReply"
      ref="commentRichEditor"
      class="me-4 ms-14 mb-6"
      :activity-id="activityId"
      :options="commentLastEditorOptions"
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
    commentEditing: {
      type: Object,
      default: null,
    },
    newCommentEditor: {
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
    replyLastEditorOptions() {
      return this.lastEditorOptions && this.lastEditorOptions.parentCommentId && this.lastEditorOptions;
    },
    commentLastEditorOptions() {
      return this.lastEditorOptions && !this.lastEditorOptions.parentCommentId && this.lastEditorOptions;
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
      this.$root.$on('activity-comment-editor-updated', this.updateEditorOptions);
    }
    this.$root.$on('activity-comment-created', this.addComment);
    this.$root.$on('activity-comment-updated', this.updateComment);
    this.$root.$on('activity-comment-deleted', this.deleteComment);

    document.addEventListener('activity-comment-created', this.handleCommentCreated);
    document.addEventListener('activity-comment-updated', this.handleCommentUpdated);
    document.addEventListener('activity-comment-deleted', this.handleCommentDeleted);
  },
  beforeDestroy() {
    document.removeEventListener('activity-comment-created', this.handleCommentCreated);
    document.removeEventListener('activity-comment-updated', this.handleCommentUpdated);
    document.removeEventListener('activity-comment-deleted', this.handleCommentDeleted);
  },
  methods: {
    handleCommentDeleted(event) {
      this.$root.$emit('activity-comment-deleted', event && event.detail);
    },
    deleteComment(comment) {
      const activityId = comment && comment.activityId;
      if (activityId === this.activityId) {
        const commentIndex = this.comments.findIndex(tmp => tmp.id === comment.id);
        if (commentIndex >= 0) {
          const comment = this.comments[commentIndex];
          this.comments.splice(commentIndex, 1);
          this.$emit('comment-deleted', comment);
        }
      }
    },
    handleCommentCreated(event) {
      this.$root.$emit('activity-comment-created', event && event.detail);
    },
    addComment(comment) {
      const activityId = comment && comment.activityId;
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
    handleCommentUpdated(event) {
      this.$root.$emit('activity-comment-updated', event && event.detail);
    },
    updateComment(comment) {
      const activityId = comment && comment.activityId;
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
    updateEditorOptions(lastEditorOptions) {
      if (lastEditorOptions && lastEditorOptions.activityId === this.activityId) {
        this.lastEditorOptions = lastEditorOptions;
      }
    },
  },
};
</script>