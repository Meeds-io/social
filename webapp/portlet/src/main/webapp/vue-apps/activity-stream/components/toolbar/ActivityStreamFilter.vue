<template>
  <v-flex class="d-flex content-box-sizing justify-end">
    <select
      v-model="streamFilter"
      class="width-auto my-auto ignore-vuetify-classes"
      @change="applyFilter">
      <option value="all">All</option>
      <option value="myActivities">My posted activities</option>
      <option value="manageSpaces">Spaces I manage</option>
      <option value="favoriteSpaces">My favorites spaces</option>
    </select>
  </v-flex>
</template>
<script>
export default {
  props: {
    streamFilter: {
      type: String,
      default: () => 'all',
    },
  },
  methods: {
    applyFilter() {
      document.dispatchEvent(new CustomEvent('filter-applied', {detail: this.streamFilter}));
    },
  },
};
</script>