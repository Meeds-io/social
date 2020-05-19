<template>
  <v-flex>
    <div
      v-for="app in applicationItems"
      :key="app.name">
      {{ app.name }}
    </div>
  </v-flex>
</template>

<script>
export default {
  props: {
    applicationItems: {
      type: Array,
      default: () => [],
    },
  },
};
</script>