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
        <exo-likers-kudos-drawer
          ref="likersAndKudosDrawer"
          :likers="likers"
          :kudos-list="kudosList"
          :kudos-number="kudosNumber"
          :likers-number="likersNumber"/>
      </div>
      <p class="reactionsNumber mb-0 pl-2 align-self-end caption">{{ likersNumber+kudosNumber }} {{ $t('UIActivity.label.Reactions_Number') }}</p>
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
    likersLabel: {
      type: String,
      default: () => ''
    },
    appId: {
      type: String,
      default: () => ''
    }
  },
  data: () => ({
    maxLikersToShow : 4,
    kudosList: [],
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
  created() {
    this.getEntityKudos('ACTIVITY', this.activityId).then(kudosList => {
      this.kudosList = kudosList;
      this.kudosNumber = kudosList.length;
      console.log(kudosList);
    });
  },
  methods: {
    getEntityKudos(entityType, entityId, limit) {
      if (entityType && entityId) {
        return fetch(`/portal/rest/kudos/api/kudos/byEntity?entityId=${entityId}&entityType=${entityType}&limit=${limit || 0}`, {
          credentials: 'include',
          headers: {
            Accept: 'application/json',
            'Content-Type': 'application/json',
          },
        }).then((resp) => resp && resp.ok && resp.json());
      } else {
        return Promise.resolve([]);
      }
    },
    openDrawer() {
      this.$refs.likersAndKudosDrawer.open();
    },
  },
};
</script>