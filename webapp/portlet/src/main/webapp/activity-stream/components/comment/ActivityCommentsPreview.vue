<template>
  <v-list v-if="lastComment" class="pb-0 pt-5 border-top-color">
    <activity-comment :comment="lastComment" />
    <activity-comment
      v-if="lastSubComment"
      :comment="lastSubComment"
      sub-comment />
  </v-list>
</template>

<script>
export default {
  props: {
    activityId: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    commentsData: null,
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
  },
  created() {
    document.addEventListener('activity-comment-added', event => {
      const activityId = event && event.detail;
      if (activityId === this.activityId) {
        this.retrieveLastComment();
      }
    });
    document.addEventListener('activity-comment-updated', event => {
      const activityId = event && event.detail;
      if (activityId === this.activityId) {
        this.retrieveLastComment();
      }
    });
    this.retrieveLastComment();
  },
  methods: {
    retrieveLastComment() {
      this.$activityService.getActivityComments(this.activityId, true, 0, 1, this.$activityConstants.FULL_COMMENT_EXPAND)
        .then(data => this.commentsData = data);
    },
  },
};
</script>