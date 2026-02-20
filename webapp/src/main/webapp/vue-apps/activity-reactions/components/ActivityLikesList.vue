<template>
  <div v-if="likers.length" class="likers-list">
    <activity-liker-item
      v-for="liker in likers"
      :key="liker.id"
      :liker="liker" />
    <v-btn
      v-if="hasMoreLikers"
      :loading="loading"
      :disabled="loading"
      block
      class="btn pa-0 mt-2"
      @click="loadMore">
      {{ $t('Search.button.loadMore') }}
    </v-btn>
  </div>
</template>
<script>
export default {
  props: {
    activityId: {
      type: String,
      default: () => ''
    },
    parentId: {
      type: String,
      default: () => ''
    }
  },
  data () {
    return {
      likers: [],
      limit: 20,
      likersSize: 0,
      loading: false
    };
  },
  computed: {
    hasMoreLikers() {
      return this.likersSize > this.limit;
    }
  },
  created() {
    this.$root.$on('activity-liked', this.handleActivityLikesUpdate);
    this.retrieveLikers();
    document.addEventListener('check-reactions', event => {
      if (event && event.detail && event.detail === this.activityId) {
        this.updateLikers();
      }
    });
  },
  beforeDestroy() {
    this.$root.$off('activity-liked', this.handleActivityLikesUpdate);
  },
  watch: {
    activityId() {
      this.retrieveLikers();
    }
  },
  methods: {
    handleActivityLikesUpdate(activityId) {
      if (activityId === this.activityId) {
        this.retrieveLikers();
      }
    },
    retrieveLikers() {
      this.loading = true;
      return this.$activityService.getActivityLikers(this.activityId, 0, this.limit)
        .then(data => {
          this.likers = data.likes;
          this.likersSize = data.size;
          this.updateLikers();
        })
        .catch((e => {
          console.error('error retrieving activity likers' , e) ;
        })).finally(() => this.loading = false);
    },
    updateLikers() {
      document.dispatchEvent(new CustomEvent('update-reaction-extension', {
        detail: {
          numberOfReactions: this.likers.length,
          type: 'like'
        }
      }));
    },
    loadMore() {
      this.limit = this.limit + 20;
      this.retrieveLikers();
    }
  },
};
</script>
