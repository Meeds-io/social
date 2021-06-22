<template>
  <v-list v-if="comments" class="pb-0 pt-5 border-top-color">
    <activity-comment
      v-for="comment in commentsToDisplay"
      :key="comment.id"
      :comment="comment"
      :display-reply="commentEditorDisplay && selectedCommentIdToReply === comment.id"
      :editor="editor"
      :editor-options="lastEditorOptions" />
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
    commentEditorDisplay: {
      type: Boolean,
      default: false,
    },
    selectedCommentIdToReply: {
      type: String,
      default: null,
    },
    editor: {
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
        if (comment.parentCommentId) {
          const subComments = commentsToDisplay[comment.parentCommentId].subComments;
          if (!subComments.find(subComment => subComment.id === comment.id)) {
            subComments.push(comment);
          }
        } else {
          commentsToDisplay[comment.id] = comment;
          if (!comment.subComments) {
            comment.subComments = [];
          }
        }
      });
      return Object.values(commentsToDisplay);
    },
  },
  created() {
    if (this.editor) {
      document.addEventListener('activity-comment-editor-updated', this.updateEditorOptions);
    }
    document.addEventListener('activity-commented', this.updateComments);
  },
  methods: {
    updateEditorOptions(event) {
      const lastEditorOptions = event && event.detail;
      if (lastEditorOptions && lastEditorOptions.activityId === this.activityId) {
        this.lastEditorOptions = lastEditorOptions;
      }
    },
    updateComments(event) {
      const activityId = event && event.detail && event.detail.activityId;
      const comment = event && event.detail && event.detail.comment;
      if (activityId === this.activityId) {
        comment.highlight = true;
        this.comments.push(comment);
        window.setTimeout(() => {
          comment.highlight = false;
        }, 5000);
      }
      // Delete preserved comment or reply content
      // When a new comment is added in any activity
      this.lastEditorOptions = null;
    },
  },
};
</script>