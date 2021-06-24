<template>
  <exo-drawer
    ref="activityCommentsDrawer"
    :temporary="temporaryDrawer"
    id="activityCommentsDrawer"
    body-classes="hide-scroll decrease-z-index"
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
        :activity-id="activityId"
        :comments="comments"
        :comment-types="commentTypes"
        :comment-actions="commentActions"
        :comment-editing="commentToEdit"
        :new-comment-editor="newCommentEditor"
        :selected-comment-id-to-reply="selectedCommentIdToReply"
        allow-edit />
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
    commentsData: null,
    activityId: null,
    loading: false,
    drawerOpened: false,
    temporaryDrawer: true,
    newCommentEditor: false,
    selectedCommentIdToReply: null,
    scrollOnOpen: true,
    commentToEdit: null,
    offset: 0,
    limit: 10,
  }),
  computed: {
    comments() {
      return this.commentsData && this.commentsData.comments;
    },
    commentsSize() {
      return this.commentsData && this.commentsData.size;
    },
  },
  watch: {
    loading() {
      if (this.loading) {
        this.$refs.activityCommentsDrawer.startLoading();
      } else {
        this.$refs.activityCommentsDrawer.endLoading();
        if (this.scrollOnOpen && (!this.newCommentEditor || !this.selectedCommentIdToReply)) {
          this.$nextTick().then(() => this.scrollToEnd());
          // Avoid scrolling again when loading
          this.scrollOnOpen = false;
        }
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
      this.drawerOpened = false;
      this.hideCommentRichEditor();
    },
    scrollToEnd() {
      window.setTimeout(() => {
        const drawerContentElement = document.querySelector('#activityCommentsDrawer .drawerContent');
        drawerContentElement.scrollTo({
          top: drawerContentElement.scrollHeight,
          left: 0,
          behavior: 'smooth'
        });
      }, 10);
    },
    editActivityComments(event) {
      const comment = event && event.detail;
      if (comment && comment.activityId) {
        this.displayActivityComments({options: {
          activityId: comment.activityId,
          editComment: comment,
          offset: 0,
          limit: 200,
        }});
      }
    },
    displayActivityComments(event) {
      const options = event && (event.detail || event.options);
      if (options) {
        this.hideCommentRichEditor();
        this.$nextTick().then(() => {
          this.activityId = options.activityId;
          this.offset = options.offset || 0;
          this.limit = options.limit || 10;
          if (!this.drawerOpened) {
            this.drawerOpened = true;
            this.scrollOnOpen = !options.editComment;
            this.retrieveComments();
            this.$refs.activityCommentsDrawer.open();
          }
          this.commentToEdit = options.editComment;
          this.newCommentEditor = !this.commentToEdit && options.newComment;
          this.selectedCommentIdToReply = !this.commentToEdit && options.commentId;
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
      window.setTimeout(() => {
        this.$activityService.getActivityComments(this.activityId, false, this.offset, this.limit, this.$activityConstants.FULL_COMMENT_EXPAND)
          .then(data => {
            this.commentsData = data;
            return this.$nextTick();
          })
          .finally(() => this.loading = false);
      }, 50);
    },
  },
};
</script>