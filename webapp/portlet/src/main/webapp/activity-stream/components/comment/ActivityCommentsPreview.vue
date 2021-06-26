<template>
  <div>
    <v-progress-linear
      v-if="loading"
      color="primary"
      height="2"
      indeterminate />
    <activity-comments
      :activity="activity"
      :comments="commentsPreviewList"
      :comment-types="commentTypes"
      :comment-actions="commentActions"
      :class="parentCommentClass"
      @comment-created="retrieveLastComment"
      @comment-deleted="retrieveLastComment"
      @comment-updated="retrieveLastComment" />
    <v-btn
      v-if="commentsSize"
      :disabled="loading"
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
    activity: {
      type: Object,
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
    comments: [],
    commentsSize: 0,
    limit: 1,
    loading: false,
  }),
  computed: {
    lastComment() {
      return this.commentsSize && this.comments[0];
    },
    lastSubComment() {
      return this.commentsSize > 1 && this.comments[this.comments.length - 1];
    },
    commentsPreviewList() {
      const commentsPreviewList = [];
      if (this.commentsSize > 0) {
        if (this.lastComment) {
          commentsPreviewList.push(this.lastComment);
        }
        if (this.lastSubComment) {
          commentsPreviewList.push(this.lastSubComment);
        }
      }
      return commentsPreviewList;
    },
    parentCommentClass() {
      return this.commentsSize && 'pb-0 pt-5' || 'pa-0';
    },
  },
  created() {
    this.retrieveLastComment();
  },
  methods: {
    retrieveLastComment() {
      this.loading = true;
      this.$activityService.getActivityComments(this.activity.id, true, 0, this.limit, this.$activityConstants.FULL_COMMENT_EXPAND)
        .then(data => {
          this.comments = [];
          this.commentsSize = 0;
          this.$nextTick().then(() => {
            this.comments = data && data.comments || [];
            this.commentsSize = data && data.size && Number(data.size) || 0;
          });
        })
        .finally(() => this.loading = false);
    },
    openCommentsDrawer() {
      document.dispatchEvent(new CustomEvent('activity-comments-display', {detail: {
        activity: this.activity,
        offset: 0,
        limit: this.commentsSize * 2, // To display all
      }}));
    },
  },
};
</script>