<template>
  <v-expansion-panel
    v-if="filteredApplications && filteredApplications.length"
    class="border-color border-radius mt-4">
    <v-expansion-panel-header>
      {{ categoryName }}
    </v-expansion-panel-header>
    <v-divider v-if="expanded" />
    <v-expansion-panel-content>
      <div
        v-for="app in filteredApplications"
        :key="app.id"
        class="d-flex flex-no-wrap justify-space-between border-radius border-color SpaceApplicationCard SpaceApplicationCardEmbedded">
        <exo-space-application-card
          :application="app"
          class="flex-grow-1"
          @add="$emit('addApplication', app)" />
      </div>
    </v-expansion-panel-content>
  </v-expansion-panel>
</template>

<script>
export default {
  props: {
    expanded: {
      type: Boolean,
      default: false,
    },
    category: {
      type: Object,
      default: null,
    },
    installedApplications: {
      type: Array,
      default: () => [],
    },
  },
  computed: {
    filteredApplications() {
      if (!this.category || !this.category.applications || !this.category.applications.length) {
        return [];
      }
      return this.category.applications.slice().filter(app => !this.installedApplications.find(installedApp => installedApp.id === app.applicationName));
    },
    categoryName() {
      return this.category && this.category.name;
    },
  },
};
</script>

