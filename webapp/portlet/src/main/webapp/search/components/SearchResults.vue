<template>
  <v-flex>
  </v-flex>
</template>

<script>
export default {
  props: {
    term: {
      type: String,
      default: null,
    },
    connectors: {
      type: Array,
      default: () => [],
    },
  },
  data: () => ({
    term: null,
    searching: 0,
  }),
  watch: {
    term() {
      if (this.term) {
        // TODO
      } else {
        // TODO
      }
    },
  },
};
</script>
