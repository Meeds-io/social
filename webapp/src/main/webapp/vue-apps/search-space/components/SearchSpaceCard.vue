<template>
  <div class="space-search-card">
    <space-card
      :min-width="cardMinWidth"
      :max-width="cardWidth"
      :height="cardHeight"
      :min-height="cardHeight"
      :class="{'full-width': isMobile}"
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
    cardMinWidth() {
      return  this.isMobile && 220 || this.cardWidth;
    },
    isMobile() {
      return this.$vuetify.breakpoint.smAndDown;
    }
  },
  created() {
    document.addEventListener('extension-profile-extension-action-updated', this.refreshExtensions);
    this.refreshExtensions();
  },
  beforeDestroy() {
    document.removeEventListener('extension-profile-extension-action-updated', this.refreshExtensions);
  },
  methods: {
    refreshExtensions() {
      this.spaceActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
    },
  }
};
</script>
