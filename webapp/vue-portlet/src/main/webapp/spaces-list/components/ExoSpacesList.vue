<template>
  <v-app 
    id="spacesListApplication"
    class="transparent"
    flat>
    <main>
      <v-card flat>
        <v-toolbar id="spacesListToolbar" flat>
          <v-toolbar-title>
            <button class="btn btn-primary addNewSpaceButton" @click="$root.$emit('addNewSpace')">
              <v-icon dark>mdi-plus</v-icon>
              <span class="d-none d-sm-inline">
                {{ $t('spacesList.label.addNewSpace') }}
              </span>
            </button>
          </v-toolbar-title>
          <v-card-subtitle class="d-none d-sm-flex">
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
              class="selectSpacesFilter my-auto mr-2 subtitle-1 ignore-vuetify-classes d-none d-sm-inline">
              <option
                v-for="spaceFilter in spaceFilters"
                :key="spaceFilter.value"
                :value="spaceFilter.value">
                {{ spaceFilter.text }}
              </option>
            </select>
          </v-scale-transition>
          <v-icon class="d-sm-none" @click="openBottomMenu">fa-filter</v-icon>
          <v-bottom-sheet v-model="bottomMenu" class="pa-0">
            <v-sheet class="text-center" height="169px">
              <v-toolbar color="primary" dark>
                <v-btn text @click="bottomMenu = false">
                  {{ $t('spacesList.button.cancel') }}
                </v-btn>
                <v-spacer></v-spacer>
                <v-toolbar-title>
                  <v-icon>fa-filter</v-icon>
                  {{ $t('spacesList.label.filter') }}
                </v-toolbar-title>
                <v-spacer></v-spacer>
                <v-btn text @click="changeFilterSelection">
                  {{ $t('spacesList.button.confirm') }}
                </v-btn>
              </v-toolbar>
              <v-list>
                <v-list-item
                  v-for="spaceFilter in spaceFilters"
                  :key="spaceFilter"
                  @click="filterToChange = spaceFilter.value">
                  <v-list-item-title class="align-center d-flex">
                    <v-icon v-if="filterToChange === spaceFilter.value">fa-check</v-icon>
                    <span v-else class="mr-6"></span>
                    <v-spacer />
                    <div>
                      {{ spaceFilter.text }}
                    </div>
                    <v-spacer />
                    <span class="mr-6"></span>
                  </v-list-item-title>
                </v-list-item>
              </v-list>
            </v-sheet>
          </v-bottom-sheet>
        </v-toolbar>
        <v-card-subtitle class="d-sm-none align-center mb-2">
          {{ $t('spacesList.label.spacesSize', {0: spacesSize}) }}
        </v-card-subtitle>
        <v-card-text id="spacesListBody" class="pb-0">
          <v-item-group>
            <v-container class="pb-0">
              <v-row v-if="filteredSpaces && filteredSpaces.length" class="border-box-sizing">
                <exo-space-card
                  v-for="space in filteredSpaces"
                  :key="space.id"
                  :space="space"
                  @refresh="searchSpaces" />
              </v-row>
              <div v-else-if="!loadingSpaces" class="d-flex subtitle-1">
                <span class="ma-auto">{{ $t('spacesList.label.noResults') }}</span>
              </div>
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
      <exo-space-form-drawer />
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
    filterToChange: null,
    bottomMenu: false,
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
    openBottomMenu() {
      this.filterToChange = this.filter;
      this.bottomMenu = true;
    },
    changeFilterSelection() {
      this.bottomMenu = false;
      this.filter = this.filterToChange;
    },
  }
};
</script>

