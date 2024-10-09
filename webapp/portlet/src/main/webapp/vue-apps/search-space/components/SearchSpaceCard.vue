<template>
  <div class="space-search-card">
    <space-card
      :space="result"
      :space-action-extensions="spaceActionExtensions"
      embedded
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
  }),
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
