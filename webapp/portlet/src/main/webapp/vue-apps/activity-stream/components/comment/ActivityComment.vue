<template>
  <div
    v-if="commentTypeExtension"
    :id="id"
    class="d-inline-flex flex-column activity-comment border-box-sizing">
    <div
      v-if="isEditingComment"
      class="col-auto mb-4 py-0 flex-shrink-1">
      <activity-comment-rich-text
        ref="commentEditRichEditor"
        :activity-id="activityId"
        :comment-type-extension="commentTypeExtension"
        :space-id="spaceId"
        :parent-comment-id="comment.parentCommentId"
        :comment-id="comment.id"
        :template-params="templateParams"
        :label="$t('UIActivity.label.Update')"
        :options="commentEditOptions" />
    </div>
    <template v-else>
      <v-list-item :class="highlightClass" class="pa-0 mb-4 width-fit-content">
        <exo-user-avatar
          :identity="posterIdentity"
          :size="33"
          :extra-class="'mt-0 mb-auto me-2 flex-grow-0'" 
          popover
          avatar />
        <div class="flex-grow-1 flex-shrink-1 overflow-hidden">
          <div class="px-2 py-1 flex-grow-1 activity-comment-background border-box-sizing rounded-lg">
            <v-list-item-title class="pt-1 font-weight-bold subtitle-2 d-flex justify-space-between">
              <exo-user-avatar
                :identity="posterIdentity"
                fullname
                popover
                bold-title
                link-style />
              <activity-comment-menu
                :activity="activity"
                :comment="comment"
                :actions="commentActions"
                :comment-type-extension="commentTypeExtension" />
            </v-list-item-title>
            <activity-comment-body-text
              :activity="activity"
              :comment="comment"
              :comment-type-extension="commentTypeExtension"
              @comment-initialized="$emit('comment-initialized')" />
          </div>
          <extension-registry-components
            :params="extendedComponentParams"
            name="CommentContent"
            type="comment-content-extensions"
            parent-element="div"
            element="div"
            class=" d-flex flex-column" />
          <div class="py-0 my-1 align-center d-flex flex-row border-box-sizing">
            <activity-comment-actions
              :activity="activity"
              :comment="comment"
              :comment-type-extension="commentTypeExtension"
              class="d-flex flex-row py-0 mb-auto flex-shrink-1" />
            <activity-comment-time
              :activity="activity"
              :comment="comment"
              class="d-flex align-center ps-2 activity-comment-head-time"
              no-icon />
          </div>
          <div v-if="hasMoreRepliesToDisplay" class="py-0 my-1 align-start d-flex flex-row border-box-sizing">
            <v-btn
              class="primary--text font-weight-bold mb-1 subtitle-2 pa-0"
              small
              link
              text
              @click="openReplies">
              <v-icon size="12" class="me-1 fa-flip-horizontal">fa-reply</v-icon>
              {{ $t('UIActivity.label.ViewAllReplies', {0: subCommentsSize}) }}
            </v-btn>
          </div>
        </div>
      </v-list-item>
    </template>

    <div
      v-if="displayedSubCommentsSize"
      :class="highlightRepliesClass"
      class="flex d-flex flex-column">
      <activity-comment
        v-for="subComment in subCommentsToDisplay"
        :key="subComment.id"
        :activity="activity"
        :comment="subComment"
        :comment-types="commentTypes"
        :comment-actions="commentActions"
        :comment-editing="commentEditing"
        class="ps-10"
        @comment-initialized="$emit('comment-initialized')" />
    </div>
    <div v-if="newReplyEditor" class="ps-10 py-0 mb-2 align-start border-box-sizing">
      <activity-comment-rich-text
        ref="commentRichEditor"
        class="col-auto pa-0 mt-0 mb-2 flex-shrink-1"
        :activity-id="activityId"
        :comment-type-extension="commentTypeExtension"
        :space-id="spaceId"
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
  data: () => ({
    displayedSubCommentCount: 2,
  }),
  computed: {
    activityId() {
      return this.activity && this.activity.id || '';
    },
    commentId() {
      return this.comment && this.comment.id || '';
    },
    parentCommentId() {
      return this.comment && (this.comment.parentCommentId || this.comment.id) || '';
    },
    id() {
      return `ActivityCommment_${this.comment.id}`;
    },
    spaceId() {
      return this.activity && this.activity.activityStream && this.activity.activityStream.space && this.activity.activityStream.space.id;
    },
    ckEditorId() {
      return `reply_${this.comment.id}`;
    },
    highlight() {
      return this.comment && this.comment.highlight;
    },
    highlightClass() {
      return this.highlight && 'light-grey-background' || '';
    },
    highlightReplies() {
      return this.comment && this.comment.highlightReplies;
    },
    highlightRepliesClass() {
      return this.highlightReplies && 'light-grey-background' || '';
    },
    posterIdentity() {
      return this.comment && this.comment.identity && this.comment.identity.profile && this.comment.identity.profile.dataEntity;
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
                            || commentToEdit.title
                            || commentToEdit.body
                            || '';
      return {
        activityId: this.activityId,
        parentCommentId: this.comment.parentCommentId || null,
        commentId: this.commentId,
        message: messageToEdit,
        files: this.comment.files,
      };
    },
    subCommentsToDisplay() {
      if (!this.comment.expandSubComments && this.subComments && this.subComments.length > this.displayedSubCommentCount) {
        return this.subComments && this.subComments.slice(this.subComments.length - this.displayedSubCommentCount, this.subComments.length);
      } else {
        return this.subComments;
      }
    },
    displayedSubCommentsSize() {
      return this.subCommentsToDisplay && this.subCommentsToDisplay.length || 0;
    },
    subCommentsSize() {
      return this.comment && this.comment.subCommentsSize || 0;
    },
    hasMoreRepliesToDisplay() {
      return this.subCommentsSize > this.displayedSubCommentsSize;
    },
    extendedComponentParams() {
      return {
        activity: this.comment,
        activityTypeExtension: this.commentTypeExtension,
      };
    },
  },
  watch: {
    highlight: {
      immediate: true,
      handler: function(val) {
        if (val) {
          window.setTimeout(() => {
            this.comment.highlight = false;
            this.comment.highlightReplies = false;
          }, 5000);
        }
      },
    },
    highlightReplies: {
      immediate: true,
      handler: function(val) {
        if (val) {
          window.setTimeout(() => {
            this.comment.highlightReplies = false;
            this.comment.highlight = false;
          }, 5000);
        }
      },
    },
  },
  mounted() {
    if (this.highlight) {
      this.$nextTick().then(() => this.scrollToComment());
    } else if (this.highlightReplies) {
      this.$nextTick().then(this.scrollToReplies);
    }
  },
  methods: {
    openReplies() {
      if (this.$el.closest('#activityCommentsDrawer')) {
        // Is in drawer
        this.comment.expandSubComments = true;
        this.displayedSubCommentCount = this.subCommentsSize;
      } else {
        document.dispatchEvent(new CustomEvent('activity-comments-display', {detail: {
          activity: this.activity,
          commentId: this.commentId,
          highlightRepliesCommentId: this.commentId,
        }}));
      }
    },
    scrollToReplies() {
      const repliesElement = document.querySelector(`#activityCommentsDrawer .drawerContent #${this.id} .activity-comment`);
      this.scrollTo(repliesElement);
    },
    scrollToComment(tentative) {
      const commentElement = document.querySelector(`#activityCommentsDrawer .drawerContent #${this.id}`);
      if (commentElement) {
        this.scrollTo(commentElement);
      } else if (!tentative || tentative < 5) {
        window.setTimeout(() => {
          this.scrollToComment(tentative && (tentative + 1) || 1);
        }, 200);
      } else {
        // eslint-disable-next-line no-console
        console.warn(`Can't scroll to comment with id ${this.id} after ${tentative} tentatives`);
      }
    },
    scrollTo(element) {
      window.setTimeout(() => {
        if (element && element.scrollIntoView) {
          element.scrollIntoView({
            behavior: 'smooth',
            block: 'start',
          });
        }
      }, 50);
    },
  },
};
</script>