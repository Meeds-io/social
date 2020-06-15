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
            :term="term" />
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
      const term = window.encodeURIComponent(this.term || '');
      window.history.replaceState('', this.$t('Search.page.title'), `/${eXo.env.portal.containerName}/${eXo.env.portal.portalName}/search?q=${term}`);
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
};
</script>
