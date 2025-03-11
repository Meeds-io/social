<template>
  <v-list
    v-if="spaces && spaces.length"
    subheader
    two-line>
    <template v-if="filter === 'requests'">
      <template v-for="space in spaces">
        <spaces-overview-spaces-list-item
          v-for="user in space.pending"
          :key="`${user.username}_${space.id}`"
          :filter="filter"
          :space="space"
          :user="user"
          @refresh="$emit('refresh')" />
      </template>
    </template>
    <template v-else>
      <spaces-overview-spaces-list-item
        v-for="space in spaces"
        :key="space.id"
        :filter="filter"
        :space="space"
        @edit="$emit('edit', space)"
        @refresh="$emit('refresh')" />
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