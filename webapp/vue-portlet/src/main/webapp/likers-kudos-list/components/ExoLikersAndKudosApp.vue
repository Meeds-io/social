<template>
  <v-app :id="appId">
    <div class="activityLikersAndKudos d-flex flex-nowrap">
      <div class="d-flex flex-nowrap">
        <exo-user-avatar
          v-for="liker in likersToDisplay"
          :key="liker.id"
          :title="liker.personLikeFullName"
          :avatar-url="liker.personLikeAvatarImageSource"
          :url="liker.personLikeProfileUri"
          :size="30"
          class="mr-1" />
      </div>
      <div class="activityLikersAndKudosDrawer">
        <div class="seeMoreLikers" @click="openDrawer">
          <v-avatar
            :size="30">
            <img
              :src="likers[5].personLikeAvatarImageSource"
              :title="likers[maxLikersToShow].personLikeFullName"
            >
          </v-avatar>
          <span class="seeMoreLikersDetails">+{{ showMoreLikersNumber }}</span>
        </div>
        <exo-likers-kudos-drawer
          ref="likersAndKudosDrawer"
          :likers="likers"
          :likers-number="likersNumber"
          :kudos-number="kudosNumber"/>
      </div>
    </div>
  </v-app>
</template>
<script>
export default {
  props: {
    likers: {
      type: Array,
      default: () => []
    },
    likersNumber: {
      type: Number,
      default: 0
    },
    likersLabel: {
      type: String,
      default: () => ''
    },
    appId: {
      type: String,
      default: () => ''
    },
    kudosNumber: {
      type: Number,
      default: 0
    }
  },
  data: () => ({
    maxLikersToShow : 5
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
      this.$refs.likersAndKudosDrawer.open();
    },
  },
};
</script>