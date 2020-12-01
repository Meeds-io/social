<template>
  <v-app 
    id="spacesListApplication"
    class="transparent"
    flat>
    <exo-spaces-toolbar
      :keyword="keyword"
      :filter="filter"
      :spaces-size="spacesSize"
      :can-create-space="canCreateSpace"
      :skeleton="skeleton"
      @keyword-changed="keyword = $event"
      @filter-changed="filter = $event" />
    <exo-spaces-card-list
      ref="spacesList"
      :keyword="keyword"
      :filter="filter"
      :loading-spaces="loadingSpaces"
      :skeleton="skeleton"
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
  },
  data: () => ({
    keyword: null,
    spacesSize: 0,
    loadingSpaces: false,
    skeleton: true,
  }),
  methods: {
    spacesLoaded(spacesSize) {
      document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      this.spacesSize = spacesSize;
      if (this.skeleton) {
        this.$root.$emit('application-loaded');
        this.skeleton = false;
      }
    }
  },
};
</script>

