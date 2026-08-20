<template>
  <user-notification-template
    :notification="notification"
    :avatar-url="profileAvatarUrl"
    :message="message"
    :url="usersManagementUrl" />
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
    usersManagementUrl: `${eXo.env.portal.context}/administration/home/organisation/users?status=DISABLED`,
  }),
  computed: {
    profile() {
      return this.notification?.from;
    },
    profileAvatarUrl() {
      return this.profile?.avatar;
    },
    message() {
      return this.profile && this.$t('Notification.intranet.message.AccountDeactivationRequestPlugin', {
        0: `<span class="user-name font-weight-bold">${this.profile.fullname}</span>`,
      }) || '';
    },
  },
};
</script>
