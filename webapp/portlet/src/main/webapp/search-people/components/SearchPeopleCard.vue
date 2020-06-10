<template>
  <people-card
    :user="result"
    :profile-action-extensions="profileActionExtensions"
    embedded
    @refresh="$emit('refresh')" />
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
