<template>
  <div class="activityReactionsContainer activityLikersAndKudos text-font-size d-flex flex-nowrap py-2">
    <div class="reactionsUsersAvatar position-relative d-none d-lg-inline">
      <div class="d-flex flex-nowrap">
        <exo-user-avatar
          v-for="liker in likersToDisplay"
          :key="liker.id"
          :identity="liker"
          :size="30"
          popover
          avatar 
          extra-class="me-1" />
      </div>
    </div>
    <div class="activityLikersAndKudosDrawer d-none d-lg-inline">
      <div class="seeMoreReactionsContainer">
        <div
          v-if="seeMoreLikerToDisplay"
          class="seeMoreLikers"
          @click="openDrawer">
          <span class="seeMoreLikersDetails">+{{ showMoreLikersNumber }}</span>
        </div>
        <p
          v-if="likersNumber && likersNumber <= 1"
          class="likersNumber my-auto pl-2 align-self-end caption text-no-wrap"
          @click="openDrawer">
          {{ likersNumber }} {{ $t('UIActivity.label.single_Reaction_Number') }}
        </p>
        <p
          v-if="likersNumber > 1"
          class="likersNumber my-auto pl-2 align-self-end caption text-no-wrap"
          @click="openDrawer">
          {{ likersNumber }} {{ $t('UIActivity.label.Reactions_Number') }}
        </p>
      </div>
    </div>
    <activity-reactions-mobile
      :activity="activity"
      :likers-number="likersNumber"
      :comment-number="commentNumber"
      class="d-flex d-lg-none align-center"
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
    avatarSize: {
      type: Number,
      // eslint-disable-next-line no-magic-numbers
      default: () => 30,
    },
  },
  data: () => ({
    maxLikersToShow: 4,
  }),
  computed: {
    seeMoreLikerToDisplay () {
      return this.likersNumber >= this.maxLikersToShow && this.likers[this.maxLikersToShow - 1] || null;
    },
    likersToDisplay () {
      return this.likers.slice(0, this.maxLikersToShow-1);
    },
    showMoreLikersNumber() {
      return this.likersNumber - this.maxLikersToShow + 1;
    },
    activityPosterId() {
      return this.activity && this.activity.identity && this.activity.identity.profile && this.activity.identity.profile.username;
    }
  },
  methods: {
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
