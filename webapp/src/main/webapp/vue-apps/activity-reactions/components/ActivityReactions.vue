<template>
  <div
    :class="(isDesktop && !$root.reducedWidth) && 'position-absolute' || ''"
    class="activityReactionsContainer activityLikersAndKudos text-font-size d-flex flex-nowrap py-2"
    @mouseleave="resetHoverState">
    <div
      v-if="!$root.reducedWidth"
      :style="`min-height:${avatarSize}px`"
      class="reactionsUsersAvatar position-relative d-none d-lg-inline"
      @mouseenter="showSeeMoreOverlay = true"
      @mouseleave="resetHoverState">
      <div v-if="!showSeeMoreOverlay || !likersNumber" class="d-flex flex-nowrap">
        <exo-user-avatar
          v-for="(liker, index) in likersToDisplay"
          :key="liker.id"
          :identity="liker"
          :size="avatarSize"
          :allow-animation="likersToDisplay.length > 1"
          :class="[index === 0 && 'pl-4']"
          margin-left="ml-n5"
          popover
          avatar
          compact
          extra-class="me-1 transition-2s" />
      </div>
      <v-btn
        v-else
        v-ripple="false"
        :min-width="seeMoreWidth"
        :min-height="avatarSize"
        :height="avatarSize"
        class="caption white--text grey-lighten1-background px-1 ms-n1"
        text
        @click.prevent.stop="openDrawer">
        <span class="text-body white--text text-center text-no-wrap">{{ $t('activity.reactions.seeMore') }}</span>
      </v-btn>
    </div>
    <div
      v-if="!$root.reducedWidth && !showSeeMoreOverlay"
      class="activityLikersAndKudosDrawer d-none d-lg-inline ml-n5"
      @mouseenter="showSeeMoreOverlay = true">
      <div v-if="seeMoreLikerToDisplay" class="seeMoreReactionsContainer">
        <v-tooltip bottom>
          <template #activator="{ on, attrs }">
            <div
              v-bind="attrs"
              v-on="on"
              :class="displayAnimation && 'mt-n1 transition-2s'"
              class="seeMoreLikers border-white d-flex align-center justify-center clickable"
              @click="openDrawer"
              @mouseover="showAvatarAnimation = true"
              @mouseleave="showAvatarAnimation = false">
              <span
                class="position-absolute white--text font-weight-bold z-index-one text-center">
                +{{ showMoreLikersNumber }}
              </span>
            </div>
          </template>
          <span>
            {{ $t('activity.reactions.seeMore') }}
          </span>
        </v-tooltip>
      </div>
    </div>
    <activity-reactions-mobile
      v-if="!isDesktop || $root.reducedWidth"
      :activity="activity"
      :likers-number="likersNumber"
      :comment-number="commentNumber"
      :total-comments-number="totalCommentsNumber"
      class="d-flex align-center"
      @openDrawer="openDrawer" />
  </div>
</template>
<script>
export default {
  props: {
    activityId: {
      type: String,
      default: () => ''
    },
    activity: {
      type: Object,
      default: null,
    },
    likers: {
      type: Array,
      default: () => []
    },
    likersNumber: {
      type: Number,
      default: 0
    },
    commentNumber: {
      type: Number,
      default: 0
    },
    totalCommentsNumber: {
      type: Number,
      default: 0
    },
    avatarSize: {
      type: Number,
      // eslint-disable-next-line no-magic-numbers
      default: () => 30,
    },
  },
  data: () => ({
    maxLikersToShow: 4,
    showAvatarAnimation: false,
    showSeeMoreOverlay: false
  }),
  computed: {
    seeMoreLikerToDisplay () {
      return this.likersNumber >= this.maxLikersToShow && this.likers[this.maxLikersToShow - 1] || null;
    },
    likersToDisplay () {
      return this.likers.slice(0, this.maxLikersToShow-1);
    },
    seeMoreWidth() {
      const avatarsCount = this.likersToDisplay.length + (this.seeMoreLikerToDisplay ? 1 : 0);
      return this.avatarSize + Math.max(avatarsCount - 1, 0) * 10;
    },
    showMoreLikersNumber() {
      return this.likersNumber - this.maxLikersToShow + 1;
    },
    activityPosterId() {
      return this.activity && this.activity.identity && this.activity.identity.profile && this.activity.identity.profile.username;
    },
    displayAnimation() {
      return this.showAvatarAnimation;
    },
    isDesktop() {
      return this.$vuetify.breakpoint.width >= this.$vuetify.breakpoint.thresholds.lg;
    }
  },
  methods: {
    resetHoverState() {
      this.showSeeMoreOverlay = false;
      this.showAvatarAnimation = false;
    },
    openDrawer() {
      const reactionTabDetails = {
        activityId: this.activityId,
        activityPosterId: this.activityPosterId,
        tab: 'like',
        activityType: 'ACTIVITY'
      };
      this.$root.$emit('open-reaction-drawer-selected-tab', reactionTabDetails);
    },
  },
};
</script>
