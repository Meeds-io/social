<template>
  <user-notification-template
    :actions-class="accepted && 'd-none'"
    :avatar-url="spaceAvatarUrl"
    :message="message"
    :notification="notification"
    space-avatar
    :url="spaceUrl">
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
        @click.stop.prevent="acceptToJoin">
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
        @click.stop.prevent="refuseToJoin">
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
      space () {
        return this.notification?.space;
      },
      spaceId () {
        return this.space?.id;
      },
      spaceUrl () {
        return `${eXo.env.portal.context}/s/${this.spaceId}`;
      },
      spaceAvatarUrl () {
        return this.space?.avatarUrl;
      },
      accepted () {
        return this.notification?.parameters?.status === 'accepted';
      },
      messageKey () {
        return this.accepted && 'Notification.intranet.message.accept.SpaceInvitationPlugin' || 'Notification.intranet.message.SpaceInvitationPlugin';
      },
      message () {
        return this.space && this.$t(this.messageKey, this.accepted && {
          0: `<a class="space-name font-weight-bold">${this.space.displayName}</a>`,
        } || {
          0: `<a class="user-name font-weight-bold">${this.profileFullname}</a>`,
          1: `<a class="space-name font-weight-bold">${this.space.displayName}</a>`,
        }) || '';
      },
    },
    methods: {
      acceptToJoin () {
        this.accepting = true;
        this.$spaceService.accept(this.spaceId)
          .then(() => document.dispatchEvent(new CustomEvent('refresh-notifications')))
          .finally(() => this.accepting = false);
      },
      refuseToJoin () {
        this.refusing = true;
        this.$spaceService.deny(this.spaceId)
          .then(() => document.dispatchEvent(new CustomEvent('refresh-notifications')))
          .finally(() => this.refusing = false);
      },
    },
  };
</script>