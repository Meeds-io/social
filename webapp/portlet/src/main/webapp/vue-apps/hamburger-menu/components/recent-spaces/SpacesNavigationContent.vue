<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
  <v-flex
    :class="shaped && 'ms-12'"
    class="mx-0 spacesNavigationContent"
    flat>
    <v-list dense>
      <v-list-item-group :value="selectedSpaceIndex">
        <space-navigation-item
          v-for="space in filteredSpaces" 
          :key="space.id"
          :space="space"
          :space-url="url(space)"
          :home-icon="homeIcon"
          :home-link="homeLink"
          :opened-space="openedSpace"
          :third-level="thirdLevel" />
      </v-list-item-group>
    </v-list>
    <v-row v-if="canShowMore" class="mx-0 my-4 justify-center">
      <v-btn
        class="btn"
        small
        @click="loadNextPage()">
        {{ $t('menu.spaces.showMore') }}
      </v-btn>
    </v-row>
  </v-flex>
</template>
<script>
export default {
  props: {
    homeLink: {
      type: String,
      default: null,
    },
    homeIcon: {
      type: Boolean,
      default: false,
    },
    offset: {
      type: Number,
      default: 0,
    },
    limit: {
      type: Number,
      default: 10,
    },
    pageSize: {
      type: Number,
      default: 10,
    },
    keyword: {
      type: Object,
      default: null,
    },
    showMoreButton: {
      type: Boolean,
      default: false,
    },
    shaped: {
      type: Boolean,
      default: false,
    },
    thirdLevel: {
      type: Boolean,
      default: false,
    },
    openedSpace: {
      type: Object,
      default: null,
    },
    recentSpaces: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    startSearchAfterInMilliseconds: 400,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    spaces: [],
    loadingSpaces: false,
    limitToFetch: 0,
    originalLimitToFetch: 0,
    selectedSpaceIndex: -1,
  }),
  computed: {
    canShowMore() {
      return this.showMoreButton && !this.loadingSpaces && this.spaces.length >= this.limitToFetch;
    },
    filteredSpaces() {
      if (!this.keyword) {
        if (!this.recentSpaces) {
          return this.spaces;
        } else {
          return this.recentSpaces;
        }
      } else {
        return this.spaces.slice().filter(space => space.displayName && space.displayName.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0);
      }
    },
  },
  watch: {
    keyword() {
      if (!this.keyword) {
        this.resetSearch();
        this.searchSpaces();
        return;
      }
      this.startTypingKeywordTimeout = Date.now();
      if (!this.loadingSpaces) {
        this.loadingSpaces = true;
        this.waitForEndTyping();
      }
    },
    limitToFetch() {
      this.searchSpaces();
    },
    filteredSpaces() {
      this.$emit('spaces-count', this.filteredSpaces?.length);
    },
    spaces() {
      this.refreshSelectedSpace();
    },
    loadingSpaces() {
      this.$emit('loading', this.loadingSpaces);
    },
  }, 
  created() {
    this.originalLimitToFetch = this.limitToFetch = this.limit;
    document.addEventListener('space-unread-activities-updated', this.applySpaceUnreadChanges);
    document.addEventListener('unread-items-deleted', (event) => {
      if (event) {
        this.searchSpaces();
      }
    });
    this.refreshSelectedSpace();
  },
  methods: {
    refreshSelectedSpace() {
      this.selectedSpaceIndex = this.spaces?.findIndex?.(space => eXo.env.server.portalBaseURL.includes(this.url(space)));
    },
    applySpaceUnreadChanges(event) {
      if (!event?.detail) {
        return;
      }
      const {spaceId, unread} = event.detail;
      const space = this.spaces?.find(displayedSpace => displayedSpace.id === spaceId);
      if (space) {
        space.unread = unread && JSON.parse(JSON.stringify(unread)) || null;
      }
    },
    searchSpaces() {
      this.loadingSpaces = true;
      return this.$spaceService.getSpaces('', this.offset, this.limitToFetch, 'lastVisited', 'member,managers,favorite,unread,muted')
        .then(data => {
          this.spaces = data && data.spaces || [];
          return this.$nextTick();
        })
        .then(() => {
          if (this.keyword && this.filteredSpaces.length < this.originalLimitToFetch && this.spaces.length >= this.limitToFetch) {
            this.limitToFetch += this.pageSize;
          }
        })
        .finally(() => this.loadingSpaces = false);
    },
    resetSearch() {
      if (this.limitToFetch !== this.originalLimitToFetch) {
        this.limitToFetch = this.originalLimitToFetch;
      }
    },
    loadNextPage() {
      this.originalLimitToFetch = this.limitToFetch += this.pageSize;
    },
    waitForEndTyping() {
      window.setTimeout(() => {
        if (Date.now() - this.startTypingKeywordTimeout > this.startSearchAfterInMilliseconds) {
          this.searchSpaces();
        } else {
          this.waitForEndTyping();
        }
      }, this.endTypingKeywordTimeout);
    },
    url(space) {
      if (space && space.groupId) {
        const uriPart = space.groupId.replace(/\//g, ':');
        return `${eXo.env.portal.context}/g/${uriPart}/`;
      } else {
        return '#';
      }
    },
  }
};
</script>