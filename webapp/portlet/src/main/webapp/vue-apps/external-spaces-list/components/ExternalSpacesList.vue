<template>
  <v-app v-if="isShown">
    <widget-wrapper :title="$t('externalSpacesList.title.yourSpaces')">
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
import * as externalSpacesListService from '../externalSpacesListService.js';

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
    externalSpacesListService.getExternalSpacesRequests().then(
      (data) => {
        this.spacesRequestsSize = data.spacesMemberships.length;
      }).finally(() => this.$root.$applicationLoaded());
  },
  methods: {
    getExternalSpacesList() {
      externalSpacesListService.getExternalSpacesList().then(data => {
        this.spacesList = data.spaces;
      });
    },
    refreshSpaces(space) {
      this.spacesList.unshift(space);
    },
  }
};
</script>
