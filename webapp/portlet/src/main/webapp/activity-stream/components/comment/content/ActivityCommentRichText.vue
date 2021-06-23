<template>
  <div :id="ckEditorId">
    <v-list-item-content class="col-auto px-0 pt-1 pb-0 mb-2 flex-shrink-1 border-box-sizing rich-editor-content">
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
      {{ label }}
    </v-btn>
    <v-btn
      v-if="commentUpdate"
      :disabled="commenting"
      class="btn ms-2"
      @click="$emit('cancel')">
      {{ $t('UIActivity.label.Cancel') }}
    </v-btn>
  </div>
</template>

<script>
export default {
  props: {
    activityId: {
      type: String,
      default: null,
    },
    parentCommentId: {
      type: String,
      default: null,
    },
    commentId: {
      type: String,
      default: null,
    },
    label: {
      type: String,
      default: null,
    },
    options: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    initialized: false,
    commenting: false,
    message: null,
  }),
  computed: {
    textLength() {
      const pureText = this.$utils.htmlToText(this.message);
      return pureText && pureText.length || 0;
    },
    commentUpdate() {
      return !!this.commentId;
    },
    disableButton() {
      return this.commenting || !this.message || !this.message.trim() || this.message.trim() === '<p></p>' || this.textLength > this.$activityConstants.COMMENT_MAX_LENGTH;
    },
    ckEditorId() {
      return `comment_${this.parentCommentId || ''}_${this.activityId}`;
    },
  },
  created() {
    document.addEventListener('activity-comment-editor-init', this.init);
    document.addEventListener('activity-comment-editor-destroy', this.reset);

    this.init();
  },
  methods: {
    reset() {
      if (!this.initialized) {
        return;
      }
      this.initialized = false;

      // Used to preserve last message of user even after deleting drawer
      document.dispatchEvent(new CustomEvent('activity-comment-editor-updated', {detail: {
        activityId: this.activityId,
        parentCommentId: this.parentCommentId,
        message: this.message && String(this.message),
      }}));

      this.commenting = false;
      this.message = null;

      if (this.$refs.commentEditor) {
        this.$refs.commentEditor.destroyCKEditor();
      }
      document.removeEventListener('activity-comment-editor-init', this.init);
      document.removeEventListener('activity-comment-editor-destroy', this.reset);
      this.$destroy();
    },
    init() {
      if (this.options
          && this.options.activityId === this.activityId
          && ((!this.parentCommentId && !this.parentCommentId === !this.options.parentCommentId)
          || this.options.parentCommentId === this.parentCommentId)
          && ((!this.commentId && !this.commentId === !this.options.commentId)
          || this.options.commentId === this.commentId)) {
        this.message = this.options.message || null;
      }

      this.$nextTick(() => {
        if (this.$refs.commentEditor) {
          this.$refs.commentEditor.initCKEditor();
        }
      });
      this.initialized = true;
      this.scrollToCommentEditor();
    },
    scrollToCommentEditor() {
      window.setTimeout(() => {
        const commentElementEditor = document.querySelector(`#activityCommentsDrawer .drawerContent #${this.ckEditorId}`);
        if (commentElementEditor && commentElementEditor.scrollIntoView) {
          commentElementEditor.scrollIntoView({
            behavior: 'smooth'
          });
        }
      }, 10);
    },
    postComment() {
      if (this.commenting) {
        return;
      }
      this.commenting = true;
      if (this.commentUpdate) {
        this.$activityService.updateComment(this.activityId, this.parentCommentId, this.commentId, this.message, this.$activityConstants.FULL_COMMENT_EXPAND)
          .then(comment => {
            document.dispatchEvent(new CustomEvent('activity-comment-updated', {detail: {
              activityId: this.activityId,
              comment: comment
            }}));
            this.$emit('updated', comment);
          })
          .finally(() => {
            this.message = null;
            this.commenting = false;
          });
      } else {
        this.$activityService.createComment(this.activityId, this.parentCommentId, this.message, this.$activityConstants.FULL_COMMENT_EXPAND)
          .then(comment => {
            document.dispatchEvent(new CustomEvent('activity-comment-created', {detail: {
              activityId: this.activityId,
              comment: comment
            }}));
            this.$emit('created', comment);
          })
          .finally(() => {
            this.message = null;
            this.commenting = false;
          });
      }
    },
  },
};
</script>