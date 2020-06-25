<template>
  <v-app :id="appId">
    <div class="activityLikersAndKudos d-flex flex-nowrap">
      <div class="reactionsUsersAvatar">
        <div class="d-flex flex-nowrap">
          <exo-user-avatar
            v-for="liker in likersToDisplay"
            :key="liker.id"
            :title="liker.personLikeFullName"
            :avatar-url="liker.personLikeAvatarImageSource"
            :url="liker.personLikeProfileUri"
            :size="30"
            class="mr-1" />
          <p v-if="likersNumber >0 && likersNumber < maxLikersToShow" class="likersNumber mb-0 pl-2 align-self-end caption">{{ likersNumber }} {{ $t('UIActivity.label.Reactions_Number') }}</p>
        </div>
      </div>
      <div class="activityLikersAndKudosDrawer">
        <div v-if="likersNumber > maxLikersToShow" class="seeMoreLikers" @click="openDrawer">
          <v-avatar
            :size="30">
            <img
              :src="likers[maxLikersToShow].personLikeAvatarImageSource"
              :title="likers[maxLikersToShow].personLikeFullName"
            >
          </v-avatar>
          <span class="seeMoreLikersDetails">+{{ showMoreLikersNumber }}</span>
        </div>

        <activity-reactions-drawer
          ref="reactionsDrawer"
          :likers="likers"
          :likers-number="likersNumber"
          :activity-id="activityId"
          @reactions="reactionsNumber"
        />
      </div>
      <!--<div class="actionBarMobile">
        <a class="likesNumber" @click="openDrawer">{{ likersNumber }} Likes</a>
        <a class="CommentsNumber" href="#">3 Comments</a>
        <a class="KudosNumber" href="#" @click="openDrawer">{{ kudosNumber }} Kudos</a>
      </div>-->
      <activity-reactions-mobile
        :kudos-number="kudosNumber"
        :likers-number="likersNumber"
        @openDrawer="openDrawer"></activity-reactions-mobile>
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
    appId: {
      type: String,
      default: () => ''
    }
  },
  data: () => ({
    maxLikersToShow : 5,
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
    reactionsNumber(value) {
      console.log('heloooooooooooooooooooooooooooooooooooooooooooooooooo',value);
      this.kudosNumber = value;
    }
  },
};
</script>