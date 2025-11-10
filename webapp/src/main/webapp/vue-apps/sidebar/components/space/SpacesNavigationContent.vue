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
    <v-list
      :role="null"
      dense>
      <v-list-item-group
        :value="selectedSpaceIndex"
        :role="null">
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
        :loading="loadingSpaces"
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
    filterType: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    startSearchAfterInMilliseconds: 400,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    spaces: [],
    loadingSpaces: false,
    initialized: false,
    limitToFetch: 0,
    originalLimitToFetch: 0,
    selectedSpaceIndex: -1,
  }),
  computed: {
    canShowMore() {
      return this.showMoreButton && this.initialized && (this.loadingSpaces || this.spaces.length >= this.limitToFetch);
    },
    filteredSpaces() {
      if (!this.keyword) {
        return this.spaces;
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
    filterType() {
      this.spaces = [];
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
      if (!this.loadingSpaces && !this.initialized) {
        this.initialized = true;
      }
    },
  }, 
  created() {
    this.originalLimitToFetch = this.limitToFetch = this.limit;
    document.addEventListener('space-unread-activities-updated', this.applySpaceUnreadChanges);
    document.addEventListener('unread-items-deleted', this.refreshByEvent);
    this.refreshSelectedSpace();
  },
  beforeDestroy() {
    document.removeEventListener('space-unread-activities-updated', this.applySpaceUnreadChanges);
    document.removeEventListener('unread-items-deleted', this.refreshByEvent);
    this.refreshSelectedSpace();
  },
  methods: {
    refreshByEvent(event) {
      if (event) {
        this.searchSpaces();
      }
    },
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
    async searchSpaces() {
      this.loadingSpaces = true;
      try {
        if (this.filterType === 'unread') {
          let spaceIds = this.$root.unreadPerSpace && Object.keys(this.$root.unreadPerSpace)
            .filter(id => Number(this.$root.unreadPerSpace[id]))
            .sort((id1, id2) => Number(this.$root.unreadPerSpace[id2]) - Number(this.$root.unreadPerSpace[id1]));
          if (this.offset) {
            spaceIds = spaceIds.slice(this.offset);
          }
          if (this.limitToFetch) {
            spaceIds = spaceIds.slice(0, this.limitToFetch);
          }
          if (spaceIds?.length) {
            this.spaces = await Promise
              .all(spaceIds.map(spaceId => this.$spaceService.getSpaceById(spaceId,'member,managers,favorite,unread,muted')));
          } else {
            this.spaces = [];
          }
        } else {
          const openedItem =this.$root.openedItem;
          const properties = openedItem?.properties;
          const type = openedItem.type;
          const data = await this.$spaceService.getSpacesByFilter({
            query: this.filterType === 'lastVisited' ? this.keyword : '',
            filter: this.filterType || 'lastVisited',
            sortBy: 'lastVisited',
            expand: 'member,managers,favorite,unread,muted',
            offset: this.offset,
            limit: this.limitToFetch,
            ...(type === 'SPACES'
              ? {
                excludedCategoryIds: await this.appendExcludedSubCategories(this.parseJsonArray(properties.excludedCategoryIds) || []),
                categoryIds: this.parseJsonArray(properties.spaceCategoryIds),
                templateId: this.parseJsonArray(properties.spaceTemplateIds) || this.$root.openedSpaceTemplateId || 0,
              }
              : {
                categoryIds: this.parseJsonArray(properties.spaceCategoryIds) ?? null,
                templateId: this.$root.openedSpaceTemplateId || 0,
              }),
          });
          this.spaces = data?.spaces || [];
        }
        await this.$nextTick();
        if (this.keyword && this.filteredSpaces.length < this.originalLimitToFetch && this.spaces.length >= this.limitToFetch) {
          this.limitToFetch += this.pageSize;
        }
      } finally {
        this.loadingSpaces = false;
      }
    },
    async appendExcludedSubCategories(excludedCategories) {
      const subcategoryLists = await Promise.all(
        excludedCategories.map(id =>
          this.$categoryService.getSubcategoryIds(id, {depth: 1, offset: 0, limit: -1}).catch(err => {
            console.warn(`Could not fetch subcategories for ID ${id}`, err);
            return [];
          })
        )
      );
      const subcategoryIds = subcategoryLists.flat();
      return [...new Set([...excludedCategories, ...subcategoryIds])];
    },
    parseJsonArray(value) {
      try {
        const parsed = JSON.parse(value);
        return Array.isArray(parsed) && parsed.length > 0 ? parsed : null;
      } catch {
        return null;
      }
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
      if (space?.id) {
        return `${eXo.env.portal.context}/s/${space.id}/`;
      } else {
        return '#';
      }
    },
  }
};
</script>
