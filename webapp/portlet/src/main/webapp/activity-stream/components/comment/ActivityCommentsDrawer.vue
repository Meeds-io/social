<template>
  <exo-drawer
    ref="activityCommentsDrawer"
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
        @click="displayComment()">
        <v-icon color="primary">mdi-chat-plus</v-icon>
      </v-btn>
    </template>
    <template slot="content">
      <activity-comments
        v-show="!loading"
        ref="activityComments"
        :activity-id="activityId"
        :comments="comments"
        :comment-editor-display="displayCommentEditor"
        :selected-comment-id-to-reply="selectedCommentIdToReply"
        editor />
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data: () => ({
    commentsData: null,
    activityId: null,
    loading: false,
    drawerOpened: false,
    displayCommentEditor: false,
    selectedCommentIdToReply: null,
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
        if (!this.displayCommentEditor || !this.selectedCommentIdToReply) {
          this.$nextTick().then(() => this.scrollToEnd());
        }
      }
    },
  },
  created() {
    document.addEventListener('activity-comments-display', event => {
      const options = event && event.detail;
      if (options) {
        this.activityId = options.activityId;
        this.offset = options.offset || 0;
        this.limit = options.limit || 10;
        if (!this.drawerOpened) {
          this.drawerOpened = true;
          this.retrieveComments();
          this.$refs.activityCommentsDrawer.open();
        }
        if (options.displayComment) {
          this.displayComment(options.commentId);
        }
      }
    });

    document.addEventListener('activity-commented', this.hideComment);
  },
  methods: {
    reset() {
      this.comments = [];
      this.drawerOpened = false;
      this.hideComment();
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
    displayComment(commentId) {
      this.selectedCommentIdToReply = commentId || null;
      // Has to make this change at the end of the method,
      // to have the correct value of this.selectedCommentIdToReply
      this.displayCommentEditor = true;

      this.$nextTick().then(() => {
        document.dispatchEvent(new CustomEvent('activity-comment-editor-init', {detail: this.lastEditorOptions}));
      });
    },
    hideComment() {
      document.dispatchEvent(new CustomEvent('activity-comment-editor-destroy'));
      this.displayCommentEditor = false;
      this.selectedCommentIdToReply = null;
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