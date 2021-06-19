<template>
  <exo-drawer
    ref="activityCommentsDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    allow-expand
    right>
    <template slot="title">
      <span class="text-capitalize-first-letter">
        {{ $t('activity.comments') }}
      </span>
    </template>
    <template slot="content">
      <activity-comments :comments="comments" />
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data: () => ({
    commentsData: null,
    activityId: null,
    loading: false,
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
      }
    },
  },
  created() {
    document.addEventListener('activity-comments-display', event => {
      const options = event && event.detail;
      if (options) {
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
      }
    });
    document.addEventListener('activity-comment-added', event => {
      const activityId = event && event.detail;
      if (activityId === this.activityId) {
        this.retrieveComments();
      }
    });
    document.addEventListener('activity-comment-updated', event => {
      const activityId = event && event.detail;
      if (activityId === this.activityId) {
        this.retrieveComments();
      }
    });
  },
  methods: {
    retrieveComments() {
      this.loading = true;
      this.$activityService.getActivityComments(this.activityId, false, this.offset, this.limit, this.$activityConstants.FULL_COMMENT_EXPAND)
        .then(data => this.commentsData = data)
        .finally(() => this.loading = false);
    },
  },
};
</script>