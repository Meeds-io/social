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
        <p v-if="likersNumber >0 && likersNumber < maxLikersToShow" class="likersNumber mb-0 pl-2 align-self-end caption">{{ likersNumber }} {{ $t('UIActivity.label.Reactions_Number') }}</p>
      </div>
      <div v-if="likersNumber > maxLikersToShow" class="activityLikersAndKudosDrawer">
        <div class="seeMoreLikers" @click="openDrawer">
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
        />
      </div>
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
      this.$refs.reactionsDrawer.open();
    },
  },
};
</script>