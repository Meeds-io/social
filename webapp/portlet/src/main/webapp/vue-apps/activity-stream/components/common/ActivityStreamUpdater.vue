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
    standalone: {
      type: Boolean,
      default: false,
    },
    streamFilter: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    updatedActivities: new Set(),
    newerActivitiesCount: 0,
  }),
  computed: {
    hasNewActivity() {
      return !this.standalone && this.streamFilter === 'all_stream' && this.newerActivitiesCount > 0;
    },
  },
  created() {
    this.$activityStreamWebSocket.initCometd(this.handleActivityStreamUpdates);
    this.$root.$on('activity-stream-activity-deleteActivity', this.broadcastActivityDeleted);
    this.$root.$on('activity-stream-activity-createActivity', this.increaseActivitiesLimitToRetrieve);
    this.$root.$on('activity-stream-activity-deleteComment', this.broadcastCommentDeleted);
  },
  methods: {
    init() {
      this.newerActivitiesCount = 0;
      this.updatedActivities = new Set();
    },
    handleActivityStreamUpdates(updateParams) {
      // handle activity stream updates
      this.$root.$emit(`activity-stream-activity-${updateParams.eventName}`, updateParams.activityId, updateParams.spaceId, updateParams.commentId, updateParams.parentCommentId);
    },
    increaseActivitiesLimitToRetrieve(activityId, spaceId) {
      if (!eXo.env.portal.spaceId || eXo.env.portal.spaceId === spaceId) {
        this.updatedActivities.add(activityId);
        this.newerActivitiesCount = this.updatedActivities.size;
      }
    },
    displayNewActivities() {
      if (this.$activityStreamWebSocket.isDisconnected()) {
        window.location.reload();
        return;
      }
      this.$emit('loadActivities', this.updatedActivities.size);
      this.init();
    },
    broadcastActivityDeleted(activityId) {
      document.dispatchEvent(new CustomEvent('activity-deleted', {detail: activityId}));
    },
    broadcastCommentDeleted(activityId, spaceId, commentId, parentCommentId) {
      document.dispatchEvent(new CustomEvent('activity-comment-deleted', {detail: {
        activityId: activityId,
        spaceId: spaceId,
        commentId: commentId,
        parentCommentId: parentCommentId,
      }}));
    }
  },
};
</script>