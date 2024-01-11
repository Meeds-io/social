<template>
  <div :id="ckEditorId">
    <v-list-item class="pa-0">
      <v-list-item-avatar :size="avatarSize" class="mt-0 mb-auto me-2">
        <img
          :src="avatarUrl"
          class="object-fit-cover my-auto"
          loading="lazy"
          role="presentation">
      </v-list-item-avatar>
      <v-list-item-content class="flex px-0 py-0 mb-2 flex-shrink-1 border-box-sizing rich-editor-content">
        <rich-editor
          ref="commentEditor"
          v-model="message"
          :ck-editor-id="ckEditorId"
          :ck-editor-type="ckEditorType"
          :max-length="$activityConstants.COMMENT_MAX_LENGTH"
          :placeholder="$t('activity.composer.placeholder')"
          :activity-id="activityId"
          :template-params="templateParams"
          :object-id="metadataObjectId"
          :object-type="metadataObjectType"
          :suggester-space-id="spaceId"
          context-name="activityComment"
          suggestor-type-of-relation="mention_comment"
          use-extra-plugins
          autofocus
          @ready="handleEditorReady"
          @attachments-edited="attachmentsEdit" />
        <extension-registry-components
          :params="extensionParams"
          name="ActivityComposerAction"
          type="activity-composer-action" />
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
    spaceId: {
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
    commentTypeExtension: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    initialized: false,
    commenting: false,
    activityCommentAttachmentsEdited: false,
    message: null,
    avatarSize: '33px',
    files: [],
    attachments: null,
    comment: null
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
      return this.commenting || ((!this.message || !this.message.trim() || this.message.trim() === '<p></p>' || this.message.trim() === '<div></div>') && !this.activityCommentAttachmentsEdited) || this.textLength > this.$activityConstants.COMMENT_MAX_LENGTH;
    },
    ckEditorType() {
      return this.commentTypeExtension?.ckEditorType
        || (this.metadataObjectType === 'activity' && 'activityComment')
        || `activityComment_${this.metadataObjectType}`;
    },
    ckEditorId() {
      return `comment_${this.commentId || ''}_${this.parentCommentId || ''}_${this.activityId}`;
    },
    ckEditorInstance() {
      return this.$refs.commentEditor || null;
    },
    entityId() {
      return this.commentId && this.commentId.replace('comment','');
    },
    extensionParams() {
      return {
        activityId: this.entityId,
        spaceId: this.spaceId,
        parentCommentId: this.parentCommentId,
        files: this.files,
        templateParams: this.templateParams,
        message: this.message,
        maxMessageLength: this.$activityConstants.COMMENT_MAX_LENGTH,
      };
    },
    metadataObjectId() {
      return this.templateParams?.metadataObjectId || this.commentId;
    },
    metadataObjectType() {
      return this.templateParams?.metadataObjectType || 'activity';
    },
  },
  created() {
    document.addEventListener('activity-composer-edited', this.activityComposerEdit);
  },
  mounted() {
    this.init();
  },
  beforeDestroy() {
    document.removeEventListener('activity-composer-edited', this.activityComposerEdit);
    this.reset();
  },
  methods: {
    activityComposerEdit(event) {
      this.files = event?.detail;
    },
    attachmentsEdit(attachments) {
      this.attachments = attachments;
      this.activityCommentAttachmentsEdited = true;
    },
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
        this.files = this.options.files || null;
      } else {
        this.message = null;
      }

      if (this.$refs.commentEditor) {
        this.$refs.commentEditor.initCKEditor(false, this.message);
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
        this.$activityService.updateComment(this.activityId, this.parentCommentId, this.commentId, this.message, this.files, this.templateParams, this.$activityConstants.FULL_COMMENT_EXPAND)
          .then(comment => this.comment = comment)
          .then(() => this.ckEditorInstance && this.ckEditorInstance.saveAttachments())
          .then(() => this.$root.$emit('activity-comment-updated', this.comment))
          .finally(() => {
            this.message = null;
            this.commenting = false;
          });
      } else {
        this.$activityService.createComment(this.activityId, this.parentCommentId, this.message,  this.files, this.templateParams, this.$activityConstants.FULL_COMMENT_EXPAND)
          .then((comment) => {
            this.comment = comment;
            this.commentId = comment.id;
          })
          .then(() => this.ckEditorInstance && this.ckEditorInstance.saveAttachments())
          .then(() => this.$root.$emit('activity-comment-created', this.comment))
          .finally(() => {
            this.message = null;
            this.commenting = false;
          });
      }
    },
  },
};
</script>
