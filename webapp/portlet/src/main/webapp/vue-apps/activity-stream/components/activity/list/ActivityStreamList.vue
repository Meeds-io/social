<template>
  <div
    :class="activityStreamTypeClass"
    class="activityStream pa-0">
    <exo-activity-composer
      v-if="canPost"
      id="activityComposer" />
    <activity-stream-confirm-dialog />
    <activity-stream-updater
      ref="activityUpdater"
      v-if="!activityId"
      :space-id="spaceId"
      :activities="activities"
      @addActivities="addActivities" />
    <template v-if="activitiesToDisplay.length">
      <activity-stream-activity
        v-for="activity of activitiesToDisplay"
        :key="activity.id"
        :activity="activity"
        :activity-types="activityTypes"
        :activity-actions="activityActions"
        :comment-types="commentTypes"
        :comment-actions="commentActions"
        :is-activity-detail="activityId"
        class="mb-6 contentBox" />
    </template>
    <template v-else-if="!loading">
      <activity-not-found v-if="activityId" />
      <template v-else-if="!error">
        <activity-stream-empty-message-space v-if="spaceId" />
        <activity-stream-empty-message-user v-else />
      </template>
    </template>
    <v-btn
      v-if="hasMore"
      :loading="loading"
      :disabled="loading"
      block
      class="btn pa-0"
      @click="loadMore">
      {{ $t('Search.button.loadMore') }}
    </v-btn>
    <activity-auto-link />
  </div>
</template>

<script>
export default {
  props: {
    activityId: {
      type: String,
      default: null,
    },
    activityTypes: {
      type: Object,
      default: null,
    },
    activityActions: {
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
    initialData: {
      type: Object,
      default: null,
    },
    initialLimit: {
      type: Number,
      default: null,
    },
  },
  data: () => ({
    activities: [{
      loading: true,
    }],
    pageSize: 10,
    limit: 10,
    retrievedSize: 0,
    spaceId: eXo.env.portal.spaceId,
    userName: eXo.env.portal.userName,
    canPost: false,
    hasMore: false,
    loading: false,
    error: false,
  }),
  computed: {
    activitiesToDisplay() {
      return this.activities && this.activities.filter(activity => activity && !activity.activityId) || [];
    },
    activityStreamTypeClass() {
      return this.spaceId && 'activity-stream-space' || 'activity-stream-user';
    },
  },
  watch: {
    loading() {
      if (this.loading) {
        document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      } else {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
        window.setTimeout(() => {
          socialUIProfile.initUserProfilePopup('ActivityStream', {});
          document.dispatchEvent(new CustomEvent('analytics-install-watchers'));
        }, 500);
      }
    },
  },
  created() {
    document.addEventListener('activity-deleted', event => {
      const activityId = event && event.detail;
      if (activityId) {
        const index = this.activities.findIndex(activity => activityId === activity.id);
        if (index >= 0) {
          this.activities.splice(index, 1);
          this.$forceUpdate();
        }
      }
    });
    document.addEventListener('activity-updated', event => {
      const activityId = event && event.detail;
      this.updateActivityDisplayById(activityId);
    });
    this.$root.$on('activity-updated', (activityId, activity) => {
      if (activity) {
        this.updateActivityDisplay(activity);
      } else {
        this.updateActivityDisplayById(activityId);
      }
    });

    this.activities = [];
    this.limit = this.pageSize;
    this.retrievedSize = (this.initialLimit && this.initialLimit) / 2 || this.limit;
    this.hasMore = false;
    this.init();
  },
  methods: {
    init() {
      if (this.activityId) {
        if (this.initialData) {
          this.setDisplayedActivity(this.initialData);
        } else {
          this.loadActivity();
        }
      } else {
        if (this.initialData) {
          this.loadActivityIds(this.initialData);
        } else {
          this.loadActivities();
        }
      }
      if (this.$refs && this.$refs.activityUpdater) {
        this.$refs.activityUpdater.init();
      }
    },
    loadActivity() {
      this.loading = true;
      this.$activityService.getActivityById(this.activityId, this.$activityConstants.FULL_ACTIVITY_EXPAND)
        .then(this.setDisplayedActivity)
        .catch(() => this.error = true)
        .finally(() => this.loading = false);
    },
    loadActivities() {
      // Load 'retrievedSize + 10' instead of only 'limit' to avoid retrieving count of user activities
      // Which can be CPU consuming in server side.
      // If the retrieved elements count > 'limit', then there are more elements to retrieve,
      // else no more elements to retrieve
      const limitToRetrieve = this.retrievedSize + 10;

      this.loading = true;
      this.$activityService.getActivities(this.spaceId, limitToRetrieve, 'ids')
        .then(this.loadActivityIds)
        .then(this.handleRetrievedActivities)
        .catch(() => this.error = true)
        .finally(() => this.loading = false);
    },
    loadActivityIds(data) {
      this.canPost = data.canPost;
      const activityIds = data && data.activityIds || [];
      const promises = activityIds.map(activity => {
        this.$set(activity, 'loading', true);
        const activityId = activity && activity.id;
        if (activityId) {
          return this.$activityService.getActivityById(activityId, this.$activityConstants.FULL_ACTIVITY_EXPAND)
            .then(fullActivity => {
              Object.assign(activity, fullActivity);
              this.$set(activity, 'loading', false);
              return fullActivity;
            });
        }
      });
      this.handleRetrievedActivities(activityIds);
      return Promise.all(promises);
    },
    setDisplayedActivity(activity) {
      if (activity.activityId) { // a comment
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
        this.$emit('activity-select', activity.activityId, activity.id);
        return;
      }
      this.activities = activity && [activity] || [];
    },
    handleRetrievedActivities(activities) {
      this.activities = activities.filter(activity => !!activity).slice(0, this.limit);
      this.retrievedSize = activities.length;
      this.hasMore = this.retrievedSize > this.limit;
    },
    updateActivityDisplayById(activityId) {
      this.loading = true;
      return this.$activityService.getActivityById(activityId, this.$activityConstants.FULL_ACTIVITY_EXPAND)
        .then(activity => this.updateActivityDisplay(activity))
        .finally (() => this.loading = false);
    },
    updateActivityDisplay(updatedActivity) {
      const index = this.activities.findIndex(activity => updatedActivity.id === activity.id);
      if (index >= 0) {
        this.activities.splice(index, 1, updatedActivity);
        this.$root.$emit('activity-refresh-ui', updatedActivity.id);
      }
    },
    loadMore() {
      if (this.activities.length === this.limit) {
        this.limit += this.pageSize;
      }
      this.loadActivities();
    },
    addActivities(activities) {
      if (activities && activities.length) {
        this.loading = true;
        const activity = activities[0];
        activity.highlight = true;
        this.activities.unshift(...activities);
        this.$nextTick().then(() => this.loading = false);
      }
    },
  },
};
</script>