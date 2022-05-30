<template>
  <div>
    <popover-user v-if="!isMobile" />
  </div>
</template>
<script>
export default {
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
  },
  mounted() {
    this.$root.$applicationLoaded();
  },
};
</script>