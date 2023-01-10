<template>
  <v-app class="white">
    <v-card-text v-if="error" class="errorMessage">
      <v-alert type="error">
        {{ error }}
      </v-alert>
    </v-card-text>
    <v-card class="d-flex flex py-2" flat>
      <v-flex class="sm12 md4" flat>
        <groups-management-tree-toolbar />
        <groups-management-tree />
      </v-flex>
      <v-divider vertical />
      <v-flex class="sm12 md8" flat>
        <groups-management-membership-toolbar />
        <groups-management-membership-list />
      </v-flex>
    </v-card>
    <groups-management-form-drawer />
    <groups-management-membership-form-drawer />
  </v-app>
</template>

<script>
export default {
  data: () => ({
    error: null,
  }),
  mounted() {
    this.$root.$on('error', error => this.error = error);
  },
};
</script>