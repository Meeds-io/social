<template>
  <div class="activityReactionsContainer activityLikersAndKudos text-font-size d-flex flex-nowrap py-2">
    <div class="reactionsUsersAvatar d-none d-lg-inline">
      <div class="d-flex flex-nowrap">
        <exo-user-avatar
          v-for="liker in likersToDisplay"
          :key="liker.id"
          :username="liker.username"
          :title="liker.fullname"
          :avatar-url="liker.avatar"
          :size="30"
          class="me-1" />
      </div>
    </div>
    <div class="activityLikersAndKudosDrawer d-none d-lg-inline">
      <div class="seeMoreReactionsContainer">
        <div
          v-if="likersNumber > maxLikersToShow"
          class="seeMoreLikers"
          @click="openDrawer">
          <v-avatar
            :size="30">
            <img
              :src="likers[maxLikersToShow].avatar"
              :title="likers[maxLikersToShow].fullname">
          </v-avatar>
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
      <activity-reactions-drawer
        ref="reactionsDrawer"
        :likers="likers"
        :likers-number="likersNumber"
        :activity-id="activityId"
        :max-items-to-show="maxLikersToShow"
        @reactions="reactionsNumber" />
    </div>
    <activity-reactions-mobile
      :activity="activity"
      :kudos-number="kudosNumber"
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
  },
  data: () => ({
    maxLikersToShow: 4,
    kudosNumber: 0
  }),
  computed: {
    likersToDisplay () {
      return this.likers.slice(0, this.maxLikersToShow-1);
    },
    showMoreLikersNumber() {
      return this.likers.length - this.maxLikersToShow;
    }
  },
  methods: {
    openDrawer() {
      this.$refs.reactionsDrawer.open();
    },
    reactionsNumber(kudosCount) {
      this.kudosNumber = kudosCount;
    }
  },
};
</script>
