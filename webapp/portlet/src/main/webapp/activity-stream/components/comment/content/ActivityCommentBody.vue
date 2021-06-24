<template>
  <div :id="id">
    <div
      v-if="isEditingComment"
      class="col-auto ps-13 mt-1 mb-2 flex-shrink-1">
      <activity-comment-rich-text
        ref="commentEditRichEditor"
        :activity-id="activityId"
        :parent-comment-id="comment.parentCommentId"
        :comment-id="comment.id"
        :label="$t('UIActivity.label.Update')"
        :options="commentEditOptions" />
    </div>
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
          <activity-comment-body-text
            :comment-types="commentTypes"
            :comment="comment" />
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
      <activity-comment-body
        v-for="subComment in subComments"
        :key="subComment.id"
        :comment="subComment"
        :comment-types="commentTypes"
        :comment-actions="commentActions"
        :comment-editing="commentEditing"
        class="ms-10" />
    </template>
    <div v-if="newReplyEditor" class="ps-12 py-0 mb-2 align-start border-box-sizing">
      <activity-comment-rich-text
        ref="commentRichEditor"
        class="col-auto ps-13 mt-1 mb-2 flex-shrink-1"
        :activity-id="activityId"
        :parent-comment-id="parentCommentId"
        :label="$t('UIActivity.label.Comment')"
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
    commentTypes: {
      type: Object,
      default: null,
    },
    commentActions: {
      type: Object,
      default: null,
    },
    newReplyEditor: {
      type: Boolean,
      default: false,
    },
    commentEditing: {
      type: Boolean,
      default: false,
    },
    editorOptions: {
      type: Object,
      default: null,
    },
  },
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
    isEditingComment() {
      return this.commentEditing && this.commentEditing.id === this.comment.id;
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