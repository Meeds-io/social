<template>
  <v-app 
    id="GettingStartedPortlet"
    class="transparent"
    flat>
    <v-card>
      <v-toolbar flat>
        <v-toolbar-title>
          <button class="btn btn-primary">
            <v-icon dark>mdi-plus</v-icon>
            {{ $t('spacesList.button.addNewSpace') }}
          </button>
        </v-toolbar-title>
        <v-spacer></v-spacer>
        <v-scale-transition>
          <v-text-field
            v-model="keyword"
            :placeholder="$t('spacesList.label.filterSpaces')"
            prepend-inner-icon="fa-filter" 
            class="pa-0"></v-text-field>
        </v-scale-transition>
        <v-scale-transition>
          <v-select
            v-model="filter"
            :items="spaceFilters"
            outlined>
          </v-select>
        </v-scale-transition>
      </v-toolbar>
      <v-card-text>
        <v-item-group>
          <v-container>
            <v-row class="border-box-sizing">
              <exo-spaces-list-item
                v-for="space in spaces"
                :key="space.id"
                :space="space" />
            </v-row>
          </v-container>
        </v-item-group>
      </v-card-text>
      <v-divider></v-divider>
      <v-card-actions class="border-box-sizing">
        <v-btn
          v-if="canShowMore"
          :loading="loadingSpaces"
          class="ma-auto"
          block
          @click="loadNextPage">
          {{ $t('spacesList.button.loadMore') }}
        </v-btn>
      </v-card-actions>
    </v-card>
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
    startSearchAfterInMilliseconds: 400,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    offset: 0,
    pageSize: 12,
    limit: 12,
    keyword: null,
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
        text: this.$t('spacesList.filter.mySpaces'),
        value: 'mySpaces',
      },{
        text: this.$t('spacesList.filter.invitedSpaces'),
        value: 'invitedSpaces',
      },{
        text: this.$t('spacesList.filter.pendingSpaces'),
        value: 'pendingSpaces',
      }];
    },
    canShowMore() {
      return this.spaces.length >= this.limitToFetch;
    },
    filteredSpaces() {
      if (!this.keyword) {
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
        space.spaceUrl = `${eXo.env.portal.context}${space.spaceUrl}`;
      });
    },
    limitToFetch() {
      this.searchSpaces();
    },
  }, 
  created() {
    this.originalLimitToFetch = this.limitToFetch = this.limit;
  },
  methods: {
    searchSpaces() {
      spaceService.getSpaces(this.keyword, this.offset, this.limitToFetch)
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
  }
};
</script>

