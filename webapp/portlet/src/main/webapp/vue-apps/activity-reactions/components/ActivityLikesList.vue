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
    document.addEventListener(`check-reactions-${this.activityId}`, this.updateLikers);
    document.addEventListener(`activity-liked-${this.activityId}`,this.retrieveLikers);

  },
  watch: {
    activityId() {
      this.retrieveLikers();
    }
  },
  methods: {
    retrieveLikers() {
      return this.$activityService.getActivityLikers(this.activityId, 0)
        .then(data => {
          this.likers = data.likes;
          this.updateLikers();
        })
        .catch((e => {
          console.error('error retrieving activity likers' , e) ;
        }));
    },
    updateLikers() {
      document.dispatchEvent(new CustomEvent(`update-reaction-extension-${this.activityId}`, {
        detail: {
          numberOfReactions: this.likers.length,
          type: 'like'
        }
      }));
    }
  },
};
</script>
