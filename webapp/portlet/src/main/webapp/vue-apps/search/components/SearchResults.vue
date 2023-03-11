<template>
  <v-flex>
    <div class="searchConnectorsParent d-flex align-center mx-4 mb-4 border-box-sizing">
      <v-chip
        :outlined="!favorites"
        :color="favorites ? 'primary' : ''"
        class="ms-1 me-2 border-color"
        @click="selectFavorites">
        <v-icon
          size="16"
          class="pb-1 pe-2 yellow--text text--darken-2">
          fas fa-star
        </v-icon>
        <span class="subtitle-1">{{ $t('search.connector.label.favorites') }}</span>
      </v-chip>
      <search-tag-selector @tags-changed="selectTags" />
      <v-menu
        v-model="connectorsListOpened"
        :close-on-content-click="false"
        content-class="connectors-list"
        bottom
        right
        offset-y>
        <template #activator="{ on, attrs }">
          <v-chip
            :outlined="!allEnabled"
            :color="allEnabled ? 'primary' : ''"
            class="border-color mx-1 subtitle-1"
            v-bind="attrs"
            v-on="on">
            <span class="me-8">{{ $t('search.connector.label.all') }}</span>
            <i class="fas fa-chevron-down"></i>
          </v-chip>
        </template>
        <v-list dense class="pa-0">
          <v-list-item @click="selectAllConnector()">
            <v-list-item-title class="d-flex align-center">
              <v-checkbox
                :input-value="allEnabled"
                :ripple="false"
                readonly
                dense
                class="ma-0" />
              <span class="subtitle-1">{{ $t('search.connector.label.all') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item
            v-for="connector in sortedConnectors"
            :key="connector.name"
            class="clickable"
            dense
            @click="selectConnector(connector)">
            <v-list-item-title class="d-flex align-center">
              <v-checkbox
                :input-value="!allEnabled && connector.enabled"
                :ripple="false"
                dense
                class="ma-0" />
              <span class="subtitle-1">{{ connector.label }}</span>
            </v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
      <div v-if="!allEnabled" class="selected-connectors">
        <v-chip
          v-for="connector in enabledConnectors"
          :key="connector.name"
          color="primary"
          class="mx-1 border-color">
          <span class="text-capitalize-first-letter subtitle-1">{{ connector.label }}</span>
          <v-icon
            size="10"
            class="ms-2"
            right
            @click="selectConnector(connector)">
            fas fa-times
          </v-icon>
        </v-chip>
      </div>
    </div>
    <v-row v-if="hasResults" class="searchResultsParent justify-center justify-md-start mx-4 border-box-sizing">
      <v-col
        v-for="result in resultsArray"
        :key="result.domId"
        cols="12"
        md="6"
        lg="4"
        xl="3"
        class="searchCard pa-0">
        <search-result-card :result="result" :term="term" />
      </v-col>
    </v-row>
    <v-flex v-if="noResults" class="searchNoResultsParent d-flex my-auto border-box-sizing">
      <div class="d-flex flex-column ma-auto text-center text-sub-title">
        <div class="position-relative">
          <i class="uiIconSearchLight text-sub-title my-auto position-relative">
            <i class="uiIconCloseLight text-sub-title"></i>
          </i>
        </div>
        <span class="headline">{{ $t('Search.noResults') }}</span>
        <span class="caption">{{ $t('Search.noResultsMessage') }}</span>
      </div>
    </v-flex>
    <v-flex v-if="hasMore" class="searchLoadMoreParent d-flex my-4 border-box-sizing">
      <v-btn
        :loading="searching > 0"
        :disabled="searching > 0"
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
    standalone: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    index: 0,
    totalSize: 0,
    results: null,
    pageSize: 10,
    limit: 10,
    selectedTags: [],
    favorites: false,
    allEnabled: true,
    searching: 0,
    abortController: null,
    searchInitialized: false,
    connectorsListOpened: false,
  }),
  computed: {
    hasMore() {
      return this.totalSize && this.enabledConnectors && this.enabledConnectors.filter(connector => connector.hasMore).length;
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
    searchEnabledConnectors() {
      return this.enabledConnectors.filter(connector => {
        return (connector.favoritesEnabled || !this.favorites)
          && (connector.tagsEnabled || !this.selectedTags.length);
      });
    },
    resultsArray() {
      if (!this.results || !this.totalSize || this.searching < 0) {
        return;
      }
      const connectorNames = Object.keys(this.results);
      let results = {};
      connectorNames.forEach(connectorName => {
        if (this.enabledConnectorNames.includes(connectorName)) {
          results[connectorName] = this.results[connectorName];
        }
      });

      results = Object.values(results).flat();
      if (this.favorites) {
        results = results.filter(result => result.metadatas && result.metadatas.favorites || result.favorite || result.isFavorite);
      }
      return results.sort((a, b) => a.index - b.index);
    },
  },
  watch: {
    searching(newValue, oldValue) {
      if (newValue && !oldValue) {
        document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      } else if (oldValue && !newValue) {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      }
    },
    selectedTags() {
      this.$emit('tags-changed', this.selectedTags);
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
  },
  created() {
    // Workaround to fix closing menu when clicking outside
    $(document).on('click', (e) => {
      if (e.target && !$(e.target).parents('.connectors-list').length) {
        this.connectorsListOpened = false;
      }
    });
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
      const parameters = JSON.parse(
        `{"${decodeURI(search)
          .replace(/"/g, '\\"')
          .replace(/&/g, '","')
          .replace(/=/g, '":"')}"}`
      );
      this.favorites = parameters['favorites'] === 'true';
    }
    if (this.favorites || this.term) {
      this.search();
    } else {
      this.searchInitialized = true;
    }
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
  },
};
</script>
