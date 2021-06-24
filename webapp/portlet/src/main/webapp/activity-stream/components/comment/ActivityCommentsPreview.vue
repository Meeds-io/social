<template>
  <v-progress-linear
    v-if="loading"
    color="primary"
    height="2"
    indeterminate />
  <div v-else-if="commentsSize">
    <activity-comments
      :activity-id="activityId"
      :comments="commentsPreviewList"
      :comment-types="commentTypes"
      :comment-actions="commentActions"
      @comment-created="retrieveLastComment"
      @comment-deleted="retrieveLastComment"
      @comment-updated="retrieveLastComment" />
    <v-btn
      class="primary--text font-weight-bold mb-1 subtitle-2"
      small
      link
      text
      @click="openCommentsDrawer">
      {{ $t('UIActivity.label.Show_All_Comments', {0: commentsSize}) }}
    </v-btn>
  </div>
</template>

<script>
export default {
  props: {
    activityId: {
      type: String,
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
  },
  data: () => ({
    commentsData: null,
    limit: 1,
    loading: false,
  }),
  computed: {
    comments() {
      return this.commentsData && this.commentsData.comments;
    },
    commentsSize() {
      return this.commentsData && this.commentsData.size;
    },
    lastComment() {
      return this.comments && this.comments.length && this.comments[0];
    },
    lastSubComment() {
      return this.comments && this.comments.length > 1 && this.comments[this.comments.length - 1];
    },
    commentsPreviewList() {
      const commentsPreviewList = [];
      if (this.lastComment) {
        commentsPreviewList.push(this.lastComment);
      }
      if (this.lastSubComment) {
        commentsPreviewList.push(this.lastSubComment);
      }
      return commentsPreviewList;
    },
  },
  created() {
    this.retrieveLastComment();
  },
  methods: {
    retrieveLastComment() {
      this.loading = true;
      this.$activityService.getActivityComments(this.activityId, true, 0, this.limit, this.$activityConstants.FULL_COMMENT_EXPAND)
        .then(data => {
          this.commentsData = data;
          return this.$nextTick();
        })
        .finally(() => this.loading = false);
    },
    openCommentsDrawer() {
      document.dispatchEvent(new CustomEvent('activity-comments-display', {detail: {
        activityId: this.activityId,
        offset: 0,
        limit: this.commentsSize * 2, // To display all
      }}));
    },
  },
};
</script>