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
        color="primary"
        class="primary-border-color d-flex"
        elevation="0"
        small
        outlined
        @click="unwatch">
        <v-icon size="14" class="me-1">fa-eye-slash</v-icon>
        <span class="text-none">
          {{ $t('UIActivity.label.Unwatch') }}
        </span>
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