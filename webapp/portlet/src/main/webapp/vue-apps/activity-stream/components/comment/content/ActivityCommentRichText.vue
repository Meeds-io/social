<template>
  <div :id="ckEditorId">
    <v-list-item class="pa-0">
      <v-list-item-avatar :size="avatarSize" class="mt-0 mb-auto me-2">
        <img
          :src="avatarUrl"
          class="object-fit-cover my-auto"
          loading="lazy">
      </v-list-item-avatar>
      <v-list-item-content class="flex px-0 py-0 mb-2 flex-shrink-1 border-box-sizing rich-editor-content">
        <exo-activity-rich-editor
          ref="commentEditor"
          v-model="message"
          :ck-editor-type="ckEditorId"
          :max-length="$activityConstants.COMMENT_MAX_LENGTH"
          :placeholder="$t('activity.composer.placeholder', {0: $activityConstants.COMMENT_MAX_LENGTH})"
          :activity-id="activityId"
          :template-params="templateParams"
          suggestor-type-of-relation="mention_comment"
          autofocus
          @ready="handleEditorReady" />
      </v-list-item-content>
    </v-list-item>
    <v-btn
      :loading="commenting"
      :disabled="disableButton"
      :aria-label="label"
      class="btn btn-primary ms-10"
      color="primary"
      @click="postComment">
      {{ label }}
    </v-btn>
    <v-btn
      v-if="commentUpdate"
      :disabled="commenting"
      class="btn ms-2"
      @click="$root.$emit('activity-comment-edit-cancel')">
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
    templateParams: {
      type: Object,
      default: () => ({}),
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
    avatarSize: '33px',
  }),
  computed: {
    avatarUrl() {
      return this.$currentUserIdentity && this.$currentUserIdentity.profile && this.$currentUserIdentity.profile.avatar;
    },
    textLength() {
      const pureText = this.$utils.htmlToText(this.message);
      return pureText && pureText.length || 0;
    },
    commentUpdate() {
      return !!this.commentId;
    },
    disableButton() {
      return this.commenting || !this.message || !this.message.trim() || this.message.trim() === '<p></p>' || this.message.trim() === '<div></div>' || this.textLength > this.$activityConstants.COMMENT_MAX_LENGTH;
    },
    ckEditorId() {
      return `comment_${this.commentId || ''}_${this.parentCommentId || ''}_${this.activityId}`;
    },
  },
  mounted() {
    this.init();
  },
  beforeDestroy() {
    this.reset();
  },
  methods: {
    reset() {
      if (!this.initialized) {
        return;
      }
      this.initialized = false;

      if (this.message) {
        // Used to preserve last message of user even after deleting drawer
        this.$root.$emit('activity-comment-editor-updated', {
          activityId: this.activityId,
          commentId: this.commentId,
          parentCommentId: this.parentCommentId,
          message: this.message && String(this.message),
          templateParams: this.templateParams,
        });
      }

      this.commenting = false;
      this.message = null;

      if (this.$refs.commentEditor) {
        this.$refs.commentEditor.destroyCKEditor();
      }
    },
    init() {
      if (this.options
          && this.options.activityId === this.activityId
          && ((!this.parentCommentId && !this.parentCommentId === !this.options.parentCommentId)
          || this.options.parentCommentId === this.parentCommentId)
          && ((!this.commentId && !this.commentId === !this.options.commentId)
          || this.options.commentId === this.commentId)) {
        this.message = this.options.message || null;
      } else {
        this.message = null;
      }

      if (this.$refs.commentEditor) {
        this.$refs.commentEditor.initCKEditor();
        this.initialized = true;
        this.scrollToCommentEditor();
      }
    },
    handleEditorReady() {
      if (this.commentUpdate) {
        this.scrollToCommentEditor();
      }
    },
    scrollToCommentEditor() {
      window.setTimeout(() => {
        const commentElementEditor = document.querySelector(`#activityCommentsDrawer .drawerContent #${this.ckEditorId}`);
        if (commentElementEditor && commentElementEditor.scrollIntoView) {
          commentElementEditor.scrollIntoView({
            behavior: 'smooth',
            block: 'start',
          });
        }
      }, 50);
    },
    postComment() {
      if (this.commenting) {
        return;
      }
      this.commenting = true;
      if (this.commentUpdate) {
        this.$activityService.updateComment(this.activityId, this.parentCommentId, this.commentId, this.message, this.templateParams,this.$activityConstants.FULL_COMMENT_EXPAND)
          .then(comment => this.$root.$emit('activity-comment-updated', comment))
          .finally(() => {
            this.message = null;
            this.commenting = false;
          });
      } else {
        this.$activityService.createComment(this.activityId, this.parentCommentId, this.message, this.templateParams, this.$activityConstants.FULL_COMMENT_EXPAND)
          .then(comment => this.$root.$emit('activity-comment-created', comment))
          .finally(() => {
            this.message = null;
            this.commenting = false;
          });
      }
    },
  },
};
</script>
