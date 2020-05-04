<template>
  <v-app class="spaceMenuParent white">
    <v-tabs
      v-model="selectedNavigationUri"
      class="mx-auto"
      active-class="SelectedTab"
      center-active
      show-arrows
      slider-size="4">
      <v-tab
        v-for="nav in navigations"
        :key="nav.id"
        :value="nav.id"
        :href="nav.uri">
        <a :href="nav.uri" style="color: inherit;">
          {{ nav.label }}
        </a>
      </v-tab>
    </v-tabs>
  </v-app>
</template>

<script>
export default {
  props: {
    navigations: {
      type: Array,
      default: () => [],
    },
    selectedNavigationUri: {
      type: String,
      default: null,
    },
  },
  created() {
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
  },
};
</script>