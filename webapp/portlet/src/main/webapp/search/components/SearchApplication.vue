<template>
  <div>
    <v-btn icon class="transparent" @click="dialog = !dialog">
      <i class="uiIconPLF24x24Search" />
    </v-btn>
    <v-flex
      v-show="dialog || standalone"
      id="searchDialog"
      transition="fade-transition"
      hide-overlay>
      <v-card>
        
      </v-card>
    </v-flex>
  </div>
</template>

<script>
export default {
  props: {
    connectors: {
      type: Object,
      default: () => ({}),
    },
  },
  data: () => ({
    dialog: false,
    standalone: false,
  }),
  watch: {
    dialog() {
      if (this.dialog) {
        $('body').addClass('hide-scroll');
      } else {
        window.setTimeout(() => {
          $('body').removeClass('hide-scroll');
        }, 200);
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
