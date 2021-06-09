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
  },
  data: () => ({
    newActivities: [],
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
  },
  methods: {
    init() {
      this.newActivities = [];
    },
    handleActivityStreamUpdates(updateParams) {
      // handle activity stream updates
      this.$root.$emit(`activity-stream-activity-${updateParams.eventName}`, updateParams.activityId);
    },
    checkNewerActivities(activityId) {
      this.$activityService.getActivityById(activityId, this.$activityConstants.FULL_ACTIVITY_EXPAND)
        .then(activity => {
          // If the activity has been retrieved
          // and the displayed activity stream corresponds
          // to Activity's stream, the add the activity
          if (activity
              && (!this.spaceId
                  || (activity.activityStream
                      && activity.activityStream.space
                      && activity.activityStream.space.id === this.spaceId))) {
            this.newActivities.unshift(activity);
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