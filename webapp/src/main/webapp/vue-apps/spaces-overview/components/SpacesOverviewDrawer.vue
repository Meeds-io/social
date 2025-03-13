<template>
  <exo-drawer
    ref="overviewDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template #title>
      {{ title }}
    </template>
    <template #content>
      <v-layout
        class="ma-3"
        column>
        <spaces-overview-spaces-list
          v-if="spaces && spaces.length"
          class="ma-0 border-box-sizing"
          :filter="filter"
          :spaces="spaces"
          @edit="editSpace"
          @refresh="refresh" />
        <template v-else-if="!loadingSpaces">
          <span class="ma-auto">{{ $t('spacesOverview.label.noResults') }}</span>
        </template>
        <v-card-actions class="flex-grow-1 justify-center my-2">
          <v-spacer />
          <v-btn
            v-if="canShowMore"
            class="loadMoreButton ma-auto btn"
            :disabled="loadingSpaces"
            :loading="loadingSpaces"
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
  export default {
    data: () => ({
      title: null,
      filter: null,
      loadingSpaces: false,
      offset: 0,
      limit: 20,
      pageSize: 20,
      spaces: [],
    }),
    computed: {
      canShowMore () {
        return this.loadingSpaces || this.spaces.length >= this.limit;
      },
    },
    methods: {
      refresh (itemType) {
        this.$emit('refresh', itemType);
        this.searchSpaces();
      },
      searchSpaces (filter) {
        this.spaces = [];
        this.loadingSpaces = true;
        return eXo.$spaceService.getSpacesByFilter({
          offset: this.offset,
          limit: this.limit,
          filter: filter || this.filter,
        }).then(data => {
          this.spaces = data && data.spaces || [];
          if (filter) {
            this.filter = filter;
          }
          return this.$nextTick();
        }).finally(() => this.loadingSpaces = false);
      },
      loadNextPage () {
        this.limit += this.pageSize;
        this.searchSpaces();
      },
      editSpace (space) {
        document.dispatchEvent(new CustomEvent('meeds.social.editSpace', { 'detail': { 'data': space } }));
        this.$refs.overviewDrawer.close();
      },
      open (filter, title) {
        this.title = title;
        this.limit = this.pageSize;
        this.searchSpaces(filter);
        this.$refs.overviewDrawer.open();
      },
    },
  };
</script>