<template>
  <v-flex>
    <people-user-card
      v-if="!compact"
      :profile-action-extensions="profileActionExtensions"
      :space-id="spaceId"
      :space-members-extensions="spaceMembersExtensions"
      :user="user"
      :user-navigation-extensions="userNavigationExtensions" />
    <people-user-compact-card
      v-else
      :is-updating-status="sendingAction || sendingSecondAction"
      :mobile-display="mobileDisplay"
      :profile-action-extensions="profileActionExtensions"
      :space-id="spaceId"
      :space-members-extensions="spaceMembersExtensions"
      :url="url"
      :user="user"
      :user-avatar-url="userAvatarUrl"
      :user-navigation-extensions="userNavigationExtensions" />
  </v-flex>
</template>
<script>
  export default {
    props: {
      user: {
        type: Object,
        default: () => ({}),
      },
      spaceId: {
        type: String,
        default: null,
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
      mobileDisplay: {
        type: Boolean,
        default: false,
      },
    },
    data: () => ({
      sendingAction: false,
      sendingSecondAction: false,
    }),
    computed: {
      compact () {
        return this.mobileDisplay || this.compactDisplay;
      },
      userAvatarUrl () {
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
      url () {
        if (this.user?.username) {
          return `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/${this.user.username}`;
        } else {
          return '#';
        }
      },
    },
  };
</script>
