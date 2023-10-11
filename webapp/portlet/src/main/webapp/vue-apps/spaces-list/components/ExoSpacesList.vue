<template>
  <v-app class="transparent card-border-radius overflow-hidden" flat>
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
      @loading-spaces="loadingSpaces = $event"
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
        this.$root.$applicationLoaded();
        this.initialized = true;
      }
    }
  },
};
</script>

