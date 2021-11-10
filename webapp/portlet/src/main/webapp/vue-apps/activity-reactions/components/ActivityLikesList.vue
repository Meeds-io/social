<template>
  <div v-if="likes">
    <div
      v-for="(like , i) in likes"
      :key="i">
      <exo-user-avatar
        :username="like.username"
        :fullname="like.fullname"
        :avatar-url="like.avatar"
        :url="profileUrl(like.fullname)"
        bold-title
        size="32"
        class="pl-3 pt-2 pb-2" />
      <v-divider dark />
    </div>
  </div>
</template>
<script>

export default {
  props: {
    activityId: {
      type: String,
      default: () => ''
    }
  },
  data () {
    return {
      cmpId: `react${parseInt(Math.random() * 10000)
        .toString()}`,
      user: null,
      likes: [],
      attributesLoaded: false,
      limit: 10,
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
    numberOfReactions() {
      return this.likes && this.likes.length;
    },
  },
  created() {
    this.retrieveLikers();
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
    retrieveLikers() {
      return this.$activityService.getActivityLikers(this.activityId, 0, this.limit)
        .then(data => {
          this.likes = data.likes;
          document.dispatchEvent(new CustomEvent('updateReaction', {
            detail: {
              numberOfReactions: this.numberOfReactions ,
              type: 'like'
            }
          }));
        })
        .catch((e => {
          console.error('error retrieving activity likers' , e) ;
        }));
    },
    profileUrl(fullName) {
      return fullName && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${fullName}`;
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
