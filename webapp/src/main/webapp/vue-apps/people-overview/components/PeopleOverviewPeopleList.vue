<template>
  <v-list
    v-if="users && users.length"
    subheader
    two-line>
    <people-overview-people-list-item
      v-for="user in users"
      :key="user.id"
      :filter="filter"
      :user="user"
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