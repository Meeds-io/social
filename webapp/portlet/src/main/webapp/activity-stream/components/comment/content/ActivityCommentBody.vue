<template>
  <div
    v-if="commentTypeExtension"
    :id="id"
    class="d-inline-flex flex-column width-fit-content overflow-hidden">
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
      <v-list-item :class="highlightClass" class="pe-1 width-fit-content">
        <activity-head-user
          :identity="posterIdentity"
          :size="33"
          avatar
          class="mt-0 mb-auto me-2 flex-grow-0" />
        <div class="flex-grow-1 flex-shrink-1 overflow-hidden">
          <div class="px-2 py-1 flex-grow-1 grey-background border-box-sizing rounded-lg">
            <v-list-item-title class="pt-1 font-weight-bold subtitle-2">
              <activity-head-user :identity="posterIdentity" />
            </v-list-item-title>
            <activity-comment-body-text
              :activity="activity"
              :comment="comment"
              :comment-type-extension="commentTypeExtension"
              @comment-initialized="$emit('comment-initialized')" />
          </div>
          <div class="py-0 my-1 align-start d-flex flex-row border-box-sizing">
            <activity-comment-actions
              :activity="activity"
              :comment="comment"
              :comment-type-extension="commentTypeExtension"
              class="d-flex flex-row py-0 mb-auto flex-shrink-1" />
            <activity-head-time
              :activity="comment"
              class="d-inline ps-2 ms-auto activity-comment-head-time"
              no-icon />
          </div>
        </div>
        <v-list-item-action class="mx-0 mb-auto mt-0 pt-0">
          <activity-comment-menu
            :activity="activity"
            :comment="comment"
            :actions="commentActions"
            :comment-type-extension="commentTypeExtension" />
        </v-list-item-action>
      </v-list-item>
    </template>

    <template v-if="subComments && subComments.length">
      <activity-comment-body
        v-for="subComment in subComments"
        :key="subComment.id"
        :activity="activity"
        :comment="subComment"
        :comment-types="commentTypes"
        :comment-actions="commentActions"
        :comment-editing="commentEditing"
        class="ps-10"
        @comment-initialized="$emit('comment-initialized')" />
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
    activity: {
      type: Object,
      default: null,
    },
    comment: {
      type: Object,
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
      return this.activity && this.activity.id || '';
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
    commentTypeExtension() {
      return this.commentTypes && (this.commentTypes[this.comment.type] || this.commentTypes['default']);
    },
    commentEditOptions() {
      const commentToEdit = this.isEditingComment ? this.commentEditing : this.comment;
      const messageToEdit = commentToEdit.contentToEdit
                            || (this.commentTypeExtension && this.commentTypeExtension.getBodyToEdit && this.commentTypeExtension.getBodyToEdit(commentToEdit))
                            || this.commentToEdit.title
                            || this.commentToEdit.body;
      return {
        activityId: this.activityId,
        parentCommentId: this.comment.parentCommentId || null,
        commentId: this.comment.id,
        message: messageToEdit,
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
            behavior: 'smooth',
            block: 'start',
          });
        }
      }, 10);
    },
  },
};
</script>