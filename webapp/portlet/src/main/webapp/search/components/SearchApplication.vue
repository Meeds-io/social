<template>
  <v-app>
    <v-btn icon class="transparent" @click="dialog = !dialog">
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
  }),
  watch: {
    dialog() {
      if (this.dialog) {
        $('body').addClass('hide-scroll transparent');
        this.$root.$emit('search-opened');
      } else {
        $('body').removeClass('hide-scroll transparent');
        this.$root.$emit('search-closed');
      }
    },
  },
  created() {
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
    const pathParts = window.location.pathname.split('/');
    if (pathParts && pathParts.length && pathParts[pathParts.length - 1] === 'search') {
      this.standalone = true;
    } else {
      $(document).on('keydown', (event) => {
        if (event.key === 'Escape') {
          this.dialog = false;
        }
      });
    }
  },
};
</script>
