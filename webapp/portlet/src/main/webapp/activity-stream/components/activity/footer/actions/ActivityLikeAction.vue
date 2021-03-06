<template>
  <v-tooltip bottom>
    <template v-slot:activator="{ on, attrs }">
      <v-btn
        :id="`LikeLink${activityId}`"
        :loading="changingLike"
        :class="likeTextColorClass"
        class="pa-0 ms-4"
        text
        link
        small
        v-bind="attrs"
        v-on="on"
        @click="changeLike">
        <span>
          <v-icon
            :class="likeColorClass"
            class="baseline-vertical-align"
            size="14">
            fa-thumbs-up
          </v-icon>
          {{ $t('UIActivity.msg.LikeActivity') }}
        </span>
      </v-btn>
    </template>
    <span>
      {{ likeButtonTitle }}
    </span>
  </v-tooltip>
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
  }),
  computed: {
    activityId() {
      return this.activity && this.activity.id;
    },
    likers() {
      return this.activity && this.activity.likes && this.activity.likes.slice().reverse() || [];
    },
    hasLiked() {
      return this.likers.filter(like => like && like.id === eXo.env.portal.userIdentityId).length;
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
        .then(() => {
          const liker = Object.assign({}, this.$currentUserIdentity, this.$currentUserIdentity.profile);
          this.activity.likes = [...this.likers, liker];
          this.activity.likesCount++;
          this.$root.$emit('activity-liked', this.activity);
        })
        .finally(() => this.changingLike = false);
    },
    unlikeActivity() {
      this.changingLike = true;
      return this.$activityService.unlikeActivity(this.activityId)
        .then(() => {
          this.activity.likes = this.likers.filter(likeIdentity => likeIdentity.id !== eXo.env.portal.userIdentityId);
          this.activity.likesCount--;
          this.$root.$emit('activity-liked', this.activity);
        })
        .finally(() => this.changingLike = false);
    },
  },
};
</script>