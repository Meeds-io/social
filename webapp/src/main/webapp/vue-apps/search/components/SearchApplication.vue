<template>
  <v-app role="search">
    <v-btn
      :title="buttonTooltip"
      icon
      class="transparent"
      @click="$refs.dialog.toogle()">
      <v-icon size="20">fa-search</v-icon>
    </v-btn>
    <search-dialog
      ref="dialog"
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
  data: () => ({
    dialog: false,
  }),
};
</script>
