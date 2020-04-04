<template>
  <v-app 
    id="spacesListApplication"
    class="transparent"
    flat>
    <main>
      <v-card flat>
        <v-toolbar id="spacesListToolbar" flat>
          <v-toolbar-title>
            <button class="btn btn-primary">
              <v-icon dark>mdi-plus</v-icon>
              <span class="mobile-hidden">
                {{ $t('spacesList.button.addNewSpace') }}
              </span>
            </button>
          </v-toolbar-title>
          <v-card-subtitle>
            {{ $t('spacesList.label.spacesSize', {0: spacesSize}) }}
          </v-card-subtitle>
          <v-spacer></v-spacer>
          <v-scale-transition>
            <v-text-field
              v-model="keyword"
              :placeholder="$t('spacesList.label.filterSpaces')"
              prepend-inner-icon="fa-filter"
              class="inputSpacesFilter pa-0 mr-3 my-auto"></v-text-field>
          </v-scale-transition>
          <v-scale-transition>
            <select
              v-model="filter"
              class="selectSpacesFilter my-auto mr-2 ignore-vuetify-classes">
              <option
                v-for="spaceFilter in spaceFilters"
                :key="spaceFilter.value"
                :value="spaceFilter.value">
                {{ spaceFilter.text }}
              </option>
            </select>
          </v-scale-transition>
        </v-toolbar>
        <v-card-text id="spacesListBody" class="pb-0">
          <v-item-group>
            <v-container class="pb-0">
              <v-row class="border-box-sizing">
                <exo-spaces-card-flip
                  v-for="space in filteredSpaces"
                  :key="space.id"
                  :space="space"
                  @refresh="searchSpaces" />
              </v-row>
            </v-container>
          </v-item-group>
        </v-card-text>
        <v-card-actions id="spacesListFooter" class="px-5 border-box-sizing">
          <v-btn
            v-if="canShowMore"
            :loading="loadingSpaces"
            :disabled="loadingSpaces"
            class="loadMoreButton ma-auto btn"
            block
            @click="loadNextPage">
            {{ $t('spacesList.button.showMore') }}
          </v-btn>
        </v-card-actions>
      </v-card>
      <exo-space-managers-drawer />
    </main>
  </v-app>    
</template>

<script>
import * as spaceService from '../js/SpaceService.js'; 

export default {
  props: {
    appId: {
      type: String,
      default: null,
    },
    filter: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    startSearchAfterInMilliseconds: 600,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    offset: 0,
    pageSize: 20,
    limit: 20,
    keyword: null,
    spacesSize: 0,
    spaces: [],
    loadingSpaces: false,
    limitToFetch: 0,
    originalLimitToFetch: 0,
  }),
  computed:{
    spaceFilters() {
      return [{
        text: this.$t('spacesList.filter.all'),
        value: 'all',
      },{
        text: this.$t('spacesList.filter.userSpaces'),
        value: 'userSpaces',
      }];
    },
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
  }, 
  created() {
    this.originalLimitToFetch = this.limitToFetch = this.limit;
  },
  methods: {
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

