<template>
  <exo-drawer ref="overviewDrawer" right>
    <template slot="title">
      <span class="subtitle-2 font-weight-bold text-truncate">
        {{ title }}
      </span>
    </template>
    <template slot="content">
      <v-layout column class="ma-3">
        <template v-if="spaces && spaces.length" class="ma-0 border-box-sizing">
          <v-flex
            v-for="space in spaces"
            :key="space.id"
            class="flex-grow-1 text-truncate ma-1">
            <exo-space-avatar :space="space">
              <template slot="subTitle">
                {{ $t('subtitle.members', {0: space.membersCount}) }}
              </template>
            </exo-space-avatar>
          </v-flex>
        </template>
        <template v-else>
          <span class="ma-auto">{{ $t('label.noSpaces') }}</span>
        </template>
        <v-card-actions class="flex-grow-1 justify-center my-2">
          <v-spacer />
          <v-btn
            v-if="canShowMore"
            :loading="loadingSpaces"
            :disabled="loadingSpaces"
            class="loadMoreButton ma-auto btn"
            @click="loadNextPage">
            {{ $t('label.showMore') }}
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
    searchSpaces() {
      if (!this.filter) {
        return;
      }
      this.loadingSpaces = true;
      return spaceService.getSpaces(null, this.offset, this.limit, this.filter)
        .then(data => {
          this.spaces = data && data.spaces || [];
          return this.$nextTick();
        })
        .finally(() => this.loadingSpaces = false);
    },
    loadNextPage() {
      this.limit += this.pageSize;
      this.searchSpaces();
    },
    open(filter, title) {
      this.title = title;
      this.filter = filter;
      this.limit = this.pageSize;
      this.searchSpaces();
      this.$refs.overviewDrawer.open();
    },
  }
};
</script>