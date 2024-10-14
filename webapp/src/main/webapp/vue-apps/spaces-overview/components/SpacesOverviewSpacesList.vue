<template>
  <v-list
    v-if="spaces && spaces.length"
    two-line
    subheader>
    <template v-if="filter === 'requests'">
      <template v-for="space in spaces">
        <spaces-overview-spaces-list-item
          v-for="user in space.pending"
          :key="`${user.username}_${space.id}`"
          :space="space"
          :user="user"
          :filter="filter"
          @refresh="$emit('refresh')" />
      </template>
    </template>
    <template v-else>
      <spaces-overview-spaces-list-item
        v-for="space in spaces"
        :key="space.id"
        :space="space"
        :filter="filter"
        @refresh="$emit('refresh')"
        @edit="$emit('edit', space)" />
    </template>
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