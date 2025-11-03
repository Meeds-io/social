<template>
  <div class="space-search-card">
    <space-card
      :min-width="cardWidth"
      :max-width="cardWidth"
      :height="cardHeight"
      :min-height="cardHeight"
      :space="result"
      :space-action-extensions="spaceActionExtensions"
      display-members-count
      hide-space-description
      class="pa-0"
      @refresh="$emit('refresh')" />
  </div>
</template>
<script>
export default {
  props: {
    term: {
      type: String,
      default: null,
    },
    result: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    spaceActionExtensions: [],
    cardWidth: 400,
    cardHeight: 130,
  }),
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.smAndDown;
    }
  },
  created() {
    document.addEventListener('extension-profile-extension-action-updated', this.refreshExtensions);
    this.refreshExtensions();
    this.$root.$on('spaces-list-refresh', this.emitRefresh);
  },
  beforeDestroy() {
    document.removeEventListener('extension-profile-extension-action-updated', this.refreshExtensions);
    this.$root.$off('spaces-list-refresh', this.emitRefresh);
  },
  methods: {
    refreshExtensions() {
      this.spaceActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
    },
    emitRefresh() {
      this.$emit('refresh');
    }
  }
};
</script>
