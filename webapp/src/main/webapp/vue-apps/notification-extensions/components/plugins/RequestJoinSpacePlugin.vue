<template>
  <user-notification-template
    :actions-class="accepted && 'd-none'"
    :avatar-url="profileAvatarUrl"
    :message="message"
    :notification="notification"
    :url="profileUrl">
    <template
      v-if="!accepted"
      #actions>
      <v-btn
        class="ignore-vuetify-classes me-2"
        color="success"
        elevation="0"
        :loading="accepting"
        outlined
        small
        @click.stop.prevent="acceptUserRequest">
        <v-icon
          class="me-2 pt-2px"
          size="14">
          fa-check
        </v-icon>
        <span class="text-none">{{ $t('Notification.label.Accept') }}</span>
      </v-btn>
      <v-btn
        class="ignore-vuetify-classes"
        color="error"
        dark
        elevation="0"
        :loading="refusing"
        outlined
        small
        @click.stop.prevent="refuseUserRequest">
        <v-icon
          class="me-2 pt-2px"
          size="14">
          fa-times
        </v-icon>
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
      profile () {
        return this.notification?.from;
      },
      profileFullname () {
        return this.profile?.fullname || this.notification?.parameters?.sender;
      },
      username () {
        return this.profile?.username;
      },
      profileUrl () {
        return `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/${this.username}`;
      },
      space () {
        return this.notification?.space;
      },
      spaceId () {
        return this.space?.id;
      },
      spaceDisplatName () {
        return this.space?.displayName;
      },
      spaceGroupId () {
        return this.space?.groupId?.replace(/\//g, ':');
      },
      profileAvatarUrl () {
        return this.profile?.avatar;
      },
      accepted () {
        return this.notification?.parameters?.status === 'accepted';
      },
      messageKey () {
        return this.accepted && 'Notification.intranet.message.accept.RequestJoinSpacePlugin' || 'Notification.intranet.message.RequestJoinSpacePlugin';
      },
      message () {
        return this.space && this.profile && this.$t(this.messageKey, {
          0: `<a class="user-name font-weight-bold">${this.profileFullname}</a>`,
          1: `<a class="space-name font-weight-bold">${this.spaceDisplatName}</a>`,
        }) || '';
      },
    },
    methods: {
      acceptUserRequest () {
        this.accepting = true;
        eXo.$spaceService.acceptUserRequest(this.spaceId, this.username)
          .then(() => document.dispatchEvent(new CustomEvent('refresh-notifications')))
          .finally(() => this.accepting = false);
      },
      refuseUserRequest () {
        this.refusing = true;
        eXo.$spaceService.refuseUserRequest(this.spaceId, this.username)
          .then(() => document.dispatchEvent(new CustomEvent('refresh-notifications')))
          .finally(() => this.refusing = false);
      },
    },
  };
</script>