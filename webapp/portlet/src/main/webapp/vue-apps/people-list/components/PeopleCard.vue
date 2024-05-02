<template>
  <v-flex :class="flipCard && 'peopleCardFlip peopleCardFlipped' || 'peopleCardFlip'">
    <div class="peopleCardFront">
      <people-card-front
        v-if="!compactDisplay && !isMobile"
        :user="user"
        :enabled-profile-action-extensions="enabledProfileActionExtensions"
        :is-sending-action="sendingAction"
        :is-sending-second-action="sendingSecondAction"
        :relationship-status="relationshipStatus"
        :url="url"
        :user-avatar-url="userAvatarUrl"
        front
        @flip="flipCard = true"
        @connect="connect"
        @disconnect="disconnect"
        @accept-to-connect="acceptToConnect"
        @refuse-to-connect="refuseToConnect"
        @cancel-request="cancelRequest" />
      <people-card-front-compact
        v-else
        :is-mobile="isMobile"
        :user="user"
        :enabled-profile-action-extensions="enabledProfileActionExtensions"
        :relationship-status="relationshipStatus"
        :url="url"
        :user-avatar-url="userAvatarUrl"
        :is-updating-status="sendingAction || sendingSecondAction"
        @connect="connect"
        @disconnect="disconnect"
        @accept-to-connect="acceptToConnect"
        @refuse-to-connect="refuseToConnect"
        @cancel-request="cancelRequest" />
    </div>
    <div class="peopleCardBack">
      <people-card-reverse
        :user="user"
        @flip="flipCard = false" />
    </div>
  </v-flex>
</template>

<script>
export default {
  props: {
    user: {
      type: Object,
      default: () => ({}),
    },
    profileActionExtensions: {
      type: Array,
      default: () => [],
    },
    compactDisplay: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    flipCard: false,
    sendingAction: false,
    sendingSecondAction: false,
    relationshipStatus: null,
  }),
  watch: {
    relationshipStatus() {
      this.$root.$emit('relationship-status-updated', this.user, this.relationshipStatus);
    }
  },
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.smAndDown;
    },
    userAvatarUrl() {
      let userAvatarUrl;
      if (this.user?.enabled) {
        userAvatarUrl = this.user.avatar || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${this.user.username}/avatar`;
      } else {
        userAvatarUrl = `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/default-image/avatar`;
      }
      if (!userAvatarUrl.includes('?')) {
        userAvatarUrl += '?';
      } else {
        userAvatarUrl += '&';
      }
      userAvatarUrl += 'size=65x65';
      return userAvatarUrl;
    },
    url() {
      if (this.user?.username) {
        return `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/${this.user.username}`;
      } else {
        return '#';
      }
    },
    enabledProfileActionExtensions() {
      if (!this.profileActionExtensions || !this.user) {
        return [];
      }
      if (this.isSameUser && this.user.isManager) {
        return this.profileActionExtensions.slice().filter(extension => ((extension.title === this.$t('peopleList.button.removeManager'))
            || (extension.title === this.$t('peopleList.button.setAsRedactor') || extension.title === this.$t('peopleList.button.removeRedactor') || extension.title === this.$t('peopleList.button.promotePublisher') || extension.title === this.$t('peopleList.button.removePublisher')) && (extension.enabled(this.user))));
      }
      return this.profileActionExtensions.slice().filter(extension => extension.enabled(this.user));
    },
  },
  created() {
    this.relationshipStatus = this.user.relationshipStatus;
  },
  methods: {
    getRelationshipStatus(relationship) {
      if (relationship.status === 'PENDING'
          && relationship?.sender?.username === eXo?.env?.portal?.userName) {
        return 'OUTGOING';
      } else if (relationship.status === 'PENDING') {
        return 'INCOMING';
      } else {
        return relationship.status;
      }
    },
    connect() {
      this.sendingAction = true;
      this.$userService.connect(this.user.username).then((relationship) => {
        this.relationshipStatus = this.getRelationshipStatus(relationship);
      }).finally(() => {
        this.sendingAction = false;
      });
    },
    disconnect() {
      this.sendingAction = true;
      this.$userService.deleteRelationship(this.user.username).then(() => {
        this.relationshipStatus = null;
      }).finally(() => {
        this.sendingAction = false;
      });
    },
    acceptToConnect() {
      this.sendingAction = true;
      this.$userService.confirm(this.user.username).then((relationship) => {
        this.relationshipStatus = this.getRelationshipStatus(relationship);
      }).finally(() => {
        this.sendingAction = false;
      });
    },
    refuseToConnect() {
      this.sendingSecondAction = true;
      this.$userService.deleteRelationship(this.user.username).then(() => {
        this.relationshipStatus = null;
      }).finally(() => {
        this.sendingSecondAction = false;
      });
    },
    cancelRequest() {
      this.sendingAction = true;
      this.$userService.deleteRelationship(this.user.username)
        .then(() => {
          this.relationshipStatus = null;
        }).finally(() => {
          this.sendingAction = false;
        });
    },
  }
};
</script>
