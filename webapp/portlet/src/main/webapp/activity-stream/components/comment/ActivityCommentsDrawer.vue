<template>
  <exo-drawer
    ref="activityCommentsDrawer"
    id="activityCommentsDrawer"
    body-classes="hide-scroll decrease-z-index-more"
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
        ref="activityComments"
        :activity-id="activityId"
        :comments="comments"
        :comment-editor-display="displayCommentEditor"
        :selected-comment-id-to-reply="selectedCommentIdToReply"
        @editor-loaded="scrollToEnd" />
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data: () => ({
    commentsData: null,
    activityId: null,
    loading: false,
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
          this.$nextTick(this.scrollToEnd);
        }
      }
    },
  },
  created() {
    document.addEventListener('activity-comments-display', event => {
      const options = event && event.detail;
      if (options) {
        if (options.activityId !== this.activityId) {
          this.reset();
        }
        if (options.activityId) {
          this.activityId = options.activityId;
        }
        if (options.offset) {
          this.offset = options.offset;
        }
        if (options.limit) {
          this.limit = options.limit;
        }
        this.$refs.activityCommentsDrawer.open();
        this.retrieveComments();
        if (options.displayComment) {
          this.displayComment(options.commentIdToReply);
        }
      }
    });

    this.$root.$on('activity-commented', () => {
      this.displayCommentEditor = false;
      this.$nextTick(this.scrollToEnd);
    });
  },
  methods: {
    reset() {
      this.comments = [];
      this.hideComment();
    },
    scrollToEnd() {
      const drawerContentElement = document.querySelector('#activityCommentsDrawer .drawerContent');
      drawerContentElement.scrollTo(0, drawerContentElement.scrollHeight);
    },
    displayComment(commentIdToReply) {
      this.selectedCommentIdToReply = commentIdToReply || null;
      // Has to make this change at the end of the method,
      // to have the correct value of this.selectedCommentIdToReply
      this.displayCommentEditor = true;
    },
    hideComment() {
      this.displayCommentEditor = false;
      this.selectedCommentIdToReply = null;
    },
    retrieveComments() {
      this.loading = true;
      this.$activityService.getActivityComments(this.activityId, false, this.offset, this.limit, this.$activityConstants.FULL_COMMENT_EXPAND)
        .then(data => this.commentsData = data)
        .finally(() => this.loading = false);
    },
  },
};
</script>