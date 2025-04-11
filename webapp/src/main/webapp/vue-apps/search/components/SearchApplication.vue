<template>
  <v-app role="search">
    <v-btn
      :title="buttonTooltip"
      icon
      class="transparent"
      @click="$refs.drawer.toogle()">
      <v-icon size="20">fa-search</v-icon>
    </v-btn>
    <search-drawer
      ref="drawer"
      :connectors="connectors"
      :skin-urls="skinUrls" />
  </v-app>
</template>
<script>
export default {
  props: {
    connectors: {
      type: Array,
      default: () => [],
    },
    skinUrls: {
      type: Array,
      default: () => [],
    },
  },
};
</script>
