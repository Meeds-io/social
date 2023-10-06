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
    activityIds: {
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
      return !this.standalone && this.newerActivitiesCount > 0;
    },
  },
  created() {
    this.$activityStreamWebSocket.initCometd(this.handleActivityStreamUpdates);
    this.$root.$on('activity-stream-activity-createActivity', this.increaseActivitiesLimitToRetrieve);
    this.$root.$on('activity-stream-activity-deleteActivity', this.broadcastActivityDeleted);
    this.$root.$on('activity-stream-activity-deleteComment', this.broadcastCommentDeleted);
    this.$root.$on('activity-stream-activity-likeActivity', this.handleActivityUnread);
    this.$root.$on('activity-stream-activity-updateActivity', this.handleActivityUnread);
    this.$root.$on('activity-stream-activity-createComment', this.handleActivityUnread);
    this.$root.$on('activity-stream-activity-updateComment', this.handleActivityUnread);
    this.$root.$on('activity-stream-activity-likeComment', this.handleActivityUnread);
    this.$root.$on('activity-stream-activity-pinActivity', this.handleActivityPin);
    document.addEventListener('notification.unread.item', this.handleActivityUnreadEvent);

    this.$root.$on('activity-stream-reset-filter', this.init);
    document.addEventListener('activity-stream-type-filter-applied', this.init);
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
    handleActivityUnreadEvent(event) {
      const activityId = event?.detail?.message?.spacewebnotificationitem?.activityId;
      const spaceId = event?.detail?.message?.spacewebnotificationitem?.spaceId;
      if (this.streamFilter === 'unread_spaces_stream' && activityId && spaceId) {
        this.handleActivityUnread(activityId, spaceId);
      }
    },
    handleActivityUnread(activityId, spaceId) {
      if (this.streamFilter === 'unread_spaces_stream'
          && (!eXo.env.portal.spaceId || Number(eXo.env.portal.spaceId) === Number(spaceId))
          && !this.updatedActivities.has(activityId)
          && !this.activityIds.includes(activityId)) {
        this.checkActivityWithFilter(activityId);
      }
    },
    handleActivityPin(activityId, spaceId) {
      if (this.streamFilter === 'pin_stream'
        && (!eXo.env.portal.spaceId || Number(eXo.env.portal.spaceId) === Number(spaceId))
        && !this.updatedActivities.has(activityId)
        && !this.activityIds.includes(activityId)) {
        this.markActivityIdAsNew(activityId);
      }
    },
    increaseActivitiesLimitToRetrieve(activityId, spaceId) {
      if ((!eXo.env.portal.spaceId || Number(eXo.env.portal.spaceId) === Number(spaceId))
          && !this.updatedActivities.has(activityId)
          && !this.activityIds.includes(activityId)) {
        if (this.streamFilter === 'all_stream') {
          this.markActivityIdAsNew(activityId);
        } else {
          window.setTimeout(() => this.checkActivityWithFilter(activityId), 500);
        }
      }
    },
    checkActivityWithFilter(activityId) {
      return this.$activityService.getActivityById(activityId, this.$activityConstants.FULL_ACTIVITY_EXPAND)
        .then(activity => {
          if (this.streamFilter === 'unread_spaces_stream') {
            if (activity?.metadatas?.unread) {
              this.markActivityIdAsNew(activityId);
            }
          } else if (this.streamFilter === 'user_favorite_stream') {
            if (activity?.metadatas?.favorites?.length) {
              this.markActivityIdAsNew(activityId);
            }
          } else if (this.streamFilter === 'pin_stream') {
            if (activity?.pinned) {
              this.markActivityIdAsNew(activityId);
            }
          } else if (this.streamFilter === 'user_stream') {
            if (eXo.env.portal.userIdentityId === activity?.owner?.id) {
              this.markActivityIdAsNew(activityId);
            }
          } else if (activity?.activityStream?.space) {
            if (this.streamFilter === 'manage_spaces_stream') {
              if (activity.activityStream.space?.isManager) {
                this.markActivityIdAsNew(activityId);
              }
            } else if (this.streamFilter === 'favorite_spaces_stream') {
              if (activity.activityStream.space?.isFavorite === 'true') {
                this.markActivityIdAsNew(activityId);
              }
            }
          }
        });
    },
    markActivityIdAsNew(activityId) {
      this.updatedActivities.add(activityId);
      this.newerActivitiesCount = this.updatedActivities.size;
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
