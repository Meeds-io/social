<template>
  <v-list-item class="likerItem">
    <v-list-item-avatar :size="avatarSize">
      <v-img :src="avatar" class="likerAvatar" />
    </v-list-item-avatar>
    <v-list-item-content class="pb-3">
      <v-list-item-title class="body-2 font-weight-bold text-color">
        <a
          :id="cmpId"
          :href="profileUrl"
          rel="nofollow"
          class="text-color"
          v-html="name">
        </a>
      </v-list-item-title>
      <v-list-item-subtitle v-if="attributesLoaded && !sameUser" class="caption text-bold">
        {{ inCommonConnections }} {{ $t('UIActivity.label.Reactions_in_Common') }}
      </v-list-item-subtitle>
    </v-list-item-content>
    <v-list-item-action v-if="notConnected">
      <v-btn-toggle class="transparent">
        <a
          text
          icon
          min-width="auto"
          @click="connect()">
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
  },
  data () {
    return {
      cmpId: `react${parseInt(Math.random() * 10000)
        .toString()}`,
      user: null,
      attributesLoaded: false
    };
  },
  computed: {
    inCommonConnections() {
      return this.user && this.user.connectionsInCommonCount || 0;
    },
    sameUser() {
      return this.user && this.user.username === eXo.env.portal.userName;
    },
    notConnected() {
      return this.user && !this.user.relationshipStatus && !this.sameUser;
    },
    profileUrl() {
      return this.user && `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/${this.user.username}`;
    },
  },
  created() {
    this.retrieveUserInformations();
  },
  methods: {
    retrieveUserInformations() {
      return this.$userService.getUser(this.userId, 'all,connectionsInCommonCount,relationshipStatus')
        .then(item => this.user = item)
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error while getting user details', e);
        })
        .finally(() => this.attributesLoaded = true);
    },
    connect() {
      this.$userService.connect(this.userId)
        .then(this.retrieveUserInformations())
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        });
    },
  },
};
</script>
