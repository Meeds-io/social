<template>
  <v-app>
    <v-btn
      :title="buttonTooltip"
      icon
      class="transparent"
      @click="dialog = !dialog">
      <i class="uiIconPLF24x24Search" />
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
            @close-search="dialog = false"/>
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
  },
  watch: {
    term() {
      this.changeURI();
    },
    dialog() {
      if (this.dialog) {
        $('body').addClass('hide-scroll transparent');
        this.$root.$emit('search-opened');
        window.history.replaceState('', this.$t('Search.page.title'), `/${eXo.env.portal.containerName}/${eXo.env.portal.portalName}/search?q=${this.term || ''}`);
      } else {
        $('body').removeClass('hide-scroll transparent');
        this.$root.$emit('search-closed');
        window.history.replaceState('', this.pageTitle, this.pageUri);
      }
    },
  },
  created() {
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));

    this.pageUri = window.location.href;
    this.pageTitle = window.document.title;

    const pathParts = window.location.pathname.split('/');
    if (pathParts && pathParts.length && pathParts[pathParts.length - 1] === 'search') {
      this.standalone = true;

      const search = window.location.search && window.location.search.substring(1);
      if(search) {
        const parameters = JSON.parse(
          `{"${decodeURI(search)
            .replace(/"/g, '\\"')
            .replace(/&/g, '","')
            .replace(/=/g, '":"')}"}`
        );
        const selectedTypes = window.decodeURIComponent(parameters['types']);
        if (selectedTypes && selectedTypes.length) {
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
  methods: {
    changeURI() {
      const term = window.encodeURIComponent(this.term || '');
      const enabledConnectorNames = this.connectors.filter(connector => connector.enabled).map(connector => connector.name);
      let enabledConnectorsParam = '';
      if (enabledConnectorNames.length !== this.connectors.length) {
        enabledConnectorsParam = window.encodeURIComponent(enabledConnectorNames.join(','));
      }
      window.history.replaceState('', this.$t('Search.page.title'), `/${eXo.env.portal.containerName}/${eXo.env.portal.portalName}/search?q=${term}&types=${enabledConnectorsParam}`);
    }
  },
};
</script>
