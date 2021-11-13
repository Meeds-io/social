<template>
  <div v-if="likers.length" class="likers-list">
    <activity-liker-item
      v-for="(liker , i) in likers"
      :key="i"
      :liker="liker" />
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
  created() {
    this.retrieveLikers();
  },
  methods: {
    retrieveLikers() {
      return this.$activityService.getActivityLikers(this.activityId, 0)
        .then(data => {
          this.likers = data.likes;
          document.dispatchEvent(new CustomEvent('update-reaction-extension', {
            detail: {
              numberOfReactions: this.likers.length,
              type: 'like'
            }
          }));
        })
        .catch((e => {
          console.error('error retrieving activity likers' , e) ;
        }));
    },
  },
};
</script>
