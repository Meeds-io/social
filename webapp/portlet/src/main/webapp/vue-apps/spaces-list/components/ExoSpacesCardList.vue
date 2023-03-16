<template>
  <v-card flat>
    <v-progress-linear
      v-if="loadingSpaces"
      indeterminate
      height="2"
      color="primary" />
    <v-card-text id="spacesListBody" class="pb-0">
      <v-item-group>
        <v-container class="pa-0">
          <v-row v-if="filteredSpaces && filteredSpaces.length" class="ma-0 border-box-sizing">
            <v-col
              v-for="space in filteredSpaces"
              :key="space.id"
              cols="12"
              md="6"
              lg="4"
              xl="4"
              class="pa-0">
              <exo-space-card
                :space="space"
                :profile-action-extensions="profileActionExtensions"
                @refresh="searchSpaces" />
            </v-col>
          </v-row>
          <div v-else-if="!loadingSpaces" class="d-flex text-center noSpacesYetBlock">
            <div class="ma-auto noSpacesYet">
              <p class="noSpacesYetIcons">
                <v-icon>fa-chevron-left</v-icon>
                <v-icon>fa-chevron-right</v-icon>
              </p>
              <template v-if="hasSpaces">
                <p class="title font-weight-bold">
                  {{ $t('spacesOverview.label.noResults') }}
                </p>
              </template>
              <template v-else>
                <p class="title font-weight-bold">
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
                    class="primary--text px-0 addNewSpaceLink"
                    @click="$root.$emit('addNewSpace')">
                    {{ $t('spacesList.label.noSpacesLink') }}
                  </v-btn>
                </span>
              </template>
            </div>
          </div>
        </v-container>
      </v-item-group>
    </v-card-text>
    <v-card-actions id="spacesListFooter" class="pt-0 px-5 border-box-sizing">
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
    startSearchAfterInMilliseconds: 600,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    hasSpaces: false,
    offset: 0,
    pageSize: 20,
    limit: 20,
    spaces: [],
    limitToFetch: 0,
    originalLimitToFetch: 0,
    typing: false,
  }),
  computed: {
    canShowMore() {
      return this.loadingSpaces || this.spaces.length >= this.limitToFetch;
    },
    filteredSpaces() {
      if (!this.keyword || !this.loadingSpaces) {
        return this.spaces;
      } else {
        return this.spaces.slice().filter(space => space.displayName && space.displayName.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0
            || space.description && space.description.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0 );
      }
    },
    selectedSpaceIndex() {
      return this.spaces.findIndex(space => space.spaceUrl === eXo.env.server.portalBaseURL || eXo.env.server.portalBaseURL.indexOf(`${space.spaceUrl}/`) === 0);
    },
  },
  watch: {
    keyword() {
      if (!this.keyword?.length) {
        this.resetSearch();
        this.searchSpaces();
        return;
      }
      this.startTypingKeywordTimeout = Date.now();
      if (!this.typing) {
        this.typing = true;
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
    typing() {
      if (this.typing) {
        this.$emit('loading-spaces', true);
      }
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
      this.$emit('loading-spaces', true);
      const expand = this.filter === 'requests' ? 'pending,favorite' : 'managers,favorite';
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
          this.typing = false;
          this.searchSpaces();
        } else {
          this.waitForEndTyping();
        }
      }, this.endTypingKeywordTimeout);
    },
  }
};
</script>

