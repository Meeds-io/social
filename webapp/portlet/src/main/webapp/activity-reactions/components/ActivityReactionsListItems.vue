<template>
  <v-list-item class="likerItem">
    <v-list-item-avatar :size="avatarSize">
      <v-img :src="avatar" class="likerAvatar"></v-img>
    </v-list-item-avatar>
    <v-list-item-content class="pb-3">
      <v-list-item-title class="body-2 font-weight-bold text-color">
        <a
          :href="profileUrl"
          class="text-color">
          {{ name }}
        </a>
      </v-list-item-title>
      <v-list-item-subtitle class="caption text-bold">
        {{ getUserInformations(userId).InCommonconnections }} {{ $t('UIActivity.label.Reactions_in_Common') }}
      </v-list-item-subtitle>
    </v-list-item-content>
    <v-list-item-action v-if="user.isMyConnexion">
      <v-btn-toggle class="transparent">
        <a :class="isInvited ? 'hideInvitationButton' : ''"
           text
           icon
           small
           min-width="auto"
           @click="connect(userId)">
          <i class="uiIconInviteUser"></i>
        </a>
      </v-btn-toggle>
    </v-list-item-action>
  </v-list-item>
</template>
<script>
export default {
  props: {
    userId: {
      type: String,
      default: ''
    },
    avatarSize: {
      type: Number,
      default: () => 34,
    },
    avatar: {
      type: String,
      default: () => '',
    },
    name: {
      type: String,
      default: () => ''
    },
    profileUrl: {
      type: String,
      default: ''
    }
  },
  data () {
    return {
      user: {
        InCommonconnections: 0,
        isMyConnexion: false
      },
      isInvited: false
    };
  },
  methods: {
    getUserInformations(userId) {
      this.$userService.getUser(userId, 'all,connectionsInCommonCount,relationshipStatus')
        .then(item => {
          this.user.InCommonconnections = item.connectionsInCommonCount;
          this.user.isMyConnexion = !item.relationshipStatus ? true : false;
        })
        .catch((e) => {
          console.error('Error while getting user details', e);
        });
      return this.user;
    },
    connect(userId) {
      this.isInvited = true;
      this.$userService.connect(userId)
        .then(this.getUserInformations(userId))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.isInvited = false;
        });
    },
  },
};
</script>