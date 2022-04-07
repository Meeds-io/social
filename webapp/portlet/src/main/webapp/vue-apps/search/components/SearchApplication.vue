<template>
  <v-app role="search">
    <v-btn
      :title="buttonTooltip"
      icon
      class="transparent"
      @click="dialog = !dialog">
      <i class="v-icon fas fa-search icon-medium-size icon-default-color position-static d-flex"></i>
    </v-btn>
    <v-fade-transition>
      <v-flex
        v-show="dialog || standalone"
        id="searchDialog"
        transition="fade-transition"
        hide-overlay>
        <v-card flat>
          <template v-if="!loading">
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
              @favorites-changed="favorites = $event"
              @tags-changed="selectedTags = $event"
              @filter-changed="changeURI" />
          </template>
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
    skinUrls: {
      type: Array,
      default: () => [],
    },
  },
  data: () => ({
    dialog: false,
    loading: true,
    term: null,
    favorites: false,
    selectedTags: [],
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
    favorites() {
      this.changeURI();
    },
    selectedTags() {
      this.changeURI();
    },
    loading() {
      if (!this.loading) {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
        this.$root.$applicationLoaded();
      }
    },
    dialog() {
      if (this.dialog) {
        $('body').addClass('hide-scroll');
        this.$root.$emit('search-opened');
        this.changeURI();
      } else {
        $('body').removeClass('hide-scroll');
        this.$root.$emit('search-closed');
        window.history.replaceState('', this.pageTitle, this.pageUri);
      }
    },
  },
  created() {
    if (this.skinUrls && this.skinUrls.length) {
      this.skinUrls.forEach(skinUrl => {
        if (!document.querySelector(`link[href="${skinUrl}"]`)) {
          const link = document.createElement('link');
          link.type = 'text/css';
          link.rel = 'stylesheet';
          link.href = skinUrl;
          document.head.appendChild(link);
        }
      });
    }

    const lang = eXo.env.portal.language;
    const basePath = `${eXo.env.portal.context}/${eXo.env.portal.rest}`;
    const urls = [`${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Portlets-${lang}.json`];
    if (this.connectors && this.connectors.length) {
      this.connectors.forEach(connector => {
        if (connector.i18nBundle) {
          urls.push(`${basePath}/i18n/bundle/${connector.i18nBundle}-${lang}.json`);
        }
      });
    }
    exoi18n.loadLanguageAsync(lang, urls)
      .then(() => this.$nextTick())
      .finally(() => this.loading = false);

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
        this.term = parameters['q'] || '';
        this.favorites = parameters['favorites'] === 'true';
        this.selectedTags = parameters['tags'] && parameters['tags'].split(',') || [];
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
    document.addEventListener('search-metadata-tag', this.open);
  },
  mounted() {
    this.dialog = true;
  },
  methods: {
    open() {
      this.dialog = true;
    },
    changeURI() {
      const term = window.encodeURIComponent(this.term || '');
      const enabledConnectorNames = this.connectors.filter(connector => connector.enabled).map(connector => connector.name);
      let enabledConnectorsParam = '';
      if (enabledConnectorNames.length !== this.connectors.length) {
        enabledConnectorsParam = window.encodeURIComponent(enabledConnectorNames.join(','));
      }
      let pageUri = `${this.searchUri}?q=${term}&types=${enabledConnectorsParam}`;
      if (this.favorites) {
        pageUri += '&favorites=true';
      }
      if (this.selectedTags && this.selectedTags.length) {
        pageUri += `&tags=${this.selectedTags.join(',')}`;
      }
      window.history.replaceState('', this.$t('Search.page.title'), pageUri);
    }
  },
};
</script>
