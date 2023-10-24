<template>
  <v-list
    v-if="comments"
    class="flex d-flex flex-column transparent">
    <activity-comment
      v-for="comment in comments"
      :key="comment.id"
      :activity="activity"
      :comment="comment"
      :sub-comments="comment.subComments"
      :new-reply-editor="newCommentEditor && selectedCommentIdToReply === comment.id"
      :editor-options="replyLastEditorOptions"
      :comment-types="commentTypes"
      :comment-actions="commentActions"
      :comment-editing="commentEditing"
      @comment-initialized="handleCommentInitialized" />
    <activity-comment-rich-text
      v-if="allowEdit && newCommentEditor && !selectedCommentIdToReply && initialized"
      ref="commentRichEditor"
      class="mb-6"
      :activity-id="activityId"
      :space-id="spaceId"
      :comment-type-extension="commentTypeExtension"
      :options="commentLastEditorOptions"
      :label="$t('UIActivity.label.Comment')" />
  </v-list>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    comments: {
      type: Array,
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
    initialized: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    lastEditorOptions: null,
    initializedComment: 0,
  }),
  computed: {
    activityId() {
      return this.activity && this.activity.id;
    },
    spaceId() {
      return this.activity && this.activity.activityStream && this.activity.activityStream.space && this.activity.activityStream.space.id;
    },
    replyLastEditorOptions() {
      return this.lastEditorOptions && this.lastEditorOptions.parentCommentId && this.lastEditorOptions;
    },
    commentLastEditorOptions() {
      return this.lastEditorOptions && !this.lastEditorOptions.parentCommentId && this.lastEditorOptions;
    },
    commentsSize() {
      return this.comments.length;
    },
    commentTypeExtension() {
      return this.commentTypes['default'];
    },
  },
  watch: {
    comments() {
      if (!this.commentsSize) {
        this.initializedComment = 0;
      }
    },
    initializedComment(newVal, oldVal) {
      // emit comments display initialization to scroll to end
      // of the drawer only when all comments are displayed
      if (oldVal < this.commentsSize && newVal >= this.commentsSize) {
        this.$emit('initialized');
      }
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
    if (this.allowEdit) {
      this.$root.$off('activity-comment-editor-updated', this.updateEditorOptions);
    }
    this.$root.$off('activity-comment-created', this.addComment);
    this.$root.$off('activity-comment-updated', this.updateComment);
    this.$root.$off('activity-comment-deleted', this.deleteComment);

    document.removeEventListener('activity-comment-created', this.handleCommentCreated);
    document.removeEventListener('activity-comment-updated', this.handleCommentUpdated);
    document.removeEventListener('activity-comment-deleted', this.handleCommentDeleted);
  },
  methods: {
    handleCommentInitialized() {
      this.initializedComment = this.initializedComment + 1;
    },
    handleCommentDeleted(event) {
      this.$root.$emit('activity-comment-deleted', event && event.detail);
    },
    deleteComment(event) {
      const activityId = event?.activityId;
      const commentId = event?.commentId;
      const parentCommentId = event?.parentCommentId;
      if (activityId === this.activityId) {
        let commentIndex = this.comments.findIndex(tmp => tmp.id === commentId);
        if (commentIndex >= 0) {
          const comment = this.comments[commentIndex];
          this.$emit('comment-deleted', comment, commentIndex);
        } else {
          commentIndex = this.comments.findIndex(tmp => tmp.id === parentCommentId);
          const displayedComment = this.comments[commentIndex];
          const subComments = displayedComment?.subComments || [];
          const commentReplyIndex = subComments.findIndex(tmp => tmp.id === commentId);
          if (commentReplyIndex >= 0) {
            const comment = subComments[commentReplyIndex];
            comment.highlight = true;
            comment.updated = true;
            subComments.splice(commentReplyIndex, 1);
            this.$emit('comment-updated', comment, commentIndex, commentReplyIndex);
          }
        }
      }
    },
    handleCommentCreated(event) {
      const comment = event && event.detail;
      if (!comment || !comment.id) {
        console.error('no comment was sent in triggered event');
        return;
      }
      const activityId = comment.activityId;
      if (activityId !== this.activityId) {
        return;
      }
      if (comment.templateParams) {
        this.$root.$emit('activity-comment-created', comment);
      } else { // Comment not completely loaded
        this.$activityService.getActivityById(comment.id, this.$activityConstants.FULL_COMMENT_EXPAND)
          .then(activity => this.$root.$emit('activity-comment-created', activity));
      }
    },
    addComment(comment) {
      const activityId = comment && comment.activityId;
      if (activityId === this.activityId) {
        comment.highlight = true;
        comment.added = true;
        if (comment.parentCommentId) {
          const commentToUpdateIndex = this.comments.findIndex(tmp => tmp.id === comment.parentCommentId);
          if (commentToUpdateIndex >= 0) {
            const commentToUpdate = this.comments[commentToUpdateIndex];
            if (!commentToUpdate.subComments.find(tmp => tmp.id === comment.id)) {
              commentToUpdate.subComments.push(comment);
              commentToUpdate.subCommentsSize++;
            }
            this.$emit('comment-updated', commentToUpdate, commentToUpdateIndex);
          } else {
            this.$emit('comment-created', comment);
          }
        } else {
          this.$emit('comment-created', comment);
        }
      }
      this.resetEditorOptions();
    },
    handleCommentUpdated(event) {
      const comment = event && event.detail;
      if (!comment || !comment.id) {
        console.error('no comment was sent in triggered event');
        return;
      }
      const activityId = comment.activityId;
      if (activityId !== this.activityId) {
        return;
      }
      if (comment.templateParams) {
        this.$root.$emit('activity-comment-updated', comment);
      } else { // Comment not completely loaded
        this.$activityService.getActivityById(comment.id, this.$activityConstants.FULL_COMMENT_EXPAND)
          .then(activity => this.$root.$emit('activity-comment-updated', activity));
      }
    },
    updateComment(comment) {
      const activityId = comment && comment.activityId;
      if (activityId === this.activityId) {
        const commentIndex = this.comments.findIndex(tmp => tmp.id === comment.id);
        if (commentIndex >= 0) {
          comment.highlight = true;
          comment.updated = true;
          comment.subComments = this.comments[commentIndex].subComments;
          this.$emit('comment-updated', comment, commentIndex);
        } else {
          this.comments.forEach((displayedComment, index) => {
            const subComments = displayedComment.subComments || [];
            const commentReplyIndex = subComments.findIndex(tmp => tmp.id === comment.id);
            if (commentReplyIndex >= 0) {
              comment.highlight = true;
              comment.updated = true;
              subComments.splice(commentReplyIndex, 1, comment);
              this.$emit('comment-updated', displayedComment, index, commentReplyIndex);
            }
          });
        }
      }
      this.resetEditorOptions();
    },
    resetEditorOptions() {
      // Delete preserved comment or reply content
      // When a comment is added or updated in any activity
      this.lastEditorOptions = false;
      window.setTimeout(() => {
        this.lastEditorOptions = null;
      }, 5000);
    },
    updateEditorOptions(lastEditorOptions) {
      if (lastEditorOptions && this.lastEditorOptions !== false && lastEditorOptions.activityId === this.activityId) {
        this.lastEditorOptions = lastEditorOptions;
      }
    },
  },
};
</script>