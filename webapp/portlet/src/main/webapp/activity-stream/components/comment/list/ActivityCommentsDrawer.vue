<template>
  <exo-drawer
    ref="activityCommentsDrawer"
    :temporary="temporaryDrawer"
    id="activityCommentsDrawer"
    allow-expand
    right
    @closed="reset">
    <template slot="title">
      <span class="text-capitalize-first-letter">
        {{ $t('activity.comments') }}
      </span>
    </template>
    <template slot="titleIcons">
      <v-btn
        class="my-auto"
        elevation="0"
        icon
        plain
        @click="displayCommentRichEditor()">
        <v-icon color="primary">mdi-chat-plus</v-icon>
      </v-btn>
    </template>
    <template slot="content">
      <activity-comments
        v-show="!loading"
        ref="activityComments"
        :activity="activity"
        :comments="comments"
        :comment-types="commentTypes"
        :comment-actions="commentActions"
        :comment-editing="commentToEdit"
        :new-comment-editor="newCommentEditor"
        :selected-comment-id-to-reply="selectedCommentIdToReply"
        class="pb-0 pt-5 px-4"
        allow-edit
        @comment-created="addComment"
        @comment-deleted="deleteComment"
        @comment-updated="updateComment"
        @initialized="scrollToEnd" />
    </template>
  </exo-drawer>
</template>

<script>
export default {
  props: {
    commentTypes: {
      type: Object,
      default: null,
    },
    commentActions: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    comments: [],
    commentsSize: 0,
    activity: null,
    loading: false,
    drawerOpened: false,
    temporaryDrawer: true,
    newCommentEditor: false,
    selectedCommentIdToReply: null,
    highlightCommentId: null,
    highlightRepliesCommentId: null,
    scrollOnOpen: true,
    commentToEdit: null,
    offset: 0,
    limit: 10,
  }),
  watch: {
    loading() {
      if (this.loading) {
        this.$refs.activityCommentsDrawer.startLoading();
      } else {
        this.$refs.activityCommentsDrawer.endLoading();
      }
    },
  },
  created() {
    document.addEventListener('activity-comments-display', this.displayActivityComments);
    document.addEventListener('activity-comment-edit', this.editActivityComments);

    this.$root.$on('activity-comment-created', this.hideCommentRichEditor);
    this.$root.$on('activity-comment-updated', this.hideCommentRichEditor);
    this.$root.$on('activity-comment-edit-cancel', this.hideCommentRichEditor);

    // Avoid closing drawer when closing dialog
    this.$root.$on('activity-stream-confirm-opened', () => this.temporaryDrawer = false);
    this.$root.$on('activity-stream-confirm-closed', () => this.temporaryDrawer = true);
  },
  methods: {
    reset() {
      this.comments = [];
      this.commentsSize = 0;
      this.drawerOpened = false;
      this.hideCommentRichEditor();
      this.$root.selectedCommentId = null;
    },
    addComment(comment) {
      if (this.comments) {
        this.comments.push(comment);
        this.commentsSize++;
      } else {
        this.comments = [comment];
        this.commentsSize = 1;
      }
    },
    deleteComment(comment, commentIndex) {
      this.comments.splice(commentIndex, 1);
      this.commentsSize--;
    },
    updateComment(comment, commentIndex) {
      this.comments.splice(commentIndex, 1, comment);
    },
    scrollToEnd() {
      if (this.scrollOnOpen
          && (!this.newCommentEditor || !this.selectedCommentIdToReply)
          && !this.highlightCommentId
          && !this.highlightRepliesCommentId) {
        // Avoid scrolling again when loading
        this.scrollOnOpen = false;
        this.$root.commentsDrawerInitializing = false;

        window.setTimeout(() => {
          const drawerContentElement = document.querySelector('#activityCommentsDrawer .drawerContent');
          drawerContentElement.scrollTo({
            top: drawerContentElement.scrollHeight,
            left: 0,
            behavior: 'smooth',
            block: 'start',
          });
        }, 100);
      }
    },
    editActivityComments(event) {
      const comment = event && event.detail && event.detail.comment;
      const activity = event && event.detail && event.detail.activity;

      if (comment && activity) {
        const activityBody = event && event.detail && event.detail.activityBody;
        comment.contentToEdit = activityBody;

        this.displayActivityComments({options: {
          activity: activity,
          editComment: comment,
          offset: 0,
          limit: 200,
        }});
      }
    },
    displayActivityComments(event) {
      const options = event && (event.detail || event.options);
      // Activity object with its identifier is mandatory
      if (options && options.activity && options.activity.id) {
        this.hideCommentRichEditor();
        this.$nextTick().then(() => {
          this.activity = options.activity;
          this.offset = options.offset || 0;
          this.limit = options.limit || 10;
          if (!this.drawerOpened) {
            this.drawerOpened = true;
            this.scrollOnOpen = !options.editComment && !options.noAuitomaticScroll;
            this.retrieveComments();
            this.$nextTick().then(() => {
              if (this.$refs.activityCommentsDrawer) {
                this.$refs.activityCommentsDrawer.open();
              }
            });
          }
          this.commentToEdit = options.editComment;
          this.newCommentEditor = !this.commentToEdit && options.newComment;
          this.selectedCommentIdToReply = !this.commentToEdit && options.commentId;
          this.highlightCommentId = options.highlightCommentId;
          this.highlightRepliesCommentId = options.highlightRepliesCommentId;
        });
      }
    },
    displayCommentRichEditor(commentId) {
      this.hideCommentRichEditor();
      this.$nextTick().then(() => {
        this.selectedCommentIdToReply = commentId;
        this.newCommentEditor = true;
      });
    },
    hideCommentRichEditor() {
      this.newCommentEditor = false;
      this.selectedCommentIdToReply = null;
      this.commentToEdit = null;
    },
    retrieveComments() {
      this.loading = true;
      this.$activityService.getActivityComments(this.activity.id, false, this.offset, this.limit, this.$activityConstants.FULL_COMMENT_EXPAND)
        .then(data => {
          const comments = data && data.comments || [];
          if (this.$root.selectedCommentId && this.$root.selectedActivityId === this.activity.id) {
            const selectedComment = comments.find(comment => comment.id === this.$root.selectedCommentId);
            if (selectedComment) {
              selectedComment.highlight = true;
            }
          } else if (this.highlightCommentId) {
            const selectedComment = comments.find(comment => comment.id === this.highlightCommentId);
            if (selectedComment) {
              selectedComment.highlight = true;
            }
          } else if (this.highlightRepliesCommentId) {
            const selectedComment = comments.find(comment => comment.id === this.highlightRepliesCommentId);
            if (selectedComment) {
              selectedComment.highlightReplies = true;
            }
          }
          this.comments = comments;
          this.commentsSize = data && data.size && Number(data.size) || 0;
        })
        .finally(() => {
          window.setTimeout(() => {
            this.loading = false;
          }, this.commentsSize * 10 + 50);
        });
    },
  },
};
</script>