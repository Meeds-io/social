<template>
  <v-card-text id="spacesListBody" class="pb-0">
    <v-item-group>
      <v-container class="pb-0">
        <v-row v-if="filteredSpaces && filteredSpaces.length" class="border-box-sizing">
          <exo-space-card
            v-for="space in filteredSpaces"
            :key="space.id"
            :space="space"
            :profile-action-extensions="profileActionExtensions"
            @refresh="searchSpaces" />
        </v-row>
        <div v-else-if="!loadingSpaces" class="d-flex subtitle-1">
          <span class="ma-auto">{{ $t('spacesList.label.noResults') }}</span>
        </div>
      </v-container>
    </v-item-group>
  </v-card-text>
</template>

<script>
import * as spaceService from '../js/SpaceService.js'; 

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
      type: String,
      default: null,
    },
    loadingSpaces: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    profileActionExtensions: [],
    startSearchAfterInMilliseconds: 600,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    offset: 0,
    pageSize: 20,
    limit: 20,
    spaces: [],
    limitToFetch: 0,
    originalLimitToFetch: 0,
  }),
  computed:{
    canShowMore() {
      return this.loadingSpaces || this.spaces.length >= this.limitToFetch;
    },
    filteredSpaces() {
      if (!this.keyword || !this.loadingSpaces) {
        return this.spaces;
      } else {
        return this.spaces.slice().filter(space => space.displayName && space.displayName.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0);
      }
    },
    selectedSpaceIndex() {
      return this.spaces.findIndex(space => space.spaceUrl === eXo.env.server.portalBaseURL || eXo.env.server.portalBaseURL.indexOf(`${space.spaceUrl}/`) === 0);
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
    spaces() {
      this.spaces.forEach(space => {
        space.spaceUrl = `${eXo.env.portal.context}${space.url}`;
      });
    },
    limitToFetch() {
      this.searchSpaces();
    },
    filter() {
      this.searchSpaces();
    },
    spacesSize() {
      this.$emit('spaces-size-changed', this.spacesSize);
    },
  }, 
  created() {
    this.originalLimitToFetch = this.limitToFetch = this.limit;

    document.addEventListener('profile-extension-updated', this.refreshExtensions);
    this.refreshExtensions();
  },
  methods: {
    refreshExtensions() {
      this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
    },
    searchSpaces() {
      this.loadingSpaces = true;
      return spaceService.getSpaces(this.keyword, this.offset, this.limitToFetch, this.filter)
        .then(data => {
          this.spaces = data && data.spaces || [];
          this.spacesSize = data && data.size || 0;
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
  }
};
</script>

