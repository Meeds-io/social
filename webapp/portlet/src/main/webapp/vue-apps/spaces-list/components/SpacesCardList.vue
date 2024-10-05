<template>
  <v-card
    class="d-flex flex-column"
    min-height="calc(var(--100vh, 100vh) - 180px)"
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
    <div id="spacesListBody" class="flex-grow-1 flex-shrink-1 pt-2">
      <div
        v-if="filteredSpaces && filteredSpaces.length"
        class="d-flex flex-wrap border-box-sizing">
        <space-card
          v-for="space in filteredSpaces"
          :key="space.id"
          :space="space"
          :profile-action-extensions="profileActionExtensions"
          :style="cardFlexBasis && `flex-basis: ${cardFlexBasis}`"
          :height="cardHeight"
          :min-height="cardHeight"
          max-width="100%"
          class="mx-2 mb-4 flex-grow-0 flex-shrink-0 pa-0" />
      </div>
      <v-card
        v-else-if="!loadingSpaces"
        min-height="calc(var(--100vh, 100vh) - 280px)"
        class="d-flex text-center noSpacesYetBlock"
        flat>
        <div class="ma-auto noSpacesYet">
          <p class="noSpacesYetIcons">
            <v-icon class="fa-9x">fa-chevron-left</v-icon>
            <v-icon class="fa-9x">fa-chevron-right</v-icon>
          </p>
          <template v-if="hasSpaces">
            <p class="text-title">
              {{ $t('spacesList.label.noResults') }}
            </p>
          </template>
          <template v-else>
            <p class="text-title">
              {{ $t('spacesList.label.noSpacesYet') }}
            </p>
            <div>
              {{ $t('spacesList.label.noSpacesYetDescription1') }}
            </div>
            <span>
              {{ $t('spacesList.label.noSpacesYetDescription2') }}
              <v-btn
                link
                text
                class="primary--text px-0 pb-1 addNewSpaceLink"
                @click="$root.$emit('addNewSpace')">
                {{ $t('spacesList.label.noSpacesLink') }}
              </v-btn>
            </span>
          </template>
        </div>
      </v-card>
    </div>
    <div id="spacesListFooter" class="flex-grow-0 flex-shrink-0 pb-5 border-box-sizing px-2">
      <v-btn
        v-if="canShowMore"
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
    profileActionExtensions: [],
    resizeObserver: null,
    hasSpaces: false,
    offset: 0,
    pageSize: 12,
    limit: 12,
    spaces: [],
    limitToFetch: 0,
    originalLimitToFetch: 0,
    cardXSpacing: 16 + 2, // margin left/right + border left/right
    cardsListWidth: 0, // computed
    cardHeight: 229,
  }),
  computed: {
    canShowMore() {
      return this.loadingSpaces || this.spaces.length >= this.limitToFetch;
    },
    cardMinWidthBase() {
      if (this.cardsListWidth > 1000) {
        return 280;
      } else if (this.cardsListWidth < 480) {
        return this.cardsListWidth - this.cardXSpacing;
      } else {
        return 220;
      }
    },
    cardPerLine() {
      return this.cardsListWidth && parseInt((this.cardsListWidth + 8) / (this.cardMinWidthBase + this.cardXSpacing));
    },
    cardFlexBasis() {
      return this.cardsListWidth && `calc(${String(100 / this.cardPerLine).substring(0, 12)}% - ${this.cardXSpacing - 2}px)`;
    },
    filteredSpaces() {
      if (!this.keyword || !this.loadingSpaces) {
        return this.spaces;
      } else {
        return this.spaces.slice().filter(space => space.displayName && space.displayName.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0
            || space.description && space.description.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0 );
      }
    },
  },
  watch: {
    keyword() {
      this.searchSpaces();
    },
    limitToFetch() {
      this.searchSpaces();
    },
    filter() {
      this.searchSpaces();
    },
  }, 
  created() {
    this.originalLimitToFetch = this.limitToFetch = this.limit;

    document.addEventListener('extension-profile-extension-action-updated', this.refreshExtensions);
    this.$root.$on('spaces-list-refresh', this.searchSpaces);
    this.refreshExtensions();
  },
  mounted() {
    this.resizeObserver = new ResizeObserver(this.computeWidth);
    this.resizeObserver.observe(this.$el);
    this.computeWidth();
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
      this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
    },
    searchSpaces() {
      this.$emit('loading-spaces', true);
      const expand = this.filter === 'requests' ? 'pending,favorite' : 'managers,favorite,unread';
      return this.$spaceService.getSpaces(this.keyword, this.offset, this.limitToFetch, this.filter, expand)
        .then(data => {
          this.spaces = data && data.spaces || [];
          this.spacesSize = data && data.size || 0;
          this.hasSpaces = this.hasSpaces || this.spacesSize > 0;
          this.$emit('loaded', this.spacesSize);
          return this.$nextTick();
        })
        .then(() => {
          if (this.keyword && this.filteredSpaces.length < this.originalLimitToFetch && this.spaces.length >= this.limitToFetch) {
            this.limitToFetch += this.pageSize;
          }
        })
        .finally(() => this.$emit('loading-spaces', false));
    },
    loadNextPage() {
      this.originalLimitToFetch = this.limitToFetch += this.pageSize;
    },
  }
};
</script>

