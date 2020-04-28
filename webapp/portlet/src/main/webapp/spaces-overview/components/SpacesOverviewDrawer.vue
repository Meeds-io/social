<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
contact@meeds.io
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License
along with this program; if not, write to the Free Software Foundation,
Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
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
            @refresh="refresh"
            @edit="editSpace" />
        </template>
        <template v-else-if="!loadingSpaces">
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
    refresh(itemType) {
      this.$emit('refresh', itemType);
      this.searchSpaces();
    },
    searchSpaces(filter) {
      this.spaces = [];
      this.loadingSpaces = true;
      return this.$spaceService.getSpaces(null, this.offset, this.limit, filter || this.filter)
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