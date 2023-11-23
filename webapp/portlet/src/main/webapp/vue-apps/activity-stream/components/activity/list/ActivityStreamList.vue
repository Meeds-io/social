<template>
  <div
    :class="activityStreamTypeClass"
    class="activityStream pa-0">
    <activity-stream-confirm-dialog />
    <activity-stream-updater
      ref="activityUpdater"
      :space-id="spaceId"
      :activity-ids="activityIds"
      :standalone="!!activityId"
      :stream-filter="streamFilter"
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
        :pin-activity-enabled="pinActivityEnabled"
        class="mb-5 contentBox"
        @loaded="activityLoaded(activity.id)" />
    </template>
    <div
      v-else-if="loading"
      class="white border-radius activity-detail flex d-flex flex-column mb-5 contentBox">
      <v-progress-circular
        color="primary"
        size="32"
        indeterminate
        class="mx-auto my-10" />
    </div>
    <template v-else-if="!isDeleted">
      <activity-not-found v-if="activityId" />
      <template v-else-if="!error">
        <activity-stream-empty-message-filter v-if="streamFilter && streamFilter !== 'all_stream'" :stream-filter="streamFilter" />
        <activity-stream-empty-message-space v-else-if="spaceId" />
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
    spaceId: eXo.env.portal.spaceId,
    userName: eXo.env.portal.userName,
    hasMore: false,
    loading: false,
    error: false,
    isDeleted: false,
    streamFilter: 'all_stream',
    hadUnread: false,
    unreadCount: 0,
  }),
  computed: {
    activitiesCount() {
      return this.activitiesToDisplay.length;
    },
    activitiesToDisplay() {
      return this.activities && this.activities.filter(activity => activity && !activity.activityId) || [];
    },
    activityStreamTypeClass() {
      return this.spaceId && 'activity-stream-space' || 'activity-stream-user';
    },
    pinActivityEnabled() {
      return this.spaceId && (this.streamFilter === null || this.streamFilter === 'all_stream') || false;
    },
    allActivitiesRead() {
      return this.hadUnread && !this.loading && !this.hasMore && this.streamFilter === 'unread_spaces_stream' && !this.unreadCount;
    },
    activityIds() {
      return this.activities?.map?.(a => a.id)?.filter?.(id => !!id) || [];
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
        if (!this.activities.length && this.streamFilter === 'unread_spaces_stream') {
          this.$root.$emit('activity-stream-notify-all-read', true);
        }
        this.$emit('has-activities', this.activitiesCount > 0);
      }
    },
    activities() {
      this.refreshUnreadCount();
    },
    allActivitiesRead() {
      if (this.allActivitiesRead) {
        this.$root.$emit('activity-stream-notify-all-read', true);
      }
    },
    unreadCount() {
      if (this.unreadCount > 0) {
        this.hadUnread = true;
      } else {
        this.hadUnread = false;
      }
    },
  },
  created() {
    this.streamFilter = this.$activityUtils.getStreamFilter();
    document.addEventListener('activity-favorite-removed', event => {
      const favoriteActivity = event?.detail;
      if (this.streamFilter === 'user_favorite_stream') {
        this.$set(favoriteActivity, 'deleted', true);
        const self = this;
        setTimeout(function() {
          const index = self.activities.findIndex(activity => favoriteActivity.id === activity.id);
          if (index >= 0) {
            self.activities.splice(index, 1);
          }
        }, 200);
      }
    });
    document.addEventListener('activity-deleted', event => {
      const activityId = event?.detail;
      if (this.activityId === activityId) {
        this.isDeleted = true;
        const activity = this.activities.find(obj => activityId === obj.id);
        if (activity) {
          setTimeout(() => {
            if (activity.activityStream.type === 'space') {
              location.href = `${eXo.env.portal.context}/g/${activity.activityStream.space.groupId.replace(/\//g, ':')}`;
            } else {
              location.href = eXo.env.portal.context;
            }
          }, 500);
        }
      }
      if (activityId) {
        const index = this.activities.findIndex(activity => activityId === activity.id);
        if (index >= 0) {
          this.activities.splice(index, 1);
          this.$forceUpdate();
        }
      }
    });
    document.addEventListener('activity-pinned', event => {
      if (this.pinActivityEnabled) {
        const pinnedActivity = event?.detail;
        this.$set(pinnedActivity, 'pinned', true);
        const index = this.activitiesToDisplay.findIndex(activity => pinnedActivity.id === activity.id);
        this.activitiesToDisplay.splice(index, 1);
        this.$forceUpdate();
        const self = this;
        setTimeout(function () {
          self.activitiesToDisplay.unshift(pinnedActivity);
          self.$forceUpdate();
        }, 10);
      }
      this.displayAlert(this.$t('UIActivity.label.successfullyPinned'));
    });
    document.addEventListener('activity-unpinned', event => {
      if (this.pinActivityEnabled) {
        const unpinnedActivity = event?.detail;
        this.$set(unpinnedActivity, 'pinned', false);
        const index = this.activitiesToDisplay.findIndex(activity => unpinnedActivity.id === activity.id);
        if (index >= 0) {
          this.activitiesToDisplay.splice(index, 1);
        }
        this.$forceUpdate();
        const self = this;
        setTimeout(function () {
          let added = false;
          for (let i = 0; i < self.activitiesToDisplay.length; i++) {
            if ((new Date(unpinnedActivity.updateDate) > new Date(self.activitiesToDisplay[i].updateDate)) && !self.activitiesToDisplay[i].pinned) {
              self.activitiesToDisplay.splice(i, 0, unpinnedActivity);
              added = true;
              break;
            }
          }
          if (!added && !self.hasMore) {
            self.activitiesToDisplay.push(unpinnedActivity);
          }
          self.$forceUpdate();
        }, 50);
      }
      this.displayAlert(this.$t('UIActivity.label.successfullyUnpinned'));
    });
    document.addEventListener('activity-updated', event => {
      const activityId = event && event.detail;
      this.updateActivityDisplayById(activityId);
    });
    document.addEventListener('activity-stream-type-filter-applied', event => {
      this.streamFilter = event && event.detail;
      this.activities = [];
      this.loadActivityIds();
    });
    this.$root.$on('activity-updated', (activityId, activity) => {
      if (activity) {
        this.updateActivityDisplay(activity);
      } else {
        this.updateActivityDisplayById(activityId);
      }
    });
    this.$root.$on('activities-refresh', this.refreshActivities);
    this.$root.$on('activity-read', this.markActivityAsRead);
    this.$root.$on('activity-loaded', this.refreshUnreadCount);

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
    refreshActivities() {
      this.activities = [];
      this.loadActivityIds();
    },
    loadActivityIds() {
      this.loading = true;
      return this.$activityService.getActivities(this.spaceId, this.streamFilter, this.limit * 2, this.$activityConstants.FULL_ACTIVITY_IDS_EXPAND)
        .then(data => {
          this.$emit('can-post-loaded', data.canPost);
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
        const activityToUpdate = Object.assign({}, this.activities[index], {metadatas: {}}, updatedActivity);
        this.activities.splice(index, 1, activityToUpdate);
        this.$root.$emit('activity-refresh-ui', updatedActivity.id);
      }
    },
    loadMore() {
      if (this.streamFilter === 'unread_spaces_stream') {
        this.activities = [];
      }
      this.limit += this.pageSize;
      this.loadActivityIds();
    },
    loadActivities(newActivitiesCount) {
      this.limit += newActivitiesCount;
      this.loadActivityIds().catch(() => window.location.reload());
      this.$nextTick().then(() => {
        const streamPageContainerElement = document.getElementById('ActivityStream');
        window.setTimeout(() => {
          if (streamPageContainerElement && streamPageContainerElement.scrollIntoView) {
            streamPageContainerElement.scrollIntoView({
              behavior: 'smooth',
              block: 'start',
            });
          }
        }, 10);
      });
    },
    applyFilter(streamFilter) {
      this.streamFilter = streamFilter;
      this.loadActivityIds();
    },
    refreshUnreadCount() {
      this.unreadCount = this.activities?.filter?.(a => a?.metadatas?.unread?.length)?.length || 0;
    },
    markActivityAsRead(activityId) {
      const activity = this.activities?.find?.(a => a.id === activityId);
      if (activity?.metadatas?.unread) {
        activity.metadatas.unread = null;
      }
      this.refreshUnreadCount();
    },
    displayAlert(message, type) {
      this.$root.$emit('alert-message', message, type || 'success');
    },
  },
};
</script>
