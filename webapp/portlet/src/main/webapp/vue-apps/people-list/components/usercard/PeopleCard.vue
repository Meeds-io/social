<template>
  <v-flex>
    <people-user-card
      v-if="!compact"
      :user="user"
      :user-navigation-extensions="userNavigationExtensions"
      :space-members-extensions="filteredSpaceMembersExtensions"
      :profile-action-extensions="profileActionExtensions"
      :preferences="userCardSettings" />
    <people-user-compact-card
      v-else
      :is-mobile="isMobile"
      :user="user"
      :user-navigation-extensions="userNavigationExtensions"
      :enabled-profile-action-extensions="enabledProfileActionExtensions"
      :space-members-extensions="filteredSpaceMembersExtensions"
      :url="url"
      :user-avatar-url="userAvatarUrl"
      :is-updating-status="sendingAction || sendingSecondAction" />
  </v-flex>
</template>

<script>
export default {
  props: {
    user: {
      type: Object,
      default: () => ({}),
    },
    userNavigationExtensions: {
      type: Array,
      default: () => [],
    },
    spaceMembersExtensions: {
      type: Array,
      default: () => [],
    },
    profileActionExtensions: {
      type: Array,
      default: () => [],
    },
    compactDisplay: {
      type: Boolean,
      default: false,
    },
    userCardSettings: {
      type: Object,
      default: () => ({}),
    },
  },
  data: () => ({
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
    filteredSpaceMembersExtensions() {
      return this.spaceMembersExtensions.filter(extension => extension.enabled(this.user));
    },
    isMobile() {
      return this.$vuetify.breakpoint.smAndDown;
    },
    compact() {
      return this.isMobile || this.compactDisplay;
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

  }
};
</script>
