<template>
  <div v-if="liker" class="activityLikerItem">
    <exo-user-avatar
      :identity="liker"
      avatar-class="me-2"
      size="42"
      extra-class="pl-3 pt-2 pb-1"
      popover
      bold-title>
      <template slot="subTitle">
        <span v-if="!sameUser" class="caption text-bold">
          {{ inCommonConnections }} {{ $t('UIActivity.label.Reactions_in_Common') }}
        </span>
      </template>
    </exo-user-avatar>
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
