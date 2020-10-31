<template>
  <v-app class="transparent" flat>
    <exo-spaces-toolbar
      :keyword="keyword"
      :filter="filter"
      :spaces-size="spacesSize"
      :can-create-space="canCreateSpace"
      @keyword-changed="keyword = $event"
      @filter-changed="filter = $event" />
    <exo-spaces-card-list
      ref="spacesList"
      :keyword="keyword"
      :filter="filter"
      :loading-spaces="loadingSpaces"
      :spaces-size="spacesSize"
      @loaded="spacesLoaded" />

    <exo-space-managers-drawer />
    <exo-space-form-drawer />
  </v-app>    
</template>

<script>

export default {
  props: {
    canCreateSpace: {
      type: Boolean,
      default: false,
    },
    filter: {
      type: String,
      default: null,
    },
    appId: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    keyword: null,
    spacesSize: 0,
    loadingSpaces: false,
    initialized: false,
  }),
  methods: {
    spacesLoaded(spacesSize) {
      this.spacesSize = spacesSize;
      if (!this.initialized) {
        this.$root.$emit('application-loaded');
        this.initialized = true;
      }
    }
  },
};
</script>

