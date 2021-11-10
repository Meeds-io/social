<template>
  <div v-if="likers">
    <div
      v-for="(liker , i) in likers"
      :key="i">
      <activity-liker-item
        :liker="liker" />
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
      likers: [],
      limit: 10,
    };
  },
  computed: {
    numberOfReactions() {
      return this.likers && this.likers.length;
    },
  },
  created() {
    this.retrieveLikers();
  },
  methods: {
    retrieveLikers() {
      return this.$activityService.getActivityLikers(this.activityId, 0, this.limit)
        .then(data => {
          this.likers = data.likes;
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
