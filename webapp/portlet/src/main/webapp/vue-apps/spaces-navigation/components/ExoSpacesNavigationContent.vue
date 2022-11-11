<template>
  <v-flex
    :class="shaped && 'ms-12'"
    class="mx-0 spacesNavigationContent"
    flat>
    <v-list dense>
      <v-list-item-group v-model="selectedSpaceIndex">
        <space-navigation-item 
          v-for="space in filteredSpaces" 
          :key="space.id" 
          :space="space"
          :space-url="url(space)"
          :home-icon="homeIcon"
          :home-link="homeLink"
          @open-space-panel="openOrCloseSpacePanel(space)"
          @close-space-panel="openOrCloseSpacePanel(space)" />
      </v-list-item-group>
    </v-list>
    <v-row v-if="canShowMore" class="mx-0 my-4 justify-center">
      <v-btn
        small
        depressed
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
  },
  data: () => ({
    startSearchAfterInMilliseconds: 400,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    spaces: [],
    initialized: false,
    loadingSpaces: false,
    limitToFetch: 0,
    originalLimitToFetch: 0,
  }),
  computed: {
    canShowMore() {
      return this.showMoreButton && !this.loadingSpaces && this.spaces.length >= this.limitToFetch;
    },
    filteredSpaces() {
      if (!this.keyword) {
        return this.spaces;
      } else {
        return this.spaces.slice().filter(space => space.displayName && space.displayName.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0);
      }
    },
    selectedSpaceIndex() {
      return this.spaces.findIndex(space => this.url(space) === eXo.env.server.portalBaseURL || eXo.env.server.portalBaseURL.indexOf(`${this.url(space)}/`) === 0);
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
      this.searchSpaces()
        .finally(() => {
          if (!this.initialized) {
            this.initialized = true;
            this.$root.$applicationLoaded();
          }
        });
    },
  }, 
  created() {
    this.originalLimitToFetch = this.limitToFetch = this.limit;
  },
  methods: {
    searchSpaces() {
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces?q=&offset=${this.offset}&limit=${this.limitToFetch}&filterType=lastVisited&returnSize=true&expand=member,managers,favorite`, {
        method: 'GET',
        credentials: 'include',
      })
        .then(resp => resp && resp.ok && resp.json())
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
    openOrCloseSpacePanel(space) {
      if (space) {
        this.$emit('open-space-panel',space);
      } else {
        this.$emit('close-space-panel',null);
      }
    }
  }
};
</script>

