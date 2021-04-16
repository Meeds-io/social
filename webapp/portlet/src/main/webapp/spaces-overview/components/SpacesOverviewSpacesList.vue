<template>
  <v-list
    v-if="spaces && spaces.length"
    two-line
    subheader>
    <spaces-overview-spaces-list-item
      v-for="space in spaces"
      :key="space.id"
      :space="space"
      :filter="filter"
      @refresh="$emit('refresh')"
      @edit="$emit('edit', space)" />
  </v-list>
</template>

<script>
export default {
  props: {
    spaces: {
      type: Array,
      default: () => null,
    },
    filter: {
      type: String,
      default: () => null,
    },
  },
};
</script>