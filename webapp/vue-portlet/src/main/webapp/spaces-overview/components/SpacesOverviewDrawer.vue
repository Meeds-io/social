<template>
  <exo-drawer
    ref="overviewDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template slot="title">
      {{ title }}
    </template>
    <template slot="content">
      <v-layout column class="ma-3">
        <template v-if="spaces && spaces.length" class="ma-0 border-box-sizing">
          <spaces-overview-spaces-list
            :spaces="spaces"
            :filter="filter"
            @refresh="searchSpaces()"
            @edit="editSpace" />
        </template>
        <template v-else>
          <span class="ma-auto">{{ $t('spacesOverview.label.noResults') }}</span>
        </template>
        <v-card-actions class="flex-grow-1 justify-center my-2">
          <v-spacer />
          <v-btn
            v-if="canShowMore"
            :loading="loadingSpaces"
            :disabled="loadingSpaces"
            class="loadMoreButton ma-auto btn"
            @click="loadNextPage">
            {{ $t('spacesOverview.label.showMore') }}
          </v-btn>
          <v-spacer />
        </v-card-actions>
      </v-layout>
    </template>
  </exo-drawer>
</template>

<script>
import * as spaceService from '../../common/js/SpaceService.js'; 

export default {
  data: () => ({
    title: null,
    filter: null,
    loadingSpaces: false,
    offset: 0,
    pageSize: 20,
    spaces: [],
  }),
  computed:{
    canShowMore() {
      return this.loadingSpaces || this.spaces.length >= this.limit;
    },
  },
  methods: {
    searchSpaces(filter) {
      this.$emit('refresh');

      this.spaces = [];
      this.loadingSpaces = true;
      return spaceService.getSpaces(null, this.offset, this.limit, filter || this.filter)
        .then(data => {
          this.spaces = data && data.spaces || [];
          if (filter) {
            this.filter = filter;
          }
          return this.$nextTick();
        })
        .finally(() => this.loadingSpaces = false);
    },
    loadNextPage() {
      this.limit += this.pageSize;
      this.searchSpaces();
    },
    editSpace(space) {
      document.dispatchEvent(new CustomEvent('meeds.social.editSpace', {'detail': {'data' : space}}));
      this.$refs.overviewDrawer.close();
    },
    open(filter, title) {
      this.title = title;
      this.limit = this.pageSize;
      this.searchSpaces(filter);
      this.$refs.overviewDrawer.open();
    },
  }
};
</script>