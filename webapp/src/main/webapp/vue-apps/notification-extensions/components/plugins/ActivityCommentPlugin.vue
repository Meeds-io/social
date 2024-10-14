<template>
  <user-notification-activity-base
    :loading="loading"
    :notification="notification"
    :message-text="message"
    :from-identity="fromIdentity"
    :reply="!isWatchedNotification"
    icon="fa-comment">
    <template v-if="isWatchedNotification" #reply>
      <v-btn
        v-if="!watchCanceled"
        :loading="unwatching"
        class="primary-border-color px-2 position-relative z-index-one"
        color="primary"
        elevation="0"
        small
        outlined
        @click.stop.prevent="unwatch">
        <v-hover v-slot="{hover}">
          <div>
            <v-icon size="14" class="mt-n1 pt-2px">{{ hover && 'fa-eye-slash' || 'fa-eye' }}</v-icon>
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
    posterIdentities: [],
  }),
  computed: {
    fromIdentity() {
      return this.notification?.from
        || (this.posterIdentities?.length && this.posterIdentities[0]);
    },
    posterUsernames() {
      return this.notification?.parameters?.poster
        && this.notification.parameters.poster.split(',')
        || [this.notification.parameters.poster];
    },
    isWatchedNotification() {
      return this.notification?.parameters?.watched === 'true';
    },
    activityId() {
      return this.notification?.parameters?.activityId;
    },
    message() {
      if (!this.posterIdentities?.length) {
        return 'Notification.intranet.message.one.ActivityCommentPlugin';
      } else if (this.posterUsernames.length < 2) {
        return this.$t('Notification.intranet.message.one.ActivityCommentPlugin', {
          0: `<a class="user-name font-weight-bold">${this.posterIdentities[0].fullname}</a>`,
        });
      } else if (this.posterUsernames.length < 3) {
        return this.$t('Notification.intranet.message.two.ActivityCommentPlugin', {
          0: `<a class="user-name font-weight-bold">${this.posterIdentities[0].fullname}</a>`,
          1: `<a class="user-name font-weight-bold">${this.posterIdentities[1].fullname}</a>`,
        });
      } else {
        return this.$t('Notification.intranet.message.more.ActivityCommentPlugin', {
          0: `<a class="user-name font-weight-bold">${this.posterIdentities[0].fullname}</a>`,
          1: `<a class="user-name font-weight-bold">${this.posterIdentities[1].fullname}</a>`,
          2: `<strong>${this.posterUsernames.length - 2}</strong>`,
        });
      }
    },
  },
  created() {
    if (this.isWatchedNotification) {
      Promise.all([
        this.loadIdentities(),
        this.checkObserved()
      ]).finally(() => this.loading = false);
      this.$root.$on('activity-notification-unwatch', this.markAsUnwatched);
    } else {
      this.loadIdentities().finally(() => this.loading = false);
    }
  },
  beforeDestroy() {
    this.$root.$off('activity-notification-unwatch', this.markAsUnwatched);
  },
  methods: {
    loadIdentities() {
      if (this.posterUsernames?.length && this.posterUsernames?.length > 1) {
        return Promise.all(this.posterUsernames.slice(0, 2).map(u => this.$identityService.getIdentityByProviderIdAndRemoteId('organization', u)))
          .then(identities => this.posterIdentities = identities.map(i => i?.profile).filter(p => !!p));
      } else {
        this.posterIdentities = this.notification.from && [this.notification.from] || [];
        return Promise.resolve();
      }
    },
    checkObserved() {
      return this.$observerService.isObserved('activity', this.activityId)
        .then(watched => this.watchCanceled = !watched);
    },
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