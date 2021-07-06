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
      v-if="commentsSize > 2"
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
    limit: 2,
    loading: false,
  }),
  computed: {
    commentsPreviewList() {
      const commentsPreviewList = [];
      if (this.comments && this.commentsSize > 0) {
        const commentsPerId = {};
        this.comments.forEach(comment => {
          if (comment.parentCommentId) {
            let parentComment = commentsPerId[comment.parentCommentId];
            if (parentComment) {
              parentComment.subCommentsSize++;
              if (parentComment.lastSubComment && new Date(comment.createDate) > new Date(parentComment.lastSubComment.createDate)) {
                parentComment.lastSubComment = comment;
              }
            } else {
              parentComment = commentsPreviewList.find(tmpComment => tmpComment.id === comment.parentCommentId);
              if (!parentComment) {
                return;
              }
              commentsPerId[comment.parentCommentId] = parentComment;
              parentComment.subCommentsSize = 1;
              parentComment.lastSubComment = comment;
            }
          } else {
            commentsPerId[comment.parentCommentId] = comment;
            if (!comment.subCommentsSize) {
              comment.subCommentsSize = 0;
            }
            commentsPreviewList.push(comment);
          }
        });
        if (commentsPreviewList.length) {
          commentsPreviewList.forEach(comment => {
            if (comment.lastSubComment) {
              commentsPreviewList.push(comment.lastSubComment);
            }
          });
          commentsPreviewList.sort((comment1, comment2) => new Date(comment1.createDate).getTime() - new Date(comment2.createDate));
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
            this.activity.commentsSize = this.commentsSize = data && data.size && Number(data.size) || 0;
            this.$root.$emit('activity-comments-retrieved', this.activity, this.comments);
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