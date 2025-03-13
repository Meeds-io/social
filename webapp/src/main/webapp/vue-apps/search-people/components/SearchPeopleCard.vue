<template>
  <people-card
    embedded
    :mobile-display="isMobile"
    :profile-action-extensions="profileActionExtensions"
    :user="result"
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
    computed: {
      isMobile () {
        return eXo.vuetify.display.mobile.value;
      },
    },
    created () {
      this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
    },
  };
</script>
