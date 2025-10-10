<template>
  <exo-drawer
    id="searchDialog"
    v-model="drawer"
    :loading="loading"
    expanded
    right>
    <template #title>
      {{ $t("Search.page.title") }}
    </template>
    <template v-if="drawer" #content>
      <v-card
        :class="!$root.isMobile && 'pa-4' || ''"
        class="d-flex flex-column light-grey-background-color"
        min-height="100%"
        flat>
        <v-card
          :class="!$root.isMobile && 'singlePageApplication card-border-radius' || ''"
          class="pa-0 flex-grow-1 d-flex flex-column fill-height white overflow-hidden"
          flat>
          <search-toolbar
            ref="toolbar"
            v-model="term"
            :standalone="standalone" />
          <search-results
            ref="results"
            :connectors="connectors"
            :term="term"
            @loading="loading = $event"
            @favorites-changed="favorites = $event"
            @tags-changed="selectedTags = $event"
            @filter-changed="changeURI" />
        </v-card>
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
    selectedSpaces: [],
    standalone: false,
    pageUri: null,
    pageTitle: null,
    companyName: eXo?.env?.portal?.companyName
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
      if (!this.loading) {
        this.setWindowTitle();
      }
    },
    favorites() {
      this.changeURI();
    },
    selectedTags() {
      this.changeURI();
    },
    selectedSpaces() {
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
        this.setWindowTitle();
      } else {
        this.$root.$emit('search-closed');
        document.dispatchEvent(new CustomEvent('search-closed'));
        window.history.replaceState('', this.pageTitle, this.pageUri);
        window.document.title = this.pageTitle || `${this.$t('search.window.title')} - ${this.companyName}`;
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
      .finally(() => {
        this.loading = false;
        this.setWindowTitle();
      });

    this.pageUri = window.location.href;
    this.pageTitle = window.document.title;

    this.standalone = window.location.pathname.indexOf(this.searchUri) === 0;
    if (this.standalone) {
      const search = window.location.search && window.location.search.substring(1);
      if (search) {
        const parameters = new URLSearchParams(search);
        const selectedTypes = window.decodeURIComponent(parameters?.get('types'));
        if (selectedTypes && selectedTypes.trim().length) {
          this.connectors.forEach(connector => {
            connector.enabled = selectedTypes.includes(connector.name);
          });
        }
        this.term = parameters?.get('q') || '';
        this.favorites = parameters?.get('favorites') === 'true';
        this.selectedTags = parameters?.get('tags')?.split(',') || [];
        this.selectedSpaces = parameters.getAll('spaceId') || [];
      }
    }
    this.$root.$on('spaces-changed', this.setSelectedSpaces);
    document.addEventListener('search-open', this.open);
    document.addEventListener('search-metadata-tag', this.open);
  },
  mounted() {
    this.drawer = true;
  },
  beforeDestroy() {
    this.$root.$off('spaces-changed', this.setSelectedSpaces);
    document.removeEventListener('search-open', this.open);
    document.removeEventListener('search-metadata-tag', this.open);
  },
  methods: {
    toogle() {
      this.drawer = !this.drawer;
    },
    open(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
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
      if (this.selectedSpaces && this.selectedSpaces.length) {
        this.selectedSpaces.forEach(id => {
          pageUri += `&spaceId=${id}`;
        });
      }
      window.history.replaceState('', this.$t('Search.page.title'), pageUri);
    },
    setWindowTitle() {
      const termTitle = this.term ? `${this.term} - ` : '';
      const searchWindowTitle = `${termTitle}${this.$t('search.window.title')} - ${this.companyName}`;
      window.document.title = searchWindowTitle;
    },
    setSelectedSpaces(selectedSpaces) {
      this.selectedSpaces = selectedSpaces || [];
    }
  },
};
</script>
