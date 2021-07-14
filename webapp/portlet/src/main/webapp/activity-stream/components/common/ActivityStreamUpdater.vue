<template>
  <div class="newActivitiesButtonContainer d-flex">
    <div class="mx-auto">
      <v-btn
        v-show="hasNewActivity"
        class="btn primary rounded-xl newActivitiesButton"
        height="24px"
        small
        primary
        absolute
        @click="displayNewActivities">
        <v-icon size="14px" class="me-1">fa-arrow-up</v-icon>
        <span>{{ $t('activity.button.newPosts') }}</span>
      </v-btn>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    spaceId: {
      type: String,
      default: null,
    },
    activities: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    newActivities: [],
    updatedActivities: new Set(),
    limitToRetrieve: 0,
    activityCreatedEventName: 'createActivity',
    activityUpdatedEventName: 'updateActivity',
    commentCreatedEventName: 'createComment',
    commentUpdatedEventName: 'updateComment',
    activityLikedEventName: 'likeActivity',
    commentLikedEventName: 'likeComment',
  }),
  computed: {
    hasNewActivity() {
      return this.newActivities.length > 0;
    },
  },
  created() {
    this.$activityStreamWebSocket.initCometd(this.handleActivityStreamUpdates);
    this.$root.$on(`activity-stream-activity-${this.activityCreatedEventName}`, this.checkNewerActivities);
    this.$root.$on(`activity-stream-activity-${this.activityUpdatedEventName}`, this.increaseActivitiesLimitToRetrieve);
    this.$root.$on(`activity-stream-activity-${this.commentCreatedEventName}`, this.increaseActivitiesLimitToRetrieve);
    this.$root.$on(`activity-stream-activity-${this.commentUpdatedEventName}`, this.increaseActivitiesLimitToRetrieve);
  },
  methods: {
    init() {
      this.newActivities = [];
      this.updatedActivities = new Set();
    },
    handleActivityStreamUpdates(updateParams) {
      // handle activity stream updates
      this.$root.$emit(`activity-stream-activity-${updateParams.eventName}`, updateParams.activityId, updateParams.commentId, updateParams.parentCommentId);
    },
    increaseActivitiesLimitToRetrieve(activityId) {
      this.updatedActivities.add(activityId);
      this.limitToRetrieve = this.updatedActivities.size;
    },
    checkNewerActivities(activityId) {
      this.increaseActivitiesLimitToRetrieve(activityId);
      this.$activityService.getActivities(this.spaceId, this.limitToRetrieve, this.$activityConstants.FULL_ACTIVITY_EXPAND)
        .then(data => {
          const activities = data.activities || [];
          if (activities && activities.length) {
            if (this.activities && this.activities.length) {
              const firstDisplayedActivity = this.activities[0];
              const firstDisplayedActivityDate = firstDisplayedActivity.updateDate && new Date(firstDisplayedActivity.updateDate) || new Date(firstDisplayedActivity.createDate);
              this.newActivities = activities.filter(newActivity => {
                const activityDate = newActivity.updateDate && new Date(newActivity.updateDate) || new Date(newActivity.createDate);
                return activityDate > firstDisplayedActivityDate && !this.activities.find(tmpActivity => tmpActivity.id === newActivity.id);
              });
            } else {
              this.newActivities = activities;
            }
          }
        })
        .catch(() => {
          // Just catch the error when user can't access activity to not log an exception
        });
    },
    displayNewActivities() {
      if (this.$activityStreamWebSocket.isDisconnected()) {
        window.location.reload();
        return;
      }
      const newestActivityId = this.newActivities[0].id;

      this.$emit('addActivities', this.newActivities.slice());
      this.init();

      // check if network is disconnected (checking websocket connection isn't sufficient)
      this.$activityService.getActivityById(newestActivityId)
        .catch(() => window.location.reload());
    },
  },
};
</script>