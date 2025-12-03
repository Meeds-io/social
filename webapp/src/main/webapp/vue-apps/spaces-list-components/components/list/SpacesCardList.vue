<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
  <v-card
    class="d-flex flex-column"
    flat>
    <v-card
      class="position-relative px-2 overflow-hidden"
      height="8"
      flat>
      <v-progress-linear
        v-if="loadingSpaces"
        class="position-absolute"
        color="primary"
        indeterminate
        height="2" />
    </v-card>
    <div id="spacesListBody" class="flex-grow-1 flex-shrink-1">
      <div
        v-if="spacesSize"
        class="d-flex flex-wrap border-box-sizing">
        <space-card
          v-for="space in spaces"
          :key="space.id"
          :space="space"
          :space-action-extensions="spaceActionExtensions"
          :style="cardFlexBasis && `flex-basis: ${cardFlexBasis}`"
          :height="cardHeight"
          :min-height="cardHeight"
          :display-members-count="displayMembersCount"
          max-width="100%"
          class="mx-2 mb-4 flex-grow-0 flex-shrink-0 pa-0" />
      </div>
      <v-card
        v-else-if="!loadingSpaces"
        min-height="250"
        class="d-flex align-center justify-center noSpacesYetBlock"
        flat>
        <div class="noSpacesYet">
          <p>
            <v-icon color="tertiary" size="60">fa-layer-group</v-icon>
          </p>
          <p class="text-title">
            {{ $t('spacesList.noSpacesFound') }}
          </p>
          <div v-if="displayNoSpaceOptions && $root.canCreateSpace" v-html="noSpacesFoundAdminOption"></div>
          <div v-else-if="$root.canEdit" v-html="noSpacesFoundSettingsOption"></div>
          <div v-else v-html="$t('spacesList.noSpacesFoundUserOption')"></div>
        </div>
      </v-card>
    </div>
    <div
      v-if="canShowMore"
      id="spacesListFooter"
      class="flex-grow-0 flex-shrink-0 pb-5 border-box-sizing px-2">
      <v-btn
        :loading="loadingSpaces"
        :disabled="loadingSpaces"
        class="loadMoreButton border-color elevation-0 ma-auto"
        block
        @click="loadNextPage">
        {{ $t('spacesList.button.showMore') }}
      </v-btn>
    </div>
  </v-card>
</template>
<script>
export default {
  props: {
    keyword: {
      type: String,
      default: null,
    },
    filter: {
      type: String,
      default: null,
    },
    spacesSize: {
      type: Number,
      default: 0,
    },
    loadingSpaces: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    spaceActionExtensions: [],
    resizeObserver: null,
    initialized: false,
    hasSpaces: false,
    offset: 0,
    limit: 12,
    spaces: [],
    limitToFetch: 0,
    originalLimitToFetch: 0,
    cardXSpacing: 16 + 2, // margin left/right + border left/right
    cardsListWidth: 0, // computed
    cardHeightDesktop: 229,
    cardHeightMobile: 140,
  }),
  computed: {
    pageSize() {
      return this.$root.settings.pageSize;
    },
    canShowMore() {
      return this.loadingSpaces || this.spaces.length >= this.limitToFetch;
    },
    cardHeight() {
      return !this.$root.isMobile && this.cardHeightDesktop || this.cardHeightMobile;
    },
    cardMinWidthBase() {
      if (this.cardsListWidth > 800) {
        return 320;
      } else if (this.cardsListWidth < 480) {
        return this.cardsListWidth - this.cardXSpacing;
      } else {
        return 280;
      }
    },
    cardPerLine() {
      return this.cardsListWidth && parseInt((this.cardsListWidth + 8) / (this.cardMinWidthBase + this.cardXSpacing));
    },
    cardFlexBasis() {
      return this.cardsListWidth && `calc(${String(100 / this.cardPerLine).substring(0, 12)}% - ${this.cardXSpacing - 2}px)`;
    },
    cardWidth() {
      return parseInt(this.cardsListWidth / this.cardPerLine) + this.cardXSpacing + 2;
    },
    displayMembersCount() {
      return this.cardWidth > 280;
    },
    selectedCategoryIds() {
      return this.$root.selectedCategoryIds || this.$root.categoryIds;
    },
    excludeCategoryIds() {
      return this.$root.excludeCategoryIds;
    },
    displayNoSpaceOptions() {
      return !this.hasSpaces
        && this.$root.settings?.filterType === 'any'
        && this.$root.settings?.sortBy === 'title';
    },
    noSpacesFoundAdminOption() {
      return this.$t('spacesList.noSpacesFoundAdminOption', {
        0: `<a href="#" onclick="document.dispatchEvent(new CustomEvent('addNewSpaceWithAppId', {detail: ${this.$root.id}}))">`,
        1: '</a>',
      });
    },
    noSpacesFoundSettingsOption() {
      return this.$t('spacesList.noSpacesFoundSettingsOption', {
        0: `<a href="#" onclick="document.dispatchEvent(new CustomEvent('spaces-list-settings-open', {detail: ${this.$root.id}}))">`,
        1: '</a>',
      });
    },
  },
  watch: {
    keyword() {
      if (this.initialized) {
        this.searchSpaces();
      }
    },
    limitToFetch() {
      if (this.initialized) {
        this.searchSpaces();
      }
    },
    filter() {
      if (this.initialized) {
        this.searchSpaces();
      }
    },
    selectedCategoryIds() {
      if (this.initialized) {
        this.searchSpaces();
      }
    },
    excludeCategoryIds() {
      if (this.initialized) {
        this.searchSpaces();
      }
    },
  }, 
  created() {
    this.limit = this.pageSize;
    this.originalLimitToFetch = this.limitToFetch = this.limit;

    document.addEventListener('extension-profile-extension-action-updated', this.refreshExtensions);
    this.$root.$on('spaces-list-refresh', this.searchSpaces);
    this.refreshExtensions();
    this.searchSpaces();
  },
  mounted() {
    this.resizeObserver = new ResizeObserver(this.computeWidth);
    this.resizeObserver.observe(this.$el);
    this.computeWidth();
    if (!this.$root.anonymous) {
      this.refreshUnreadSpaces();
    }
  },
  beforeDestroy() {
    document.removeEventListener('extension-profile-extension-action-updated', this.refreshExtensions);
    this.$root.$off('spaces-list-refresh', this.searchSpaces);
    this.resizeObserver?.disconnect?.();
  },
  methods: {
    computeWidth() {
      this.cardsListWidth = this.$el?.offsetWidth - 40;
    },
    refreshExtensions() {
      this.spaceActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
    },
    async refreshUnreadSpaces() {
      const data = await this.$spaceService.getSpacesByFilter({
        filter: 'member',
        expand: 'unread',
      });
      this.$root.unreadPerSpace = data?.unreadPerSpace;
    },
    async searchSpaces() {
      this.$emit('loading-spaces', true);
      try {
        const data = await this.$spaceService.getSpacesByFilter({
          categoryIds: this.selectedCategoryIds,
          excludedCategoryIds: this.excludeCategoryIds,
          templateId: this.$root.templateIds,
          sortBy: this.$root.sortBy,
          query: this.keyword,
          offset: this.offset,
          limit: this.limitToFetch,
          filter: this.$root.sortBy === 'lastVisited' ? 'lastVisited' : this.filter,
          expand: this.getExpand(),
          token: this.$root.anonymous && this.$root.settingName
        });
        this.spaces = data && data.spaces || [];
        this.hasSpaces = this.hasSpaces || this.spacesSize > 0;
        this.$emit('loaded', data?.size || 0);
        await this.$nextTick();
      } finally {
        this.$emit('loading-spaces', false);
        this.initialized = true;
      }
    },
    loadNextPage() {
      this.originalLimitToFetch = this.limitToFetch += this.pageSize;
    },
    getExpand() {
      if (this.$root.anonymous) {
        return '';
      }
      return this.filter === 'requests' ? 'pending,favorite' : 'managers,favorite,groupBinding';
    }
  }
};
</script>

