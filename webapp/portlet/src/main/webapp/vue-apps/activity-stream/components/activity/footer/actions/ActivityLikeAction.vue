<template>
  <div class="d-inline-flex ms-xl-4 ms-lg-3">
    <!-- Added for mobile -->
    <v-tooltip bottom>
      <template #activator="{ on, attrs }">
        <v-btn
          :id="`LikeLink${activityId}`"
          :loading="changingLike"
          :class="likeTextColorClass"
          class="pa-0 mt-0"
          text
          link
          small
          v-bind="attrs"
          v-on="on"
          @click="changeLike">
          <div class="d-flex flex-lg-row flex-column">
            <v-icon
              :class="likeColorClass"
              class="baseline-vertical-align"
              :size="isMobile && '20' || '14'">
              fa-thumbs-up
            </v-icon>
            <span v-if="!isMobile" class="mx-auto mt-1 mt-lg-0 ms-lg-1">
              {{ $t('UIActivity.msg.LikeActivity') }}
            </span>
          </div>
        </v-btn>
      </template>
      <span>
        {{ likeButtonTitle }}
      </span>
    </v-tooltip>
  </div>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    changingLike: false,
    hasLiked: false,
  }),
  computed: {
    activityId() {
      return this.activity && this.activity.id;
    },
    likeColorClass() {
      return this.hasLiked && 'primary--text' || 'disabled--text';
    },
    likeTextColorClass() {
      return this.hasLiked && 'primary--text' || '';
    },
    likeButtonTitle() {
      return this.hasLiked && this.$t('UIActivity.msg.UnlikeActivity') || this.$t('UIActivity.msg.LikeActivity');
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs';
    },
  },
  created() {
    this.computeLikes();
  },
  methods: {
    changeLike() {
      if (this.changingLike) {
        return;
      }
      if (this.hasLiked) {
        return this.unlikeActivity();
      } else {
        return this.likeActivity();
      }
    },
    likeActivity() {
      this.changingLike = true;
      return this.$activityService.likeActivity(this.activityId)
        .then(data => {
          this.activity.hasLiked = 'true';
          this.computeLikes(data);
          document.dispatchEvent(new CustomEvent('activity-liked' , {detail: this.activityId}));
        })
        .finally(() => this.changingLike = false);
    },
    unlikeActivity() {
      this.changingLike = true;
      return this.$activityService.unlikeActivity(this.activityId)
        .then(data => {
          this.activity.hasLiked = 'false';
          this.computeLikes(data);
          document.dispatchEvent(new CustomEvent('activity-liked' , {detail: this.activityId}));
        })
        .finally(() => this.changingLike = false);
    },
    computeLikes(data) {
      if (data) {
        this.$set(this.activity, 'likes', data && data.likes || []);
        this.$set(this.activity, 'likesCount', data && data.size || 0);
      }
      this.hasLiked = this.activity && this.activity.hasLiked === 'true';
    },
  },
};
</script>
