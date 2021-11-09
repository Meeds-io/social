<template>
  <div v-if="likes">
    <v-list-item
      v-for="(like ,i) in likes"
      :key="i"
      class="likerItem">
      <v-list-item-avatar
        v-if="like"
        :size="15">
        <v-img :src="like.avatar" class="likerAvatar" />
      </v-list-item-avatar>
      <v-list-item-content class="pb-3">
        <v-list-item-title class="body-2 font-weight-bold text-color">
          <a
            :id="cmpId"
            :href="like.href"
            rel="nofollow"
            class="text-color"
            v-html="like.fullname">
          </a>
        </v-list-item-title>
      </v-list-item-content>
    </v-list-item>
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
    profileUrl() {
      return this.user && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.user.username}`;
    },
    numberOfLikes() {
      return this.likes && this.likes.length ;
    }

  },
  created() {
    this.retrieveLikers();
    //this.retrieveUserInformations();
    this.initTiptip();
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
          this.likes = data;
        })
        .catch((e => {
          console.error('error retrieving activity likers' , e) ;
        }));
    },
    connect() {
      this.$userService.connect(this.userId)
        .then(this.retrieveUserInformations())
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        });
    },
    initTiptip() {
      this.$nextTick(() => {
        $(`#${this.cmpId}`).userPopup({
          restURL: '/portal/rest/social/people/getPeopleInfo/{0}.json',
          userId: this.id,
          content: false,
          keepAlive: true,
          defaultPosition: 'top_left',
          maxWidth: '240px',
        });
      });
    },
  },
};
</script>
