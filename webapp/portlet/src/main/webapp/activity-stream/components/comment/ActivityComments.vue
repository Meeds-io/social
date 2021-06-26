<template>
  <v-list
    v-if="comments"
    class="flex d-flex flex-column">
    <activity-comment-body
      v-for="comment in commentsToDisplay"
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
      v-if="allowEdit && newCommentEditor && !selectedCommentIdToReply"
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
  },
  data: () => ({
    lastEditorOptions: null,
    initializedComment: 0,
  }),
  computed: {
    activityId() {
      return this.activity && this.activity.id;
    },
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
    commentsSize() {
      return this.commentsToDisplay.length;
    },
  },
  watch: {
    comments() {
      if (!this.commentsSize) {
        this.initializedComment = 0;
      }
    },
    initializedComment(newVal, oldVal) {
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
    deleteComment(comment) {
      const activityId = comment && comment.activityId;
      if (activityId === this.activityId) {
        const commentIndex = this.comments.findIndex(tmp => tmp.id === comment.id);
        if (commentIndex >= 0) {
          const comment = this.comments[commentIndex];
          this.$emit('comment-deleted', comment, commentIndex);
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
        this.$emit('comment-created', comment);
        window.setTimeout(() => {
          comment.highlight = false;
        }, 5000);
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
          this.$emit('comment-updated', comment, commentIndex);
          window.setTimeout(() => {
            comment.highlight = false;
          }, 5000);
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