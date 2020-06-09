<template>
  <v-row v-if="resultsArray" class="searchResultsParent mx-4 border-box-sizing">
    <v-col
      v-for="(result, index) in resultsArray"
      :key="index"
      cols="12"
      md="6"
      lg="4"
      xl="3"
      class="searchCard pa-0">
      <search-result-card :result="result" :term="term" />
    </v-col>
  </v-row>
</template>

<script>
export default {
  props: {
    term: {
      type: String,
      default: null,
    },
    pageSize: {
      type: Number,
      default: 10,
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
    searching: 0,
    abortController: null,
  }),
  computed: {
    resultsArray() {
      return this.results && this.totalSize ? Object.values(this.results).flat() : null;
    },
  },
  watch: {
    term() {
      if (this.term) {
        this.results = {};
        this.totalSize = 0;
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
  methods: {
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

      this.connectors.forEach(searchConnector => {
        if (!searchConnector.enabled
            || !searchConnector.uri
            || searchConnector.size >= 0 && searchConnector.size < this.pageSize) {
          return;
        }

        window.require([searchConnector.jsModule], connectorModule => {
          const options = Object.assign({headers: {
            Accept: 'application/json',
          }}, signal);
          if (searchConnector.uri.indexOf('/') === 0) {
            options.credentials = 'include';
          } else {
            options.referrerPolicy = 'no-referrer';
            options.mode = 'no-cors';
          }
          this.searching++;
          const uri = searchConnector.uri
            .replace('{keyword}', this.term)
            .replace('{limit}', this.pageSize);
          fetch(uri, options)
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
                resultArray.forEach(result => {
                  result.connector = searchConnector;
                });
                this.$set(this.results, searchConnector.name, resultArray);
                this.totalSize = this.results[searchConnector.name].length;
                this.$forceUpdate();
              }
            })
            .catch(e => searchConnector.error = e)
            .finally(() => this.searching--);
        });
      });
    },
  },
};
</script>
