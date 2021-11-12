<template>
  <div v-if="liker" class="activityLikerItem">
    <exo-user-avatar
      :username="userName"
      :fullname="fullName"
      :avatar-url="avatarUrl"
      :url="profileUrl"
      avatar-class="mr-5"
      size="42"
      class="pl-3 pt-2 pb-1"
      bold-title>
      <template slot="subTitle">
        <span v-if="!sameUser">
          {{ inCommonConnections }} {{ $t('UIActivity.label.Reactions_in_Common') }}
        </span>
      </template>
      <template slot="actions" v-if="notConnected">
        <v-btn-toggle class="mr-5">
          <a
            text
            icon
            min-width="auto"
            @click="connect()">
            <i class="uiIconInviteUser"></i>
          </a>
        </v-btn-toggle>
      </template>
    </exo-user-avatar>
    <v-divider />
  </div>
</template>
<script>

export default {
  props: {
    liker: {
      type: Object,
      default: null
    }
  },
  data () {
    return {
      userInformations: null,
    };
  },
  computed: {
    inCommonConnections() {
      return this.userInformations && this.userInformations.connectionsInCommonCount || 0;
    },
    sameUser() {
      return this.userInformations && this.userInformations.username === eXo.env.portal.userName;
    },
    notConnected() {
      return this.userInformations && !this.userInformations.relationshipStatus && !this.sameUser;
    },
    userName() {
      return this.liker && this.liker.username;
    },
    fullName() {
      return this.liker && this.liker.fullname;
    },
    avatarUrl() {
      return this.liker && this.liker.avatar;
    },
    profileUrl() {
      return this.liker && this.userName && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.userName}`;
    },
  },
  created() {
    this.retrieveUserInformations();
  },
  methods: {
    retrieveUserInformations() {
      return this.$userService.getUser(this.liker.username, 'all,connectionsInCommonCount,relationshipStatus')
        .then(item => this.userInformations = item)
        .catch((e) => {
          console.error('Error while getting user details', e);
        });
    },
    connect() {
      this.$userService.connect(this.liker.username)
        .then(this.retrieveUserInformations())
        .catch((e) => {
          console.error('Error while connecting to user', e);
        });
    },
  },
};
</script>
