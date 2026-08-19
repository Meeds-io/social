<template>
  <div
    :class="cssClass"
    class="d-inline-flex">
    <reaction-chooser
      :current-reaction-id="userReactionId"
      :disabled="isScheduled"
      object-type="activity"
      @reaction-select="selectReaction">
      <!-- Added for mobile -->
      <v-tooltip :disabled="isMobile || isScheduled" bottom>
        <template #activator="{ on, attrs }">
          <v-btn
            :id="`LikeLink${activityId}`"
            :loading="changingLike"
            :disabled="isScheduled"
            :class="[likeTextColorClass, isScheduled && 'opacity-5']"
            :aria-label="isScheduled ? null : (hasLiked ? $t('UIActivity.aria.Like') : $t('UIActivity.msg.LikeActivity'))"
            class="pa-0 mt-0"
            text
            link
            small
            v-bind="{
              ...attrs,
              role: null,
              'aria-haspopup': null,
              'aria-expanded': null,
              'aria-pressed': hasLiked}"
            v-on="on"
            @click="changeLike">
            <div class="d-flex flex-lg-row flex-column">
              <span
                v-if="userReactionEmoji"
                :style="`font-size: ${isMobile && '20' || '16'}px; height: ${isMobile && '20' || '16'}px; line-height: 1;`"
                class="reaction-emoji d-inline-flex align-center justify-center me-lg-1 baseline-vertical-align">{{ userReactionEmoji }}</span>
              <v-icon
                v-else
                :class="likeColorClass"
                class="baseline-vertical-align"
                :size="isMobile && '20' || '16'">
                fa-thumbs-up
              </v-icon>
              <span
                v-if="!isMobile && !$root.reducedWidth"
                :class="hasLiked && 'primary--text' || ''"
                class="mx-auto mt-1 mt-lg-0 ms-lg-1 text-body">
                {{ likeLabel }}
              </span>
            </div>
          </v-btn>
        </template>
        <span>
          {{ likeButtonTitle }}
        </span>
      </v-tooltip>
    </reaction-chooser>
  </div>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    isScheduled: {
      type: Boolean,
      default: () => false
    },
  },
  data: () => ({
    changingLike: false,
    hasLiked: false,
    reactionOptions: [],
  }),
  computed: {
    activityId() {
      return this.activity && this.activity.id;
    },
    userReactionId() {
      const reactionItems = this.activity?.metadatas?.reactions;
      const userItem = reactionItems?.find?.(item => `${item.creatorId}` === `${eXo.env.portal.userIdentityId}`);
      return userItem?.name || (this.hasLiked && 'like') || null;
    },
    likeLabel() {
      if (!this.hasLiked || !this.userReactionId) {
        return this.$t('UIActivity.msg.LikeActivity');
      }
      const option = this.reactionOptions.find(registered => registered.id === this.userReactionId);
      return option?.selectedLabelKey && this.$t(option.selectedLabelKey)
        || this.$t('UIActivity.reaction.selected.custom');
    },
    userReactionEmoji() {
      if (!this.userReactionId) {
        return null;
      }
      const option = this.reactionOptions.find(registered => registered.id === this.userReactionId);
      if (option) {
        return option.emoji;
      }
      return /^[a-z0-9_-]+$/i.test(this.userReactionId) ? null : this.userReactionId;
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
    cssClass() {
      return (this.$vuetify.breakpoint.width >= this.$vuetify.breakpoint.thresholds.xl && !this.$root.reducedWidth) ? 'ms-4'
        : ((this.$vuetify.breakpoint.width >= this.$vuetify.breakpoint.thresholds.lg && !this.$root.reducedWidth) ? ' ms-3' :'');
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'sm';
    },
  },
  created() {
    this.computeLikes();
    this.$reactionService.getReactionOptions('activity')
      .then(options => this.reactionOptions = options);
  },
  methods: {
    changeLike() {
      if (this.changingLike) {
        return;
      }
      if (this.hasLiked) {
        return this.unlikeActivity();
      } else {
        return this.applyReaction('like');
      }
    },
    selectReaction(option) {
      if (this.changingLike || this.isScheduled || (this.hasLiked && option.id === this.userReactionId)) {
        return;
      }
      return this.applyReaction(option.id);
    },
    applyReaction(reactionId) {
      this.changingLike = true;
      return this.$reactionService.setReaction('activity', this.activityId, reactionId)
        .then(() => {
          this.activity.hasLiked = 'true';
          this.updateLocalReactionItem(reactionId);
          return this.refreshLikers();
        })
        .finally(() => this.changingLike = false);
    },
    unlikeActivity() {
      this.changingLike = true;
      return this.$reactionService.deleteReaction('activity', this.activityId)
        .then(() => {
          this.activity.hasLiked = 'false';
          this.updateLocalReactionItem(null);
          return this.refreshLikers();
        })
        .finally(() => this.changingLike = false);
    },
    refreshLikers() {
      return this.$activityService.getActivityLikers(this.activityId, 0, 50)
        .then(data => {
          this.computeLikes(data);
          this.$root.$emit('activity-liked', this.activityId);
        });
    },
    updateLocalReactionItem(reactionId) {
      if (!this.activity.metadatas) {
        this.$set(this.activity, 'metadatas', {});
      }
      const userIdentityId = `${eXo.env.portal.userIdentityId}`;
      const reactionItems = (this.activity.metadatas.reactions || [])
        .filter(item => `${item.creatorId}` !== userIdentityId);
      if (reactionId && reactionId !== 'like') {
        reactionItems.push({
          name: reactionId,
          objectType: 'activity',
          objectId: this.activityId,
          creatorId: eXo.env.portal.userIdentityId,
        });
      }
      this.$set(this.activity.metadatas, 'reactions', reactionItems);
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
