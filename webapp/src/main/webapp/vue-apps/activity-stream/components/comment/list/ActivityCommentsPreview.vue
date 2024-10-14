<template>
  <div>
    <v-progress-linear
      v-if="loading"
      color="primary"
      height="2"
      indeterminate />
    <activity-comments
      v-if="displayComments"
      :activity="activity"
      :comments="comments"
      :comment-types="commentTypes"
      :comment-actions="commentActions"
      :class="parentCommentClass"
      @comment-created="retrieveLastComment"
      @comment-deleted="retrieveLastComment"
      @comment-updated="retrieveLastComment" />
    <v-btn
      v-if="commentsSize > 0"
      :disabled="loading"
      class="primary--text font-weight-bold mb-1 px-0"
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
    displayComments: {
      type: Boolean,
      default: false,
    }
  },
  data: () => ({
    comments: [],
    commentsSize: 0,
    limit: 2,
    loading: true,
  }),
  computed: {
    parentCommentClass() {
      return this.commentsSize && 'pb-0 pt-5' || 'pa-0';
    },
  },
  watch: {
    displayComments() {
      if (this.displayComments) {
        this.retrieveLastComment();
      }
    }
  },
  created() {
    if (this.activity && this.activity.comments && typeof this.activity.comments === 'object') {
      this.comments = [];
      this.commentsSize = 0;
      this.loading = true;
      this.$nextTick().then(() => {
        this.comments = this.$activityService.computeParentCommentsList(this.activity.comments) || [];
        this.activity.commentsSize = this.commentsSize = this.activity.commentsCount && Number(this.activity.commentsCount) || 0;
        this.$root.$emit('activity-comments-retrieved', this.activity, this.comments);
        this.loading = false;
      });
    } else {
      this.retrieveLastComment();
    }
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
      }}));
    },
  },
};
</script>
