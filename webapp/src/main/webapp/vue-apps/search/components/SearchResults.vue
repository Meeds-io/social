<template>
  <v-flex
    class="transparent"
    flat>
    <search-options
      :favorites="favorites"
      :all-enabled="allEnabled"
      :sorted-connectors="sortedConnectors"
      :enabled-connectors="enabledConnectors"
      @select-favorites="selectFavorites"
      @select-tags="selectTags"
      @select-all-connector="selectAllConnector"
      @select-connector="selectConnector" />
    <extension-registry-components
      :params="searchComponentParams"
      name="SearchResult"
      type="search-result-grouping-card"
      parent-element="div"
      element="div"
      class=" d-flex flex-column" />
    <div v-if="hasResults" class="searchResultsParent d-flex flex-column border-box-sizing">
      <div
        v-for="result in resultsArray"
        :key="result.domId"
        class="pa-0 searchCard">
        <search-result-card
          v-if="!isGroupingResult(result)"
          :result="result"
          :term="term" />
        <search-result-card-group
          v-else
          :results="result"
          :term="term" />
      </div>
    </div>
    <v-card
      flat
      v-if="noResults && !$root.disablePlaceholder"
      min-height="calc(var(--100vh, 100vh) - 350px)"
      class="d-flex justify-center align-center">
      <div class="d-flex flex-column ma-auto text-center text-subtitle">
        <v-icon
          class="tertiary-color my-auto"
          size="60"
          color="tertiary">
          fas fa-search
        </v-icon>
        <span class="headline">{{ $t('Search.noResults.placeholder') }}</span>
      </div>
    </v-card>
    <v-flex v-if="hasMore" class="searchLoadMoreParent d-flex my-4 border-box-sizing">
      <v-btn
        :loading="loading"
        :disabled="loading"
        class="btn mx-auto"
        @click="loadMore">
        {{ $t('Search.button.loadMore') }}
      </v-btn>
    </v-flex>
  </v-flex>
</template>
<script>
export default {
  props: {
    term: {
      type: String,
      default: null,
    },
    connectors: {
      type: Array,
      default: () => [],
    },
  },
  data: () => ({
    index: 0,
    totalSize: 0,
    results: null,
    pageSize: 10,
    limit: 10,
    selectedTags: [],
    selectedSpaces: [],
    favorites: false,
    allEnabled: true,
    searching: 0,
    abortController: null,
    searchInitialized: false,
    sortBy: '',
    sortDescending: true,
  }),
  computed: {
    hasMore() {
      return this.totalSize && this.searchEnabledConnectors?.filter(connector => connector.hasMore)?.length;
    },
    loading() {
      return this.searching > 0;
    },
    hasResults() {
      return this.resultsArray && this.resultsArray.length;
    },
    noResults() {
      return this.searchInitialized && !this.hasResults && (this.term || this.favorites) && !this.searching && this.results && Object.keys(this.results).length;
    },
    sortedConnectors() {
      if (!this.connectors) {
        return [];
      }
      return this.connectors.map(connector => {
        connector.label = this.$t(`search.connector.label.${connector.name}`);
        return connector;
      }).sort((connector1, connector2) => {
        return connector1.label > connector2.label ? 1 : connector1.label < connector2.label ? -1 : 0;
      });
    },
    enabledConnectors() {
      return this.sortedConnectors && this.sortedConnectors.filter(connector => connector.enabled) || [];
    },
    enabledConnectorNames() {
      return this.enabledConnectors.map(connector => connector.name);
    },
    enabledGroupingConnectorNames() {
      return this.enabledConnectors.filter(connector => connector.groupingEnabled).map(connector => connector.name);
    },
    searchEnabledConnectors() {
      return this.enabledConnectors.filter(connector => {
        return (connector.favoritesEnabled || !this.favorites)
          && (connector.tagsEnabled || !this.selectedTags.length)
          && (connector.spaceFilterEnabled || !this.selectedSpaces.length);
      });
    },
    resultsArray() {
      if (!this.results || !this.totalSize || this.searching < 0) {
        return;
      }
      const connectorNames = Object.keys(this.results);
      const finalResults = [];

      connectorNames.forEach(connectorName => {
        if (this.enabledConnectorNames.includes(connectorName)) {
          let connectorResults = this.results[connectorName];

          if (this.favorites) {
            connectorResults = connectorResults.filter(
              result =>
                (result.metadatas && result.metadatas.favorites) ||
                    result.favorite ||
                    result.isFavorite
            );
          }

          if (this.enabledGroupingConnectorNames.includes(connectorName) && connectorResults?.length) {
            // Keep as grouped sublist
            finalResults.push(connectorResults);
          } else {
            // Flatten into list
            finalResults.push(...connectorResults);
          }
        }
      });
      // Sort by index
      finalResults.sort((a, b) => {
        const indexA = Array.isArray(a) ? a[0]?.index ?? Infinity : a.index;
        const indexB = Array.isArray(b) ? b[0]?.index ?? Infinity : b.index;
        return indexA - indexB;
      });
      return finalResults;
    },
    searchComponentParams() {
      return {
        connectors: this.enabledConnectors,
        term: this.term,
        isFavorite: this.favorites,
        tags: this.selectedTags,
        spaces: this.selectedSpaces,
        sortBy: this.sortBy,
      };
    },
  },
  watch: {
    loading: {
      immediate: true,
      handler() {
        this.$emit('loading', this.loading);
      }
    },
    selectedTags() {
      this.$emit('tags-changed', this.selectedTags);
      if (this.searchInitialized) {
        this.$nextTick().then(this.search);
      }
    },
    selectedSpaces() {
      this.totalSize = 0;
      if (this.searchInitialized) {
        this.$nextTick().then(this.search);
      }
    },
    favorites() {
      this.$emit('favorites-changed', this.favorites);
      if (this.searchInitialized) {
        this.$nextTick().then(this.search);
      }
    },
    term() {
      this.totalSize = 0;
      this.limit = this.pageSize;
      if (this.searchInitialized) {
        this.search();
      }
    },
    sortBy() {
      this.totalSize = 0;
      if (this.searchInitialized) {
        this.$nextTick().then(this.search);
      }
    }
  },
  created() {
    this.$root.$on('refresh', (searchConnector, favorites) => {
      if (!!favorites === !!this.favorites) {
        this.$set(this.results, searchConnector.name, []);
        this.retrieveConnectorResults(searchConnector);
      }
    });
    let allEnabled = true;
    this.connectors.forEach(connector => {
      allEnabled = allEnabled && connector.enabled;
    });
    this.allEnabled = !!allEnabled;

    const search = window.location.search && window.location.search.substring(1);
    if (search) {
      const parameters = new URLSearchParams(search);
      this.favorites = parameters?.get('favorites') === 'true';
      const selectedSpaces = parameters.getAll('spaceId');
      if (selectedSpaces) {
        this.selectedSpaces.push(...selectedSpaces);
      }
    }
    if (this.favorites || this.term) {
      this.search();
    } else {
      this.searchInitialized = true;
    }
    this.$root.$on('spaces-changed', this.selectSpaces);
    this.$root.$on('favorites-changed', this.selectFavorites);
    this.$root.$on('sort-changed', this.selectSort);
  },
  beforeDestroy() {
    this.$root.$off('spaces-changed', this.selectSpaces);
    this.$root.$off('favorites-changed', this.selectFavorites);
    this.$root.$off('sort-changed', this.selectSort);
  },
  methods: {
    selectFavorites() {
      if (!this.favorites) {
        document.dispatchEvent(new CustomEvent('search-favorites-selected'));
      }
      this.favorites = !this.favorites;
      this.$emit('filter-changed');
    },
    selectTags(tags) {
      this.selectedTags = tags || [];
      this.$emit('filter-changed');
    },
    selectSpaces(spaces) {
      this.selectedSpaces = spaces || [];
    },
    selectSort(option, sortDescending) {
      this.sortBy = option?.value || '';
      this.sortDescending = sortDescending;
    },
    selectAllConnector() {
      if (this.allEnabled) {
        return;
      }
      this.connectors.forEach(connector => {
        connector.enabled = true;
      });
      this.allEnabled = true;
      window.setTimeout(() => {
        this.$emit('filter-changed');
        this.$nextTick().then(this.search);
      }, 50);
    },
    selectConnector(selectedConnector) {
      if (!selectedConnector) {
        return;
      }
      if (!selectedConnector.enabled || this.connectors.length === this.enabledConnectors.length) {
        document.dispatchEvent(new CustomEvent('search-connector-selected', {
          detail:
          selectedConnector.name,
        }));
      }
      if (this.connectors.length === this.enabledConnectors.length) {
        this.connectors.forEach(connector => {
          connector.enabled = connector.name === selectedConnector.name;
        });
      } else if (selectedConnector.enabled && this.enabledConnectors.length === 1) {
        this.connectors.forEach(connector => {
          connector.enabled = true;
        });
      } else {
        selectedConnector.enabled = !selectedConnector.enabled;
      }

      let allEnabled = true;
      this.connectors.forEach(connector => {
        allEnabled = allEnabled && connector.enabled;
      });
      this.allEnabled = allEnabled;
      window.setTimeout(() => {
        this.$emit('filter-changed');
        this.$nextTick().then(this.search);
      }, 50);
    },
    loadMore() {
      this.limit += this.pageSize;
      this.search();
    },
    search() {
      if (this.abortController) {
        this.abortController.abort();
      }
      if (!this.term && !this.favorites && !this.selectedTags.length) {
        this.results = null;
        return;
      }
      this.results = {};
      this.connectors.forEach(connector => {
        connector.size = -1;
        this.results[connector.name] = [];
      });
      let signal = {};
      if (window.AbortController) {
        this.abortController = new window.AbortController();
        signal = this.abortController.signal;
      }

      this.searchEnabledConnectors.forEach(searchConnector => {
        // If not first loading or connector doesn't have more
        if (searchConnector.size !== -1 && !searchConnector.hasMore) {
          return;
        }

        this.retrieveConnectorResults(searchConnector, signal);
      });
    },
    retrieveConnectorResults(searchConnector, signal) {
      if (!searchConnector) {
        return;
      }

      return window.require([searchConnector.jsModule], connectorModule => {
        let options = {
          headers: {
            Accept: 'application/json',
          }
        };
        if (signal) {
          options = Object.assign(options, signal);
        }
        if (searchConnector.uri.indexOf('/') === 0) {
          options.credentials = 'include';
        }
        this.searching++;
        let uri = searchConnector.uri
          .replace('{keyword}', window.encodeURIComponent(this.term || ''))
          .replace('{limit}', this.limit);
        if (this.favorites) {
          if (uri.includes('?')) {
            uri += '&favorites=true';
          } else {
            uri += '?favorites=true';
          }
        }
        if (this.selectedTags && this.selectedTags.length) {
          this.selectedTags.forEach(selectedTag => {
            const tag = selectedTag.replace('#', '');
            if (uri.includes('?')) {
              uri += `&tags=${tag}`;
            } else {
              uri += `?tags=${tag}`;
            }
          });
        }
        if (this.selectedSpaces?.length) {
          this.selectedSpaces.forEach(spaceId => {
            uri += `&spaceId=${spaceId}`;
          });
        }
        uri = this.appendSortParams(uri, this.sortBy, this.sortDescending);
        const fetchResultsQuery = connectorModule.fetchSearchResult ?
          connectorModule.fetchSearchResult(uri, options)
          : fetch(uri, options);
        return fetchResultsQuery
          .then(resp => {
            if (resp && resp.ok) {
              return resp.json();
            } else {
              throw new Error('Error getting result');
            }
          })
          .then(result => {
            if (connectorModule && connectorModule.formatSearchResult) {
              return connectorModule.formatSearchResult(result, this.term || '');
            } else {
              return result;
            }
          })
          .then(resultArray => {
            if (resultArray && resultArray.length) {
              searchConnector.size = resultArray.length;
              searchConnector.hasMore = searchConnector.enabled && searchConnector.uri && searchConnector.size >= this.limit;
              resultArray.forEach(result => {
                result.connector = searchConnector;
                result.index = ++this.index;
                result.domId = result.domId || `SearchResult${result.index}`;
              });
              this.$set(this.results, searchConnector.name, resultArray);
              this.totalSize = this.results[searchConnector.name].length;
            }
          })
          .catch(e => searchConnector.error = e)
          .finally(() => {
            this.searching--;
            this.searchInitialized = true;
          });
      });
    },
    isGroupingResult(result) {
      return Array.isArray(result);
    },
    appendSortParams(uri, sortBy, sortDescending) {
      let sortDirection = sortDescending ? 'desc' : 'asc';
      sortDirection = sortBy && sortDirection || '';
      sortDescending = sortBy && sortDescending || '';

      if (this.sortBy && !uri.includes('{sortField}') && !uri.includes('{sortDirection}') && !uri.includes('{sortDescending}')) {
        const separator = uri.includes('?') ? '&' : '?';
        uri += `${separator}sortField=${sortBy}&sortDirection=${sortDirection}`;
        return uri;
      }
      uri = uri.replace('{sortField}', sortBy)
        .replace('{sortDirection}', sortDirection)
        .replace('{sortDescending}', sortDescending);

      return uri;
    }
  },
};
</script>
