<template>
  <exo-drawer
    ref="activityCommentsDrawer"
    :temporary="temporaryDrawer"
    id="activityCommentsDrawer"
    allow-expand
    right
    disable-pull-to-refresh
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
        ref="activityComments"
        :activity="activity"
        :comments="comments"
        :comment-types="commentTypes"
        :comment-actions="commentActions"
        :comment-editing="commentToEdit"
        :new-comment-editor="newCommentEditor"
        :selected-comment-id-to-reply="selectedCommentIdToReply"
        :initialized="initialized"
        class="pb-0 pt-5 px-4"
        allow-edit
        @comment-created="addComment"
        @comment-deleted="deleteComment"
        @comment-updated="updateComment"
        @initialized="scrollToEnd" />
    </template>
    <template v-if="pagesCount > 1 && initialized" slot="footer">
      <div class="text-center">
        <v-pagination
          v-model="page"
          :length="pagesCount"
          circle
          light
          flat
          @input="changePage" />
      </div>
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
    initialized: false,
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
    pageSize: 10,
    page: 1,
    pagesCount: 1,
  }),
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
      this.page = 1;
      this.offset = 0;
      this.initialized = false;
      this.limit = this.pageSize;
      this.hideCommentRichEditor();
      this.$root.selectedCommentId = null;
    },
    addComment(comment) {
      this.$root.selectedCommentId = comment.id;
      this.retrieveComments(!comment.parentCommentId);
    },
    deleteComment() {
      this.retrieveComments();
    },
    updateComment(comment, commentIndex) {
      this.comments.splice(commentIndex, 1, comment);
    },
    scrollToEnd() {
      this.initialized = true;

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
          offset: this.offset,
          limit: this.limit,
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
          if (!this.drawerOpened) {
            this.drawerOpened = true;
            this.scrollOnOpen = !options.editComment && !options.noAuitomaticScroll;
            this.retrieveComments(true, true);
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
    loadPreviousPage() {
      this.page--;
      this.offset = (this.page - 1) * this.pageSize;
      this.comments = [];
      return this.$nextTick().then(() => this.retrieveComments(false, true));
    },
    changePage() {
      this.hideCommentRichEditor();
      this.$root.selectedCommentId = null;
      this.offset = (this.page - 1) * this.pageSize;
      this.comments = [];
      return this.$nextTick().then(() => this.retrieveComments());
    },
    retrieveComments(loadLastComments, searchSelectedComment) {
      if (loadLastComments) {
        this.offset = 0;
      }

      this.$refs.activityCommentsDrawer.startLoading();
      return this.$activityService.getActivityComments(this.activity.id, !!loadLastComments, this.offset, this.limit, this.$activityConstants.FULL_COMMENT_EXPAND)
        .then(data => {
          let comments = data && data.comments || [];
          let selectedComment;
          if (this.$root.selectedCommentId && this.$root.selectedActivityId === this.activity.id) {
            selectedComment = this.highlightComment(comments, this.$root.selectedCommentId);
          } else if (this.highlightCommentId) {
            selectedComment = this.highlightComment(comments, this.highlightCommentId);
          } else if (this.highlightRepliesCommentId) {
            selectedComment = comments.find(comment => comment.id === this.highlightRepliesCommentId);
            if (selectedComment) {
              selectedComment.highlightReplies = true;
              selectedComment.expandSubComments = true;
            }
          }
          this.commentsSize = data && data.size && Number(data.size) || 0;
          this.pagesCount = parseInt((this.commentsSize + this.limit - 1) / this.limit);
          if (loadLastComments) {
            this.page = this.pagesCount;
            this.offset = (this.page - 1) * this.pageSize;
            if (this.commentsSize > this.pageSize) {
              // Display only comments of currentPage
              const fromIndex = this.pageSize - ((this.commentsSize - 1) % this.pageSize + 1);
              const toIndex = comments.length;
              comments = comments.slice(fromIndex, toIndex);
            }
          }
          if (searchSelectedComment) {
            const selectedCommentId = selectedComment && selectedComment.id || this.selectedCommentIdToReply || this.$root.selectedCommentId;
            if (selectedCommentId && !comments.find(comment => comment.id === selectedCommentId)) {
              if (this.page > 1) {
                // Retrieve previous page to display selected comment
                return this.loadPreviousPage();
              }
            }
          }
          this.comments = comments;
        })
        .finally(() => {
          if (!this.comments.length) {
            this.initialized = true;
          }
          this.$refs.activityCommentsDrawer.endLoading();
        });
    },
    highlightComment(comments, highlightCommentId) {
      const selectedComment = comments.find(comment => comment.id === highlightCommentId);
      if (selectedComment) {
        selectedComment.highlight = true;
      } else {
        comments.forEach(comment => {
          const selectedReply = comment.subComments && comment.subComments.find(subComment => subComment.id === highlightCommentId);
          if (selectedReply) {
            selectedReply.highlight = true;
          }
        });
      }
      return selectedComment;
    },
  },
};
</script>