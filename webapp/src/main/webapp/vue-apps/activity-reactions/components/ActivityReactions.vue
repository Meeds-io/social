<template>
  <div
    class="activityReactionsContainer activityLikersAndKudos text-font-size d-flex flex-nowrap py-2"
    :class="isDesktop && 'position-absolute' || ''">
    <div
      class="reactionsUsersAvatar position-relative d-none d-lg-inline"
      :style="`min-height:${avatarSize}px`">
      <div class="d-flex flex-nowrap">
        <exo-user-avatar
          v-for="(liker, index) in likersToDisplay"
          :key="liker.id"
          :allow-animation="likersToDisplay.length > 1"
          avatar
          :class="[index === 0 && 'pl-4']"
          compact
          extra-class="me-1 transition-2s"
          :identity="liker"
          margin-left="ml-n5"
          popover
          :size="avatarSize" />
      </div>
    </div>
    <div class="activityLikersAndKudosDrawer d-none d-lg-inline ml-n5">
      <div
        v-if="seeMoreLikerToDisplay"
        class="seeMoreReactionsContainer">
        <v-tooltip bottom>
          <template #activator="{ on, attrs }">
            <div
              v-bind="attrs"
              class="seeMoreLikers border-white d-flex align-center justify-center clickable"
              :class="displayAnimation && 'mt-n1 transition-2s'"
              v-on="on"
              @click="openDrawer"
              @mouseleave="showAvatarAnimation = false"
              @mouseover="showAvatarAnimation = true">
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
      :activity="activity"
      class="d-flex d-lg-none align-center"
      :comment-number="commentNumber"
      :likers-number="likersNumber"
      @open-drawer="openDrawer" />
  </div>
</template>
<script>
  export default {
    props: {
      activityId: {
        type: String,
        default: () => '',
      },
      activity: {
        type: Object,
        default: null,
      },
      likers: {
        type: Array,
        default: () => [],
      },
      likersNumber: {
        type: Number,
        default: 0,
      },
      commentNumber: {
        type: Number,
        default: 0,
      },
      avatarSize: {
        type: Number,
       
        default: () => 30,
      },
    },
    data: () => ({
      maxLikersToShow: 4,
      showAvatarAnimation: false,
    }),
    computed: {
      seeMoreLikerToDisplay () {
        return this.likersNumber >= this.maxLikersToShow && this.likers[this.maxLikersToShow - 1] || null;
      },
      likersToDisplay () {
        return this.likers.slice(0, this.maxLikersToShow-1);
      },
      showMoreLikersNumber () {
        return this.likersNumber - this.maxLikersToShow + 1;
      },
      activityPosterId () {
        return this.activity && this.activity.identity && this.activity.identity.profile && this.activity.identity.profile.username;
      },
      displayAnimation () {
        return this.showAvatarAnimation;
      },
      isDesktop () {
        return eXo.vuetify.display.width.value >= eXo.vuetify.display.thresholds.value.lg;
      },
    },
    methods: {
      openDrawer () {
        const reactionTabDetails = {
          activityId: this.activityId,
          activityPosterId: this.activityPosterId,
          tab: 'like',
          activityType: 'ACTIVITY',
        };
        this.$root.$emit('open-reaction-drawer-selected-tab', reactionTabDetails);
      },
    },
  };
</script>
