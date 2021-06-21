<template>
  <v-list v-if="comments" class="pb-0 pt-5 border-top-color">
    <activity-comment
      v-for="comment in commentsToDisplay"
      :key="comment.id"
      :comment="comment"
      :display-reply="commentEditorDisplay && selectedCommentIdToReply === comment.id" />
    <div v-if="displayCommentEditor" class="me-4 ms-14 mb-6">
      <v-list-item-content class="col-auto px-0 pt-1 pb-0 mb-2 flex-shrink-1 border-box-sizing">
        <exo-activity-rich-editor
          ref="commentEditor"
          v-model="message"
          :ck-editor-type="ckEditorId"
          :max-length="$activityConstants.COMMENT_MAX_LENGTH"
          :placeholder="$t('activity.composer.placeholder', {0: $activityConstants.COMMENT_MAX_LENGTH})"
          autofocus
          @ready="$emit('editor-loaded')" />
      </v-list-item-content>
      <v-btn
        :loading="commenting"
        :disabled="disableButton"
        class="btn btn-primary"
        @click="postComment">
        {{ $t('UIActivity.label.Comment') }}
      </v-btn>
    </div>
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
  },
  data: () => ({
    commenting: false,
    message: null,
  }),
  computed: {
    displayCommentEditor() {
      return this.commentEditorDisplay && !this.selectedCommentIdToReply;
    },
    textLength() {
      const pureText = this.$utils.htmlToText(this.message);
      return pureText && pureText.length || 0;
    },
    disableButton() {
      return this.commenting || !this.message || !this.message.trim() || this.message.trim() === '<p></p>' || this.textLength > this.$activityConstants.COMMENT_MAX_LENGTH;
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
    ckEditorId() {
      return `comment_${this.activityId}`;
    },
  },
  watch: {
    activityId() {
      this.message = null;
    },
    displayCommentEditor() {
      if (this.displayCommentEditor) {
        this.$nextTick(() => {
          this.$refs.commentEditor.initCKEditor();
        });
      }
    },
  },
  created() {
    document.addEventListener('activity-commented', (event) => {
      const activityId = event && event.detail && event.detail.activityId;
      const comment = event && event.detail && event.detail.comment;
      if (activityId === this.activityId) {
        comment.hightlight = true;
        this.comments.push(comment);
        window.setTimeout(() => {
          comment.hightlight = false;
        }, 5000);
      }
    });
  },
  methods: {
    reset() {
      this.commenting = false;
      this.message = null;
    },
    postComment() {
      if (this.commenting) {
        return;
      }
      this.commenting = true;
      this.$activityService.createComment(this.activityId, null, this.message, this.$activityConstants.FULL_COMMENT_EXPAND)
        .then(comment => document.dispatchEvent(new CustomEvent('activity-commented', {detail: {
          activityId: this.activityId,
          comment: comment
        }})))
        .finally(() => {
          this.message = null;
          this.commenting = false;
        });
    },
  },
};
</script>