<template>
  <div
    v-show="display"
    :class="activityStreamTypeClass"
    class="activityStream pa-0">
    <activity-stream-confirm-dialog />
    <activity-stream-updater
      ref="activityUpdater"
      v-if="!activityId"
      :space-id="spaceId"
      @addActivities="addActivities" />
    <activity-stream-activity
      v-for="activity of activities"
      :key="activity.id"
      :activity="activity"
      :activity-types="activityTypes"
      :activity-actions="activityActions"
      :is-activity-detail="activityId"
      class="mb-6 contentBox" />
    <v-btn
      v-if="hasMore"
      :loading="loading"
      :disabled="loading"
      block
      class="btn border-box-sizing"
      @click="loadMore">
      {{ $t('Search.button.loadMore') }}
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
    activityTypes: {
      type: Object,
      default: null,
    },
    activityActions: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    activities: [],
    pageSize: 10,
    limit: 10,
    spaceId: eXo.env.portal.spaceId,
    userName: eXo.env.portal.userName,
    hasMore: false,
    loading: false,
    display: false,
  }),
  computed: {
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
    this.$root.$on('activity-stream-display', () => {
      this.activities = [];
      this.limit = this.pageSize;
      this.display = true;
      this.init();
    });
    this.$root.$on('activity-stream-hide', () => {
      this.display = false;
    });
    document.addEventListener('activity-stream-activity-deleted', event => {
      const activityId = event && event.detail;
      if (activityId) {
        const index = this.activities.findIndex(activity => activityId === activity.id);
        if (index >= 0) {
          this.activities.splice(index, 1);
          this.$forceUpdate();
        }
      }
    });
    document.addEventListener('activity-stream-activity-updated', event => {
      const activityId = event && event.detail;
      this.updateActivityDisplayById(activityId);
    });
    this.$root.$on('activity-stream-activity-updated', (activityId, activity) => {
      if (activity) {
        this.updateActivityDisplay(activity);
      } else {
        this.updateActivityDisplayById(activityId);
      }
    });
  },
  methods: {
    init() {
      if (this.activityId) {
        this.loadActivity();
      } else {
        this.loadActivities();
      }
      if (this.$refs && this.$refs.activityUpdater) {
        this.$refs.activityUpdater.init();
      }
    },
    loadActivity() {
      this.loading = true;
      this.$activityService.getActivityById(this.activityId, this.$activityConstants.FULL_ACTIVITY_EXPAND)
        .then(activity => this.activities = activity && [activity] || [])
        .finally(() => this.loading = false);
    },
    loadActivities() {
      this.loading = true;
      // Load 'limit + 1' instead of only 'limit' to avoid retrieving count of user activities
      // Which can be CPU consuming in server side.
      // If the retrieved elements count > 'limit', then there are more elements to retrieve,
      // else no more elements to retrieve
      const limitToRetrieve = this.limit + 1;
      if (this.spaceId) {
        this.$activityService.getSpaceActivities(this.spaceId, limitToRetrieve, this.$activityConstants.FULL_ACTIVITY_EXPAND)
          .then(data => this.handleRetrievedActivities(data && data.activities || []))
          .finally(() => this.loading = false);
      } else {
        this.$activityService.getUserActivities(this.userName, limitToRetrieve, this.$activityConstants.FULL_ACTIVITY_EXPAND)
          .then(data => this.handleRetrievedActivities(data && data.activities || []))
          .finally(() => this.loading = false);
      }
    },
    handleRetrievedActivities(activities) {
      this.activities = activities.slice(0, this.limit);
      this.hasMore = activities.length > this.limit;
    },
    updateActivityDisplayById(activityId) {
      document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      this.$activityService.getActivityById(activityId, this.$activityConstants.FULL_ACTIVITY_EXPAND)
        .then(activity => this.updateActivityDisplay(activity))
        .finally (() => document.dispatchEvent(new CustomEvent('hideTopBarLoading')));
    },
    updateActivityDisplay(updatedActivity) {
      const index = this.activities.findIndex(activity => updatedActivity.id === activity.id);
      if (index >= 0) {
        this.activities.splice(index, 1, updatedActivity);
        this.$forceUpdate();
      }
    },
    loadMore() {
      if (this.activities.length === this.limit) {
        this.limit += this.pageSize;
      }
      this.loadActivities();
    },
    addActivities(activities) {
      this.activities.unshift(...activities);
    },
  },
};
</script>