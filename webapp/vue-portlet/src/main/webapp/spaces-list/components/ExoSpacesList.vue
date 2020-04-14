<template>
  <v-app 
    id="spacesListApplication"
    class="transparent"
    flat>
    <exo-spaces-toolbar
      :keyword="keyword"
      :filter="filter"
      :spaces-size="spacesSize"
      :skeleton="firstLoadingSpaces"
      @keyword-changed="keyword = $event"
      @filter-changed="filter = $event" />
    <exo-spaces-card-list
      ref="spacesList"
      :keyword="keyword"
      :filter="filter"
      :loading-spaces="loadingSpaces"
      :skeleton="firstLoadingSpaces"
      :spaces-size="spacesSize"
      @loaded="spacesLoaded" />

    <exo-space-managers-drawer />
    <exo-space-form-drawer />
  </v-app>    
</template>

<script>

export default {
  props: {
    appId: {
      type: String,
      default: null,
    },
    filter: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    keyword: null,
    spacesSize: 0,
    loadingSpaces: false,
    firstLoadingSpaces: true,
  }),
  methods: {
    spacesLoaded(spacesSize) {
      this.spacesSize = spacesSize;
      if (this.firstLoadingSpaces) {
        this.firstLoadingSpaces = false;
      }
    }
  },
};
</script>

