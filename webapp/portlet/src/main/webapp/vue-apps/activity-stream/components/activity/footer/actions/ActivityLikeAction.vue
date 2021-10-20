<template>
  <div class="d-inline-flex ms-lg-4">
    <!-- Added for mobile -->
    <v-tooltip bottom>
      <template v-slot:activator="{ on, attrs }">
        <v-btn
          :id="`LikeLink${activityId}`"
          :loading="changingLike"
          :class="likeTextColorClass"
          :disabled="disable"
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
              size="14">
              fa-thumbs-up
            </v-icon>
            <span class="mx-auto mt-1 mt-lg-0 ms-lg-2">
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
    disable: {
      type: Boolean,
      default: false,
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
          this.$root.$emit('activity-liked', this.activity);
        })
        .finally(() => this.changingLike = false);
    },
    unlikeActivity() {
      this.changingLike = true;
      return this.$activityService.unlikeActivity(this.activityId)
        .then(data => {
          this.activity.hasLiked = 'false';
          this.computeLikes(data);
          this.$root.$emit('activity-liked', this.activity);
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