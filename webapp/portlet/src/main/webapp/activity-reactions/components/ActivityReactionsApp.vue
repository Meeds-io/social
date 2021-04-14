<template>
  <v-app :id="appId">
    <div class="activityLikersAndKudos d-flex flex-nowrap">
      <div class="reactionsUsersAvatar">
        <div class="d-flex flex-nowrap">
          <exo-user-avatar
            v-for="liker in likersToDisplay"
            :key="liker.id"
            :username="liker.likerId"
            :title="liker.personLikeFullName"
            :avatar-url="liker.personLikeAvatarImageSource"
            :url="liker.personLikeProfileUri"
            :size="30"
            class="me-1" />
        </div>
      </div>
      <div class="activityLikersAndKudosDrawer">
        <div class="seeMoreReactionsContainer">
          <div
            v-if="likersNumber > maxLikersToShow"
            class="seeMoreLikers"
            @click="openDrawer">
            <v-avatar
              :size="30">
              <img
                :src="likers[maxLikersToShow].personLikeAvatarImageSource"
                :title="likers[maxLikersToShow].personLikeFullName">
            </v-avatar>
            <span class="seeMoreLikersDetails">+{{ showMoreLikersNumber }}</span>
          </div>
          <p
            v-if="likersNumber"
            class="likersNumber my-auto ps-2 align-self-end caption"
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
        :kudos-number="kudosNumber"
        :likers-number="likersNumber"
        :comment-number="commentNumber"
        @openDrawer="openDrawer" />
    </div>
  </v-app>
</template>
<script>
export default {
  props: {
    activityId: {
      type: String,
      default: () => ''
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
    appId: {
      type: String,
      default: () => ''
    }
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