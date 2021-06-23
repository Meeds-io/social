<template>
  <div
    :id="id">
    <activity-comment-rich-text
      v-if="commentEditing"
      ref="commentRichEditor"
      key="commentRichEditor"
      class="col-auto ps-13 mt-1 mb-2 flex-shrink-1"
      :activity-id="activityId"
      :parent-comment-id="comment.parentCommentId"
      :comment-id="comment.id"
      :label="$t('UIActivity.label.Update')"
      :options="commentEditOptions"
      @cancel="commentEditing = false"
      @updated="commentEditing = false" />
    <template v-else>
      <v-list-item :class="highlightClass">
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
      <div class="ps-12 py-0 my-1 align-start border-box-sizing">
        <activity-comment-actions
          :comment="comment"
          class="d-flex flex-row py-0 mb-auto flex-shrink-1" />
      </div>
    </template>

    <template v-if="subComments && subComments.length">
      <activity-comment
        v-for="subComment in subComments"
        :key="subComment.id"
        :comment="subComment"
        :comment-actions="commentActions"
        :allow-edit="allowEdit"
        class="ms-10" />
    </template>
    <div v-if="!commentEditing" class="ps-12 py-0 mb-2 align-start border-box-sizing">
      <activity-comment-rich-text
        v-if="displayReply"
        ref="commentRichEditor"
        key="commentRichEditor"
        class="col-auto ps-13 mt-1 mb-2 flex-shrink-1"
        :activity-id="activityId"
        :parent-comment-id="parentCommentId"
        :label="$t('UIActivity.label.Comment')"
        :allow-edit="allowEdit"
        :options="editorOptions" />
    </div>
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
    allowEdit: {
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
    commentEditing: false,
  }),
  computed: {
    activityId() {
      return this.comment && this.comment.activityId || '';
    },
    parentCommentId() {
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
    commentEditOptions() {
      return {
        activityId: this.activityId,
        parentCommentId: this.comment.parentCommentId || null,
        commentId: this.comment.id,
        message: this.comment.body,
      };
    },
  },
  created() {
    document.addEventListener('activity-comment-edit', this.editComment);
  },
  mounted() {
    if (this.highlight) {
      this.$nextTick().then(this.scrollToComment);
    }
  },
  beforeDestroy() {
    document.removeEventListener('activity-comment-edit', this.editComment);
  },
  methods: {
    editComment(event) {
      if (!this.allowEdit) {
        return;
      }
      const comment = event && event.detail;
      if (!comment || comment.id !== this.comment.id) {
        return;
      }
      this.commentEditing = true;
      this.$nextTick().then(() => this.$refs.commentRichEditor.init());
    },
    updateCommentDisplay(event) {
      const comment = event && event.detail && event.detail.comment;
      if (!comment || comment.id !== this.comment.id) {
        return;
      }
      
    },
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