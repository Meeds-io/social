<template>
  <user-notification-activity-base
    :loading="loading"
    :notification="notification"
    :message-text="message"
    :from-identity="fromIdentity"
    icon="fa-comment"
    reply />
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
    message() {
      if (!this.posterIdentities?.length) {
        return 'Notification.intranet.message.one.ActivityReplyToCommentPlugin';
      } else if (this.posterUsernames.length < 2) {
        return this.$t('Notification.intranet.message.one.ActivityReplyToCommentPlugin', {
          0: `<a class="user-name font-weight-bold">${this.posterIdentities[0].fullname}</a>`,
        });
      } else if (this.posterUsernames.length < 3) {
        return this.$t('Notification.intranet.message.two.ActivityReplyToCommentPlugin', {
          0: `<a class="user-name font-weight-bold">${this.posterIdentities[0].fullname}</a>`,
          1: `<a class="user-name font-weight-bold">${this.posterIdentities[1].fullname}</a>`,
        });
      } else {
        return this.$t('Notification.intranet.message.more.ActivityReplyToCommentPlugin', {
          0: `<a class="user-name font-weight-bold">${this.posterIdentities[0].fullname}</a>`,
          1: `<a class="user-name font-weight-bold">${this.posterIdentities[1].fullname}</a>`,
          2: `<strong>${this.posterUsernames.length - 2}</strong>`,
        });
      }
    },
  },
  created() {
    this.loadIdentities().finally(() => this.loading = false);
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
  },
};
</script>