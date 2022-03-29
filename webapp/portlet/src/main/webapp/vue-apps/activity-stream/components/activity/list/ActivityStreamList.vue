<template>
  <div
    :class="activityStreamTypeClass"
    class="activityStream pa-0">
    <activity-composer
      :standalone="!canPost"
      :selected-filter="selectedFilter"
      id="activityComposer" />
    <activity-stream-confirm-dialog />
    <activity-stream-updater
      ref="activityUpdater"
      v-if="!activityId"
      :space-id="spaceId"
      :activities="activities"
      @loadActivities="loadActivities" />
    <template v-if="activitiesToDisplay.length">
      <activity-stream-loader
        v-for="activity of activitiesToDisplay"
        :key="activity.id"
        :activity="activity"
        :activity-types="activityTypes"
        :activity-actions="activityActions"
        :comment-types="commentTypes"
        :comment-actions="commentActions"
        :is-activity-detail="activityId"
        class="mb-6 contentBox"
        @loaded="activityLoaded(activity.id)" />
    </template>
    <div
      v-else-if="loading"
      class="white border-radius activity-detail flex d-flex flex-column mb-6 contentBox">
      <v-progress-circular
        color="primary"
        size="32"
        indeterminate
        class="mx-auto my-10" />
    </div>
    <template v-else>
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
  },
  data: () => ({
    activities: [],
    pageSize: 10,
    limit: 10,
    retrievedSize: 0,
    loadedActivities: new Set(),
    selectedFilter: '',
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
      if (!this.loading) {
        window.setTimeout(() => {
          document.dispatchEvent(new CustomEvent('analytics-install-watchers'));
        }, 500);
        if (!this.activities.length || this.loadedActivities >= this.activitiesToDisplay.length) {
          this.$root.$applicationLoaded();
        }
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
    this.$root.$on('filter-activities',(filter) => {
      if (filter) {
        this.selectedFilter = filter;
        this.getActivities();
      }
    });
    const filterQueryParam = localStorage.getItem('ActivityFilter');
    if (filterQueryParam) {
      // set filter value, which will trigger activities fetching
      this.selectedFilter = filterQueryParam;
    } else {
      this.selectedFilter = 'all';
    }
    this.getActivities();
  },
  methods: {
    init() {
      if (this.activityId) {
        return this.loadActivity();
      } else {
        return this.loadActivityIds();
      }
    },
    loadActivity() {
      this.loading = true;
      return this.$activityService.getActivityById(this.activityId, this.$activityConstants.FULL_ACTIVITY_EXPAND)
        .then(this.setDisplayedActivity)
        .catch(() => this.error = true)
        .finally(() => this.loading = false);
    },
    loadActivityIds() {
      this.loading = true;
      const filterToSearch = this.selectedFilter.replace(/\s/g, '').toUpperCase();
      return this.$activityService.getActivities(this.spaceId, this.limit * 2, filterToSearch, this.$activityConstants.FULL_ACTIVITY_IDS_EXPAND)
        .then(data => {
          this.canPost = data.canPost;
          const activityIds = data && (data.activityIds || data.activities) || [];
          this.retrievedSize = activityIds.length;
          this.hasMore = this.retrievedSize > this.limit;
          const activityIdsToLoad = activityIds.slice(0, this.limit);
          const promises = activityIdsToLoad.map((activity, index) => {
            const activityId = activity && activity.id;
            const existingActivity = this.activities.find(loadedActivity => loadedActivity.id === activityId);
            if (existingActivity) {
              return Promise.resolve(existingActivity);
            } else if (activityId) {
              this.$set(activity, 'loading', true);
              if (index < this.activities.length) {
                this.activities.splice(index, 0, activity);
              } else {
                this.activities.push(activity);
              }
              return this.$activityService.getActivityById(activityId, this.$activityConstants.FULL_ACTIVITY_EXPAND)
                .then(fullActivity => Object.assign(activity, fullActivity))
                .finally(() => this.$set(activity, 'loading', false));
            }
          });
          return Promise.all(promises);
        })
        .finally(() => this.loading = false);
    },
    activityLoaded(activityId) {
      this.loadedActivities.add(activityId);
      if (this.loadedActivities.size && this.loadedActivities.size >= this.activitiesToDisplay.length) {
        this.$root.$applicationLoaded();
      }
    },
    setDisplayedActivity(activity) {
      if (activity.activityId) { // a comment
        this.$emit('activity-select', activity.activityId, activity.id);
        return;
      }
      this.activities = activity && [activity] || [];
    },
    updateActivityDisplayById(activityId) {
      this.loading = true;
      return this.$activityService.getActivityById(activityId, this.$activityConstants.ACTIVITY_EXPAND)
        .then(activity => this.updateActivityDisplay(activity))
        .finally (() => this.loading = false);
    },
    updateActivityDisplay(updatedActivity) {
      const index = this.activities.findIndex(activity => updatedActivity.id === activity.id);
      if (index >= 0) {
        const activityToUpdate = Object.assign({}, this.activities[index], updatedActivity);
        this.activities.splice(index, 1, activityToUpdate);
        this.$root.$emit('activity-refresh-ui', updatedActivity.id);
      }
    },
    loadMore() {
      this.limit += this.pageSize;
      this.loadActivityIds();
    },
    loadActivities(newActivitiesCount) {
      this.limit += newActivitiesCount;
      this.loadActivityIds().catch(() => window.location.reload());
    },
    getActivities() {
      if (this.activities.length > 0) {
        this.activities = [];
      }
      this.limit = this.pageSize;
      this.retrievedSize = this.limit;
      this.hasMore = false;
      Promise.resolve(this.init())
        .finally(() => {
          if (this.$refs && this.$refs.activityUpdater) {
            this.$refs.activityUpdater.init();
          }
          document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
        });
    },
  },
};
</script>
