<template>
  <div>
    <exo-space-card
      :space="result"
      :profile-action-extensions="profileActionExtensions"
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
    profileActionExtensions: [],
  }),
  created() {
    this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
    this.$root.$on('editSpace', space => document.dispatchEvent(new CustomEvent('editSpaceDetail', {'detail': space})));
    this.$root.$on('displaySpaceManagers', space => document.dispatchEvent(new CustomEvent('openSpaceManagerDetail', {'detail': space})));
  },
};
</script>
