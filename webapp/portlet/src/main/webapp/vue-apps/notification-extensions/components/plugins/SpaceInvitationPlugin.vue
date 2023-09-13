<template>
  <user-notification-template
    :notification="notification"
    :avatar-url="spaceAvatarUrl"
    :message="message"
    :url="spaceUrl"
    :actions-class="accepted && 'd-none'"
    space-avatar>
    <template v-if="!accepted" #actions>
      <v-btn
        :loading="accepting"
        class="btn success me-2"
        small
        dark
        @click.stop.prevent="acceptToJoin">
        <v-icon size="14" class="me-2">fa-check</v-icon>
        {{ $t('Notification.label.Accept') }}
      </v-btn>
      <v-btn
        :loading="refusing"
        class="btn error"
        small
        dark
        @click.stop.prevent="refuseToJoin">
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
    space() {
      return this.notification?.space;
    },
    spaceId() {
      return this.space?.id;
    },
    spaceGroupId() {
      return this.space?.groupId?.replace(/\//g, ':');
    },
    spaceUrl() {
      return `${eXo.env.portal.context}/g/${this.spaceGroupId}`;
    },
    spaceAvatarUrl() {
      return this.space?.avatarUrl;
    },
    accepted() {
      return this.notification?.parameters?.status === 'accepted';
    },
    messageKey() {
      return this.accepted && 'Notification.intranet.message.accept.SpaceInvitationPlugin' || 'Notification.intranet.message.SpaceInvitationPlugin';
    },
    message() {
      return this.space && this.$t(this.messageKey, this.accepted && {
        0: `<a class="space-name font-weight-bold">${this.space.displayName}</a>`,
      } || {
        0: `<a class="user-name font-weight-bold">${this.profileFullname}</a>`,
        1: `<a class="space-name font-weight-bold">${this.space.displayName}</a>`,
      }) || '';
    },
  },
  methods: {
    acceptToJoin() {
      this.accepting = true;
      this.$spaceService.accept(this.spaceId)
        .then(() => document.dispatchEvent(new CustomEvent('refresh-notifications')))
        .finally(() => this.accepting = false);
    },
    refuseToJoin() {
      this.refusing = true;
      this.$spaceService.deny(this.spaceId)
        .then(() => document.dispatchEvent(new CustomEvent('refresh-notifications')))
        .finally(() => this.refusing = false);
    },
  },
};
</script>