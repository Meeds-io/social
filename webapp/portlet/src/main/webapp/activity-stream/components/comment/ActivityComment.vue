<template>
  <div :class="hightlightClass">
    <v-list-item>
      <activity-head-user
        :identity="posterIdentity"
        :size="33"
        avatar
        class="mt-0 mb-auto me-2" />
      <v-list-item-content class="col-auto ps-2 pt-1 pb-0 mb-6 flex-shrink-1 grey-background border-box-sizing rounded-lg">
        <v-list-item-title class="pt-1 font-weight-bold subtitle-2">
          <activity-head-user :identity="posterIdentity" />
        </v-list-item-title>
        <p v-if="useParagraph" v-sanitized-html="comment.body"></p>
        <div v-else v-sanitized-html="comment.body"></div>
      </v-list-item-content>
    </v-list-item>
    <template v-if="subComments && subComments.length">
      <activity-comment
        v-for="subComment in subComments"
        :key="subComment.id"
        :comment="subComment"
        class="ms-10" />
    </template>
    <v-list-item-content v-if="displayReplyEditor" class="col-auto ms-10 pt-1 pb-0 mb-6 flex-shrink-1 border-box-sizing">
      <exo-activity-rich-editor
        ref="replyEditor"
        v-model="message"
        :ck-editor-type="ckEditorId"
        :max-length="$activityConstants.COMMENT_MAX_LENGTH"
        :placeholder="$t('activity.composer.placeholder', {0: $activityConstants.COMMENT_MAX_LENGTH})" />
    </v-list-item-content>
  </div>
</template>

<script>
export default {
  props: {
    comment: {
      type: String,
      default: null,
    },
    displayReplyEditor: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    message: null,
  }),
  computed: {
    commentId() {
      return this.comment && this.comment.id || '';
    },
    ckEditorId() {
      return `reply_${this.commentId}`;
    },
    hightlight() {
      return this.comment && this.comment.hightlight;
    },
    hightlightClass() {
      return this.hightlight && 'light-grey-background';
    },
    subComments() {
      return this.comment && this.comment.subComments;
    },
    posterIdentity() {
      return this.comment && this.comment.identity;
    },
    commentBody() {
      return this.comment && this.comment.body || '';
    },
    useParagraph() {
      return !this.commentBody.includes('</p>');
    },
  },
};
</script>