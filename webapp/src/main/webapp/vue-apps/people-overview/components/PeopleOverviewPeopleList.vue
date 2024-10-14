<template>
  <v-list
    v-if="users && users.length"
    two-line
    subheader>
    <people-overview-people-list-item
      v-for="user in users"
      :key="user.id"
      :user="user"
      :filter="filter"
      @refresh="$emit('refresh')" />
  </v-list>
</template>

<script>
export default {
  props: {
    users: {
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