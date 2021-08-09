<template>
  <v-app>
    <v-btn
      :title="buttonTooltip"
      icon
      class="transparent"
      @click="dialog = !dialog">
      <i class="uiIconPLF24x24Search"></i>
    </v-btn>
    <v-fade-transition>
      <v-flex
        v-show="dialog || standalone"
        id="searchDialog"
        transition="fade-transition"
        hide-overlay>
        <v-card flat>
          <search-toolbar
            ref="toolbar"
            :standalone="standalone"
            @search="term = $event"
            @close-search="dialog = false" />
          <search-results
            ref="results"
            :connectors="connectors"
            :term="term"
            :standalone="standalone"
            @filter-changed="changeURI" />
        </v-card>
      </v-flex>
    </v-fade-transition>
  </v-app>
</template>

<script>
export default {
  props: {
    connectors: {
      type: Array,
      default: () => [],
    },
  },
  data: () => ({
    dialog: false,
    term: null,
    standalone: false,
    pageUri: null,
    pageTitle: null,
  }),
  computed: {
    buttonTooltip() {
      return this.$t('Search.button.tooltip.open', {0: 'Ctrl + Alt + F'});
    },
    searchUri() {
      return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/search`;
    },
  },
  watch: {
    term() {
      this.changeURI();
    },
    dialog() {
      if (this.dialog) {
        $('body').addClass('hide-scroll');
        this.$root.$emit('search-opened');
        window.history.replaceState('', this.$t('Search.page.title'), `${this.searchUri}?q=${this.term || ''}`);
      } else {
        $('body').removeClass('hide-scroll');
        this.$root.$emit('search-closed');
        window.history.replaceState('', this.pageTitle, this.pageUri);
      }
    },
  },
  created() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));

    this.pageUri = window.location.href;
    this.pageTitle = window.document.title;

    this.standalone = window.location.pathname.indexOf(this.searchUri) === 0;
    if (this.standalone) {
      const search = window.location.search && window.location.search.substring(1);
      if (search) {
        const parameters = JSON.parse(
          `{"${decodeURI(search)
            .replace(/"/g, '\\"')
            .replace(/&/g, '","')
            .replace(/=/g, '":"')}"}`
        );
        const selectedTypes = parameters['types'] && window.decodeURIComponent(parameters['types']);
        if (selectedTypes && selectedTypes.trim().length) {
          this.connectors.forEach(connector => {
            connector.enabled = selectedTypes.includes(connector.name);
          });
        }
      }
    } else {
      $(document).on('keydown', (event) => {
        if (event.key === 'Escape') {
          this.dialog = false;
        }
        if (event.ctrlKey && event.altKey && event.key === 'f') {
          this.dialog = !this.dialog;
        }
      });
    }
  },
  mounted() {
    this.$root.$applicationLoaded();
  },
  methods: {
    changeURI() {
      const term = window.encodeURIComponent(this.term || '');
      const enabledConnectorNames = this.connectors.filter(connector => connector.enabled).map(connector => connector.name);
      let enabledConnectorsParam = '';
      if (enabledConnectorNames.length !== this.connectors.length) {
        enabledConnectorsParam = window.encodeURIComponent(enabledConnectorNames.join(','));
      }
      window.history.replaceState('', this.$t('Search.page.title'), `${this.searchUri}?q=${term}&types=${enabledConnectorsParam}`);
    }
  },
};
</script>
