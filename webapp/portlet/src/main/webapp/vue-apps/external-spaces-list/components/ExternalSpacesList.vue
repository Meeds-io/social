<template>
  <div>
    <v-app v-if="isShown">
      <v-flex
        d-flex
        xs12
        sm12>
        <v-layout
          row
          wrap
          mx-0>
          <v-flex
            d-flex
            xs12>
            <v-card
              flat
              class="flex">
              <v-card-title class="external-spaces-list-title subtitle-1 text-uppercase pb-2">
                <span class="body-1 text-uppercase text-sub-title">
                  {{ $t('externalSpacesList.title.yourSpaces') }}
                </span>
              </v-card-title>
              <v-list dense>
                <template>
                  <external-space-item
                    v-for="space in spacesList"
                    :key="space.id"
                    :space="space" />
                  <external-spaces-requests-items @invitationReplied="refreshSpaces" />
                </template>
              </v-list>
            </v-card>
          </v-flex>
        </v-layout>
      </v-flex>
    </v-app>
  </div>
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
