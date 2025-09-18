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
      class="application-background-color application-border application-border-radius activity-detail flex d-flex flex-column mb-5 contentBox">
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
        <activity-stream-empty-message v-else />
      </template>
    </template>
    <v-btn
      v-if="hasMore"
      :loading="loading"
      :disabled="loading"
      block
      class="btn pa-0 application-background-color application-border application-border-radius"
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
    initialized: false,
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
    selectedCategoryIds() {
      return this.$root.selectedCategoryIds?.length ? this.$root.selectedCategoryIds : this.$root.categoryIds;
    },
    excludeCategoryIds() {
      return this.$root.excludeCategoryIds;
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
    selectedCategoryIds() {
      if (this.initialized) {
        this.refreshActivities();
      }
    },
    excludeCategoryIds() {
      if (this.initialized) {
        this.refreshActivities();
      }
    },
  },
  created() {
    this.streamFilter = this.$activityUtils.getStreamFilter(this.$root.appId);
    this.$root.$on('activity-favorite-removed', this.handleFavoriteRemoved);
    this.$root.$on('activity-stream-type-filter-applied', this.handleStreamTypeChanged);
    this.$root.$on('activity-updated', this.handleUpdated);
    this.$root.$on('activity-stream-activity-updateActivity', this.updateActivityDisplayById);
    this.$root.$on('activities-refresh', this.refreshActivities);
    this.$root.$on('activity-read', this.markActivityAsRead);
    this.$root.$on('activity-loaded', this.refreshUnreadCount);
    this.$root.$on('set-activity-comment-size', this.setActivityCommentSize);
    document.addEventListener('categories-updated', this.refreshActivitiesByCategories);
    document.addEventListener('activity-deleted', this.handleDeletedByEvent);
    document.addEventListener('activity-pinned', this.handlePinnedByEvent);
    document.addEventListener('activity-unpinned', this.handleUnpinnedByEvent);
    document.addEventListener('activity-updated', this.handleUpdatedByEvent);

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
  beforeDestroy() {
    this.$root.$off('activity-favorite-removed', this.handleFavoriteRemoved);
    this.$root.$off('activity-stream-type-filter-applied', this.handleStreamTypeChanged);
    this.$root.$off('activity-updated', this.handleUpdated);
    this.$root.$off('activity-stream-activity-updateActivity', this.updateActivityDisplayById);
    this.$root.$off('activities-refresh', this.refreshActivities);
    this.$root.$off('activity-read', this.markActivityAsRead);
    this.$root.$off('activity-loaded', this.refreshUnreadCount);
    this.$root.$off('set-activity-comment-size', this.setActivityCommentSize);
    document.removeEventListener('categories-updated', this.refreshActivitiesByCategories);
    document.removeEventListener('activity-deleted', this.handleDeletedByEvent);
    document.removeEventListener('activity-pinned', this.handlePinnedByEvent);
    document.removeEventListener('activity-unpinned', this.handleUnpinnedByEvent);
    document.removeEventListener('activity-updated', this.handleUpdatedByEvent);
  },
  methods: {
    init() {
      if (this.activityId) {
        return this.loadActivity();
      } else {
        return this.loadActivityIds();
      }
    },
    handleStreamTypeChanged(streamFilter) {
      this.streamFilter = streamFilter;
      this.activities = [];
      this.loadActivityIds();
    },
    handleUpdated(activityId, activity) {
      if (activity) {
        this.updateActivityDisplay(activity);
      } else {
        this.updateActivityDisplayById(activityId);
      }
    },
    handleUpdatedByEvent(event) {
      const activityId = event && event.detail;
      this.updateActivityDisplayById(activityId);
    },
    handleUnpinnedByEvent(event) {
      const unpinnedActivity = event?.detail;
      if (this.pinActivityEnabled) {
        const index = this.activitiesToDisplay.findIndex(activity => unpinnedActivity.id === activity.id);
        if (index >= 0) {
          this.$set(this.activitiesToDisplay[index], 'pinned', false);
          this.activitiesToDisplay.splice(index, 1);
          this.$forceUpdate();
          setTimeout(() => {
            let added = false;
            for (let i = 0; i < this.activitiesToDisplay.length; i++) {
              if ((new Date(unpinnedActivity.updateDate) > new Date(this.activitiesToDisplay[i].updateDate)) && !this.activitiesToDisplay[i].pinned) {
                this.activitiesToDisplay.splice(i, 0, unpinnedActivity);
                added = true;
                break;
              }
            }
            if (!added && !this.hasMore) {
              this.activitiesToDisplay.push(unpinnedActivity);
            }
            this.$forceUpdate();
          }, 50);
        }
      } else {
        const activity = this.activitiesToDisplay.find(a => unpinnedActivity.id === a.id);
        if (activity) {
          this.$set(activity, 'pinned', false);
        }
      }
      this.displayAlert(this.$t('UIActivity.label.successfullyUnpinned'));
    },
    handlePinnedByEvent(event) {
      const pinnedActivity = event?.detail;
      if (this.pinActivityEnabled) {
        const index = this.activitiesToDisplay.findIndex(activity => pinnedActivity.id === activity.id);
        if (index >= 0) {
          this.$set(this.activitiesToDisplay[index], 'pinned', true);
          this.activitiesToDisplay.splice(index, 1);
          this.$forceUpdate();
          const self = this;
          setTimeout(function () {
            self.activitiesToDisplay.unshift(pinnedActivity);
            self.$forceUpdate();
          }, 10);
        }
      } else {
        const activity = this.activitiesToDisplay.find(a => pinnedActivity.id === a.id);
        if (activity) {
          this.$set(activity, 'pinned', true);
        }
      }
      this.displayAlert(this.$t('UIActivity.label.successfullyPinned'));
    },
    handleDeletedByEvent(event) {
      const activityId = event?.detail;
      if (this.activityId === activityId) { // standalone
        this.isDeleted = true;
        const activity = this.activities.find(obj => this.activityId === obj.id);
        if (activity) {
          setTimeout(() => {
            if (activity.activityStream.type === 'space') {
              location.href = `${eXo.env.portal.context}/s/${activity.activityStream.space.id}`;
            } else {
              location.href = eXo.env.portal.context;
            }
          }, 500);
        }
      } else if (activityId) { // stream
        const index = this.activities.findIndex(activity => activityId === activity.id);
        if (index >= 0) {
          this.activities.splice(index, 1);
          this.$forceUpdate();
        }
      }
    },
    handleFavoriteRemoved(activity) {
      if (this.streamFilter === 'user_favorite_stream') {
        this.$set(activity, 'deleted', true);
        window.setTimeout(() => {
          const index = this.activities.findIndex(a => activity.id === a.id);
          if (index >= 0) {
            this.activities.splice(index, 1);
          }
        }, 200);
      }
    },
    loadActivity() {
      this.loading = true;
      return this.$activityService.getActivityById(this.activityId, this.$activityConstants.FULL_ACTIVITY_EXPAND)
        .then(this.setDisplayedActivity)
        .catch(() => this.error = true)
        .finally(() => this.loading = false);
    },
    refreshActivitiesByCategories(event) {
      const objectType = event?.detail?.objectType;
      const objectId = event?.detail?.objectId;
      if (objectType === 'activity'
          && this.activitiesToDisplay.find(a => a.id === objectId)
          && this.$root.allowFilteringPerCategory
          && (this.selectedCategoryIds?.length || this.$root.settingsSubcategoryIds?.length)) {
        this.loadActivityIds();
      }
    },
    refreshActivities() {
      this.activities = [];
      this.loadActivityIds();
    },
    loadActivityIds() {
      if (this.loading) {
        return;
      }
      this.loading = true;
      return this.$activityService.getActivitiesByFilter({
        spaceId: this.spaceId,
        streamType: this.streamFilter,
        limit: this.limit * 2,
        categoryIds: this.selectedCategoryIds,
        excludedCategoryIds: this.excludeCategoryIds,
        expand: this.$activityConstants.FULL_ACTIVITY_IDS_EXPAND,
        showPinned: !!this.spaceId
      })
        .then(data => {
          this.$emit('can-post-loaded', data.canPost);
          const activityIds = data && (data.activityIds || data.activities) || [];
          this.retrievedSize = activityIds.length;
          this.hasMore = activityIds.length > this.limit;
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
                .then(fullActivity => this.activities.splice(this.activities.indexOf(activity), 1, fullActivity))
                .catch(() => this.activities.splice(this.activities.indexOf(activity), 1))
                .finally(() => this.$set(activity, 'loading', false));
            }
          });
          return Promise.all(promises);
        })
        .finally(() => {
          this.initialized = true;
          this.loading = false;
        });
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
      activityId = Number(activityId);
      if (this.activitiesToDisplay?.find(a => Number(a.id) === activityId)) {
        this.loading = true;
        return this.$activityService.getActivityById(activityId, this.$activityConstants.ACTIVITY_EXPAND)
          .then(activity => this.updateActivityDisplay(activity))
          .finally (() => this.loading = false);
      }
    },
    updateActivityDisplay(updatedActivity) {
      const index = this.activitiesToDisplay.findIndex(activity => Number(updatedActivity.id) === Number(activity.id));
      if (index >= 0) {
        const activityToUpdate = {
          ...this.activities[index],
          metadatas: {},
          ...updatedActivity,
          categoryIds: updatedActivity?.categoryIds || null,
        };
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
    setActivityCommentSize(activityId, activityCommentSize, activityTotalCommentSize) {
      const activity = this.activities.find(activity => activity.id === activityId);
      if (activity) {
        activity.totalCommentsSize = activityTotalCommentSize;
        activity.commentsSize = activityCommentSize;
      }
    }
  },
};
</script>
