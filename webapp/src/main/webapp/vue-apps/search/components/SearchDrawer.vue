<template>
  <exo-drawer
    id="searchDialog"
    v-model="drawer"
    :loading="loading"
    expanded
    right>
    <template v-if="drawer" #content>
      <v-card
        :loading="loading"
        flat>
        <search-toolbar
          ref="toolbar"
          v-model="term"
          :standalone="standalone"
          @close="drawer = false" />
        <search-results
          ref="results"
          :connectors="connectors"
          :term="term"
          :standalone="standalone"
          @loading="loading = $event"
          @favorites-changed="favorites = $event"
          @tags-changed="selectedTags = $event"
          @filter-changed="changeURI" />
      </v-card>
    </template>
  </exo-drawer>
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
    drawer: false,
    loading: false,
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
    standalone: {
      immediate: true,
      handler() {
        if (this.standalone) {
          this.drawer = this.standalone;
        }
      },
    },
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
        this.$root.$applicationLoaded();
      }
    },
    drawer() {
      if (this.drawer) {
        this.$root.$emit('search-opened');
        document.dispatchEvent(new CustomEvent('search-opened'));
        this.changeURI();
      } else {
        this.$root.$emit('search-closed');
        document.dispatchEvent(new CustomEvent('search-closed'));
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
    const urls = [`/social/i18n/locale.portlet.Portlets?lang=${lang}`];
    if (this.connectors && this.connectors.length) {
      this.connectors.forEach(connector => {
        if (connector.i18nBundle) {
          urls.push(`${basePath}/i18n/bundle/${connector.i18nBundle}-${lang}.json`);
        }
      });
    }
    this.loading = true;
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
          this.drawer = false;
        }
      });
    }
    document.addEventListener('search-open', this.open);
    document.addEventListener('search-metadata-tag', this.open);
  },
  mounted() {
    this.drawer = true;
  },
  beforeDestroy() {
    document.removeEventListener('search-open', this.open);
    document.removeEventListener('search-metadata-tag', this.open);
  },
  methods: {
    toogle() {
      this.drawer = !this.drawer;
    },
    open() {
      this.drawer = true;
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
    },
  },
};
</script>
