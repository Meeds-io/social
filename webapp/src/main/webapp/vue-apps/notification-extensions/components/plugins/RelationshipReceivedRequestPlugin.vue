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
        class="ignore-vuetify-classes me-2"
        color="success"
        elevation="0"
        small
        outlined
        @click.stop.prevent="acceptToConnect">
        <v-icon size="14" class="me-2 pt-2px">fa-check</v-icon>
        <span class="text-none">{{ $t('Notification.label.Accept') }}</span>
      </v-btn>
      <v-btn
        :loading="refusing"
        class="ignore-vuetify-classes"
        color="error"
        elevation="0"
        small
        outlined
        dark
        @click.stop.prevent="refuseToConnect">
        <v-icon size="14" class="me-2 pt-2px">fa-times</v-icon>
        <span class="text-none">{{ $t('Notification.label.Refuse') }}</span>
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
      return `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/${this.username}`;
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