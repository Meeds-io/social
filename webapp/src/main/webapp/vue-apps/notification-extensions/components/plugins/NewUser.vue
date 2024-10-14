<template>
  <user-notification-template
    :notification="notification"
    :avatar-url="profileAvatarUrl"
    :message="message"
    :url="profileUrl" />
</template>
<script>
export default {
  props: {
    notification: {
      type: Object,
      default: null,
    },
  },
  computed: {
    profile() {
      return this.notification?.from;
    },
    profileUrl() {
      return `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/${this.username}`;
    },
    username() {
      return this.profile?.username;
    },
    profileAvatarUrl() {
      return this.profile?.avatar;
    },
    message() {
      return this.profile && this.$t('Notification.intranet.message.NewUserPlugin', {
        0: `<a class="user-name font-weight-bold">${this.profile.fullname}</a>`,
        1: eXo.env.portal.companyName,
      }) || '';
    },
  },
};
</script>