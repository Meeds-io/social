<template>
  <div :id="id" :class="highlightClass">
    <v-list-item>
      <activity-head-user
        :identity="posterIdentity"
        :size="33"
        avatar
        class="mt-0 mb-auto me-2" />
      <v-list-item-content class="col-auto ps-2 pt-1 pb-0 flex-shrink-1 grey-background border-box-sizing rounded-lg">
        <v-list-item-title class="pt-1 font-weight-bold subtitle-2">
          <activity-head-user :identity="posterIdentity" />
        </v-list-item-title>
        <p
          v-if="useParagraph"
          v-sanitized-html="comment.body"
          class="rich-editor-content"></p>
        <div
          v-else
          v-sanitized-html="comment.body"
          class="rich-editor-content"></div>
      </v-list-item-content>
      <v-list-item-action class="mx-0 mb-auto mt-0 pt-0">
        <activity-comment-menu :comment="comment" :actions="commentActions" />
      </v-list-item-action>
    </v-list-item>
    <div class="ps-12 py-0 mb-2 align-start border-box-sizing" dense>
      <activity-comment-actions
        :comment="comment"
        class="d-flex flex-row py-0 mb-auto flex-shrink-1" />
      <activity-comment-rich-text
        v-if="displayReply"
        ref="commentRichEditor"
        key="commentRichEditor"
        class="col-auto ps-13 mt-1 mb-2 flex-shrink-1"
        :activity-id="activityId"
        :comment-id="commentId"
        :label="$t('UIActivity.label.Reply')"
        :options="editorOptions" />
    </div>

    <template v-if="subComments && subComments.length">
      <activity-comment
        v-for="subComment in subComments"
        :key="subComment.id"
        :comment="subComment"
        :comment-actions="commentActions"
        class="ms-10" />
    </template>
  </div>
</template>

<script>
export default {
  props: {
    comment: {
      type: String,
      default: null,
    },
    subComments: {
      type: Array,
      default: null,
    },
    commentActions: {
      type: Object,
      default: null,
    },
    displayReply: {
      type: Boolean,
      default: false,
    },
    editor: {
      type: Boolean,
      default: false,
    },
    editorOptions: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    message: null,
  }),
  computed: {
    activityId() {
      return this.comment && this.comment.activityId || '';
    },
    commentId() {
      return this.comment && (this.comment.parentCommentId || this.comment.id) || '';
    },
    id() {
      return `ActivityCommment_${this.comment.id}`;
    },
    ckEditorId() {
      return `reply_${this.comment.id}`;
    },
    highlight() {
      return this.comment && this.comment.highlight;
    },
    highlightClass() {
      return this.highlight && 'light-grey-background';
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
  mounted() {
    if (this.highlight) {
      this.$nextTick().then(this.scrollToComment);
    }
  },
  methods: {
    scrollToComment() {
      window.setTimeout(() => {
        const commentElement = document.querySelector(`#activityCommentsDrawer .drawerContent #${this.id}`);
        if (commentElement && commentElement.scrollIntoView) {
          commentElement.scrollIntoView({
            behavior: 'smooth'
          });
        }
      }, 10);
    },
  },
};
</script>