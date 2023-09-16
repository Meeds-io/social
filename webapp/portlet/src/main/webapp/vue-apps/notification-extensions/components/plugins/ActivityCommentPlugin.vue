<template>
  <user-notification-activity-base
    :loading="loading"
    :notification="notification"
    :reply="!isWatchedNotification"
    message-key="Notification.intranet.message.one.ActivityCommentPlugin"
    icon="fa-comment">
    <template v-if="isWatchedNotification" #reply>
      <v-btn
        v-if="!watchCanceled"
        :loading="unwatching"
        class="btn primary px-2"
        outlined
        small
        @click="unwatch">
        <v-hover v-slot="{hover}">
          <div>
            <v-icon size="14" class="me-1">{{ hover && 'fa-eye-slash' || 'fa-eye' }}</v-icon>
            <span class="text-none">
              {{ hover && $t('Notification.activity.stopWatching') || $t('Notification.activity.watching') }}
            </span>
          </div>
        </v-hover>
      </v-btn>
    </template>
  </user-notification-activity-base>
</template>
<script>
export default {
  props: {
    notification: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    loading: true,
    unwatching: false,
    watchCanceled: false,
  }),
  computed: {
    isWatchedNotification() {
      return this.notification?.parameters?.watched === 'true';
    },
    activityId() {
      return this.notification?.parameters?.activityId;
    },
  },
  created() {
    if (this.isWatchedNotification) {
      this.$observerService.isObserved('activity', this.activityId)
        .then(watched => this.watchCanceled = !watched)
        .finally(() => this.loading = false);
      this.$root.$on('activity-notification-unwatch', this.markAsUnwatched);
    } else {
      this.loading = false;
    }
  },
  methods: {
    unwatch() {
      this.unwatching = true;
      this.$observerService.deleteObserver('activity', this.activityId)
        .then(() => this.$root.$emit('activity-notification-unwatch'))
        .then(() => document.dispatchEvent(new CustomEvent('activity-updated', {detail: this.activityId})))
        .finally(() => this.unwatching = false);
    },
    markAsUnwatched() {
      this.watchCanceled = true;
    },
  },
};
</script>