<template>
  <v-flex
    :class="shaped && 'ms-12'"
    class="mx-0 spacesNavigationContent"
    flat>
    <v-list :shaped="shaped" dense>
      <v-list-item-group v-model="selectedSpaceIndex">
        <v-list-item
          v-for="space in filteredSpaces"
          :key="space.id"
          :href="url(space)"
          :class="homeIcon && (homeLink === url(space) && 'UserPageLinkHome' || 'UserPageLink')"
          link
          class="px-2 spaceItem">
          <v-list-item-avatar 
            size="28"
            class="me-3 tile my-0 spaceAvatar"
            tile>
            <v-img :src="space.avatarUrl" />
          </v-list-item-avatar>
          <v-list-item-content>
            <v-list-item-title class="body-2" v-text="space.displayName" />
          </v-list-item-content>
          <v-menu
            v-model="menu"
            right
            bottom
            offset-y
            open-on-hover>
            <template #activator="{ on, attrs }">
              <v-list-item-icon
                :disabled="loading"
                :loading="loading"
                class="me-2 align-center"
                v-bind="attrs"
                v-on="on">
                <span :class="menu && 'spaceMenuOpened' || '' " class="fas spaceThreeDotsMenu"></span>
              </v-list-item-icon>
            </template>
            <v-list class="pa-0">
              <v-list-item
                :href="url(space)"
                target="_blank"
                link
                @click="leftNavigationActionEvent('openInNewTab')">
                <v-icon size="15" class="fas fa-external-link-alt icon-default-color mr-3" />
                <span class="text-color">{{ $t('menu.spaces.openInNewTab') }}</span>
              </v-list-item>
              <exo-space-favorite-action
                v-if="favoriteActionEnabled"
                :is-favorite="space.isFavorite"
                :space-id="space.id"
                entity-type="spaces_left_navigation"
                display-label />
              <v-list-item
                v-if="homeLink !== url(space)"
                @click="selectHome($event, space)">
                <v-icon size="16" class="fas fa-house-user icon-default-color mr-3" />
                <span class="text-color mt-1">{{ $t('menu.spaces.makeAsHomePage') }}</span>
              </v-list-item>
            </v-list>
          </v-menu>
        </v-list-item>
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
    favoritesSpaceEnabled: eXo.env.portal.spaceFavoritesEnabled,
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
    favoriteActionEnabled() {
      return this.favoritesSpaceEnabled;
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
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces?q=&offset=${this.offset}&limit=${this.limitToFetch}&filterType=lastVisited&returnSize=true&expand=member,favorite`, {
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
    leftNavigationActionEvent(clickedItem) {
      document.dispatchEvent(new CustomEvent('space-left-navigation-action', {detail: clickedItem} ));
    },
    selectHome(event, space) {
      this.$emit('selectHome', event, space);
      this.leftNavigationActionEvent('makeAsHomePage');
    }
  }
};
</script>

