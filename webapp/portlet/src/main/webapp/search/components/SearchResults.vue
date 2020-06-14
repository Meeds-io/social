<template>
  <v-flex>
    <v-flex class="searchConnectorsParent mx-4 mb-4 border-box-sizing">
      <v-chip
        v-for="connector in connectors"
        :key="connector.name"
        :outlined="!connector.enabled"
        :color="connector.enabled ? 'primary' : ''"
        class="mx-1 border-color"
        @click="selectConnector(connector)">
        <span class="subtitle-1">{{ $t(`search.connector.label.${connector.name}`) }}</span>
      </v-chip>
    </v-flex>
    <v-row v-if="resultsArray" class="searchResultsParent mx-4 border-box-sizing">
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
  },
  data: () => ({
    term: null,
    totalSize: 0,
    results: null,
    pageSize: 10,
    limit: 10,
    searching: 0,
    abortController: null,
  }),
  computed: {
    hasMore() {
      return this.totalSize && this.enabledConnectors && this.enabledConnectors.filter(connector => connector.hasMore).length;
    },
    enabledConnectors() {
      return this.connectors && this.connectors.filter(connector => connector.enabled) || [];
    },
    enabledConnectorNames() {
      return this.enabledConnectors.map(connector => connector.name);
    },
    resultsArray() {
      if (!this.results || !this.totalSize || this.searching < 0) {
        return;
      }
      const connectorNames = Object.keys(this.results);
      const results = {};
      connectorNames.forEach(connectorName => {
        if (this.enabledConnectorNames.includes(connectorName)) {
          results[connectorName] = this.results[connectorName];
        }
      });
      return Object.values(results).flat();
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
    term() {
      this.totalSize = 0;
      this.limit = this.pageSize;
      if (this.term) {
        this.results = {};
      }
      this.connectors.forEach(connector => {
        connector.size = -1;
        if (this.term) {
          this.results[connector.name] = [];
        }
      });

      this.search();
    },
  },
  created() {
    this.$root.$on('refresh', this.retrieveConnectorResults);
  },
  methods: {
    selectConnector(selectedConnector) {
      if (!selectedConnector) {
        return;
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
      return this.$nextTick().then(this.search);
    },
    loadMore() {
      this.limit += this.pageSize;
      this.search();
    },
    search() {
      if (this.abortController) {
        this.abortController.abort();
      }
      if (!this.term) {
        this.results = null;
        return;
      }
      let signal = {};
      if (window.AbortController) {
        this.abortController = new window.AbortController();
        signal = this.abortController.signal;
      }

      this.enabledConnectors.forEach(searchConnector => {
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
        let options = {headers: {
          Accept: 'application/json',
        }};
        if (signal) {
          options = Object.assign(options, signal);
        }
        if (searchConnector.uri.indexOf('/') === 0) {
          options.credentials = 'include';
        } else {
          options.referrerPolicy = 'no-referrer';
          options.mode = 'no-cors';
        }
        this.searching++;
        const uri = searchConnector.uri
          .replace('{keyword}', window.encodeURIComponent(this.term))
          .replace('{limit}', this.limit);
        return fetch(uri, options)
          .then(resp => {
            if (resp && resp.ok) {
              return resp.json();
            }
          })
          .then(result => {
            if (connectorModule && connectorModule.formatSearchResult) {
              return connectorModule.formatSearchResult(result);
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
                result.domId = `SearchResult${String(parseInt(Math.random() * 100000))}`;
              });
              this.$set(this.results, searchConnector.name, resultArray);
              this.totalSize = this.results[searchConnector.name].length;
              this.$forceUpdate();
            }
          })
          .catch(e => searchConnector.error = e)
          .finally(() => this.searching--);
      });
    },
  },
};
</script>
