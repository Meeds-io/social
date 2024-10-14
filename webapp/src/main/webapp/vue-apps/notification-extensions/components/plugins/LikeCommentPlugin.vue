<template>
  <user-notification-activity-base
    :loading="loading"
    :notification="notification"
    :message-text="message"
    :from-identity="fromIdentity"
    message-key="Notification.intranet.message.one.LikeCommentPlugin"
    icon="fa-thumbs-up" />
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
    likerIdentities: [],
    loading: true,
  }),
  computed: {
    fromIdentity() {
      return this.notification?.from
        || (this.likerIdentities?.length && this.likerIdentities[0]);
    },
    likerUsernames() {
      return (this.notification?.parameters?.likers && this.notification.parameters.likers.split(','))
        || (this.notification?.parameters?.likersId && this.notification.parameters.likersId.split(','))
        || [];
    },
    message() {
      if (!this.likerIdentities?.length) {
        return this.$t('Notification.intranet.message.one.LikeCommentPlugin');
      } else if (this.likerUsernames.length < 2) {
        return this.$t('Notification.intranet.message.one.LikeCommentPlugin', {
          0: `<a class="user-name font-weight-bold">${this.likerIdentities[0].fullname}</a>`,
        });
      } else if (this.likerUsernames.length < 3) {
        return this.$t('Notification.intranet.message.two.LikeCommentPlugin', {
          0: `<a class="user-name font-weight-bold">${this.likerIdentities[0].fullname}</a>`,
          1: `<a class="user-name font-weight-bold">${this.likerIdentities[1].fullname}</a>`,
        });
      } else {
        return this.$t('Notification.intranet.message.more.LikeCommentPlugin', {
          0: `<a class="user-name font-weight-bold">${this.likerIdentities[0].fullname}</a>`,
          1: `<a class="user-name font-weight-bold">${this.likerIdentities[1].fullname}</a>`,
          2: `<strong>${this.likerUsernames.length - 2}</strong>`,
        });
      }
    },
  },
  created() {
    if (this.likerUsernames?.length && this.likerUsernames?.length > 1) {
      Promise.all(this.likerUsernames.slice(0, 2).map(u => this.$identityService.getIdentityByProviderIdAndRemoteId('organization', u)))
        .then(identities => this.likerIdentities = identities.map(i => i?.profile).filter(p => !!p))
        .finally(() => this.loading = false);
    } else {
      this.likerIdentities = this.notification.from && [this.notification.from] || [];
      this.loading = false;
    }
  },
};
</script>