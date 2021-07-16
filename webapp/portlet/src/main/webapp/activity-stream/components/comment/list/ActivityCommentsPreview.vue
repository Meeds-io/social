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
      class="primary--text font-weight-bold mb-1 subtitle-2 px-0"
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
              if (!parentComment.subComments) {
                parentComment.subComments = [];
              }
              parentComment.subComments.push(comment);
            } else {
              parentComment = commentsPreviewList.find(tmpComment => tmpComment.id === comment.parentCommentId);
              if (!parentComment) {
                return;
              }
              commentsPerId[comment.parentCommentId] = parentComment;
              parentComment.subCommentsSize = 1;
              parentComment.subComments = [comment];
            }
          } else {
            commentsPerId[comment.parentCommentId] = comment;
            if (!comment.subCommentsSize) {
              comment.subCommentsSize = 0;
              comment.subComments = [];
            }
            commentsPreviewList.push(comment);
          }
        });
        if (commentsPreviewList.length) {
          commentsPreviewList.forEach(comment => {
            if (comment.subComments && comment.subComments.length) {
              comment.subComments.sort(this.sortComments).reverse();
              commentsPreviewList.push(...comment.subComments.slice(0, 2));
            }
          });
          commentsPreviewList.sort(this.sortComments);
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
    sortComments(comment1, comment2) {
      return new Date(comment1.createDate).getTime() - new Date(comment2.createDate).getTime();
    },
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