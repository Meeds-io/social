<template>
  <user-notification-template
    :notification="notification"
    :avatar-url="profileAvatarUrl"
    :message="message"
    :url="profileUrl"
    :actions-class="accepted && 'd-none'">
    <template v-if="!accepted" #actions>
      <v-btn
        :loading="accepting"
        class="btn success me-2"
        small
        dark
        @click.stop.prevent="acceptUserRequest">
        <v-icon size="14" class="me-2">fa-check</v-icon>
        {{ $t('Notification.label.Accept') }}
      </v-btn>
      <v-btn
        :loading="refusing"
        class="btn error"
        small
        dark
        @click.stop.prevent="refuseUserRequest">
        <v-icon size="14" class="me-2">fa-times</v-icon>
        {{ $t('Notification.label.Refuse') }}
      </v-btn>
    </template>
  </user-notification-template>
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
    accepting: false,
    refusing: false,
  }),
  computed: {
    profile() {
      return this.notification?.from;
    },
    profileFullname() {
      return this.profile?.fullname || this.notification?.parameters?.sender;
    },
    username() {
      return this.profile?.username;
    },
    profileUrl() {
      return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.username}`;
    },
    space() {
      return this.notification?.space;
    },
    spaceId() {
      return this.space?.id;
    },
    spaceDisplatName() {
      return this.space?.displayName;
    },
    spaceGroupId() {
      return this.space?.groupId?.replace(/\//g, ':');
    },
    profileAvatarUrl() {
      return this.profile?.avatar;
    },
    accepted() {
      return this.notification?.parameters?.status === 'accepted';
    },
    messageKey() {
      return this.accepted && 'Notification.intranet.message.accept.RequestJoinSpacePlugin' || 'Notification.intranet.message.RequestJoinSpacePlugin';
    },
    message() {
      return this.space && this.profile && this.$t(this.messageKey, {
        0: `<a class="user-name font-weight-bold">${this.profileFullname}</a>`,
        1: `<a class="space-name font-weight-bold">${this.spaceDisplatName}</a>`,
      }) || '';
    },
  },
  methods: {
    acceptUserRequest() {
      this.accepting = true;
      this.$spaceService.acceptUserRequest(this.spaceDisplatName, this.username)
        .then(() => document.dispatchEvent(new CustomEvent('refresh-notifications')))
        .finally(() => this.accepting = false);
    },
    refuseUserRequest() {
      this.refusing = true;
      this.$spaceService.refuseUserRequest(this.spaceDisplatName, this.username)
        .then(() => document.dispatchEvent(new CustomEvent('refresh-notifications')))
        .finally(() => this.refusing = false);
    },
  },
};
</script>