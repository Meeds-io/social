<template>
  <v-app>
    <widget-wrapper v-if="isShown" :title="$t('externalSpacesList.title.yourSpaces')">
      <v-list dense class="py-0">
        <template>
          <external-space-item
            v-for="space in spacesList"
            :key="space.id"
            :space="space" />
          <external-spaces-requests-items @invitationReplied="refreshSpaces" />
        </template>
      </v-list>
    </widget-wrapper>
  </v-app>
</template>
<script>
export default {
  data () {
    return {
      spacesList: [],
      spacesRequestsSize: 0,
    };
  },
  computed: {
    isShown() {
      return this.spacesList && this.spacesList.length > 0 || this.spacesRequestsSize > 0;
    }
  },
  created() {
    this.getExternalSpacesList();
    this.$externalSpacesListService.getExternalSpacesRequests()
      .then(data => this.spacesRequestsSize = data.spacesMemberships.length)
      .then(() => this.$nextTick())
      .finally(() => {
        this.$root.$applicationLoaded();
        this.$root.$updateApplicationVisibility(this.isShown);
      });
  },
  methods: {
    getExternalSpacesList() {
      this.$externalSpacesListService.getExternalSpacesList().then(data => {
        this.spacesList = data.spaces;
      });
    },
    refreshSpaces(space) {
      this.spacesList.unshift(space);
    },
  }
};
</script>
