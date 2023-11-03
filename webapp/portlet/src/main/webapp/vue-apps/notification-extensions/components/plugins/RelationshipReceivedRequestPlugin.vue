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
        @click.stop.prevent="acceptToConnect">
        <v-icon size="14" class="me-2">fa-check</v-icon>
        {{ $t('Notification.label.Accept') }}
      </v-btn>
      <v-btn
        :loading="refusing"
        class="btn error"
        small
        dark
        @click.stop.prevent="refuseToConnect">
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
    profileUrl() {
      return `${eXo.env.portal.context}/${eXo.env.portal.defaultPortal}/profile/${this.username}`;
    },
    username() {
      return this.profile?.username;
    },
    profileAvatarUrl() {
      return this.profile?.avatar;
    },
    accepted() {
      return this.notification?.parameters?.status === 'accepted';
    },
    messageKey() {
      return this.accepted && 'Notification.intranet.message.accept.RelationshipReceivedRequestPlugin' || 'Notification.intranet.message.RelationshipReceivedRequestPlugin';
    },
    message() {
      return this.profile && this.$t(this.messageKey, {
        0: `<a class="user-name font-weight-bold">${this.profile.fullname}</a>`,
      }) || '';
    },
  },
  methods: {
    acceptToConnect() {
      this.accepting = true;
      this.$userService.confirm(this.username)
        .then(() => document.dispatchEvent(new CustomEvent('refresh-notifications')))
        .finally(() => this.accepting = false);
    },
    refuseToConnect() {
      this.refusing = true;
      this.$userService.deleteRelationship(this.username)
        .then(() => document.dispatchEvent(new CustomEvent('refresh-notifications')))
        .finally(() => this.refusing = false);
    },
  },
};
</script>