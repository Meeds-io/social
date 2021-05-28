<template>
  <div v-show="display">
    <activity-stream-activity
      v-for="activity of activities"
      :key="activity.id"
      :activity="activity"
      :activity-types="activityTypes"
      :is-activity-detail="activityId"
      class="mb-6" />
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
  watch: {
    loading() {
      if (this.loading) {
        document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      } else {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
        window.setTimeout(() => {
          socialUIProfile.initUserProfilePopup('ActivityStream', {});
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
  },
  methods: {
    init() {
      if (this.activityId) {
        this.loadActivity();
      } else {
        this.loadActivities();
      }
    },
    loadActivity() {
      this.$activityService.getActivityById(this.activityId, 'identity')
        .then(activity => this.activities = activity && [activity] || []);
    },
    loadActivities() {
      this.loading = true;
      // Load 'limit + 1' instead of only 'limit' to avoid retrieving count of user activities
      // Which can be CPU consuming in server side.
      // If the retrieved elements count > 'limit', then there are more elements to retrieve,
      // else no more elements to retrieve
      const limitToRetrieve = this.limit + 1;
      if (this.spaceId) {
        this.$activityService.getSpaceActivities(this.spaceId, limitToRetrieve, 'identity')
          .then(data => this.handleRetrievedActivities(data && data.activities || []))
          .finally(() => this.loading = false);
      } else {
        this.$activityService.getUserActivities(this.userName, limitToRetrieve, 'identity')
          .then(data => this.handleRetrievedActivities(data && data.activities || []))
          .finally(() => this.loading = false);
      }
    },
    handleRetrievedActivities(activities) {
      this.activities = activities.slice(0, this.limit);
      this.hasMore = activities.length > this.limit;
    },
    loadMore() {
      this.limit += this.pageSize;
      this.loadActivities();
    },
  },
};
</script>