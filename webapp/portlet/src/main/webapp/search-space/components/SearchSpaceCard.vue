<template>
  <div>
    <exo-space-card
      :space="result"
      :profile-action-extensions="profileActionExtensions"
      embedded
      @refresh="$emit('refresh')" />

    <exo-space-managers-drawer />
    <exo-space-form-drawer />
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
  },
};
</script>
