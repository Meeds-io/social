<template>
  <v-navigation-drawer
    ref="thirdLevelDrawer"
    v-model="drawer"
    :width="drawerWidth"
    :style="`left: ${drawerLeft}px;`"
    max-width="100%"
    hide-overlay>
    <template v-if="drawer">
      <space-panel-hamburger-navigation
        :display-sequentially="displaySequentially"
        :space="openedSpace"
        :home-link="homeLink"
        :opened-space="openedSpace"
        @close="drawer = false" />
    </template>
  </v-navigation-drawer>
</template>
<script>
export default {
  props: {
    value: {
      type: Boolean,
      default: false,
    },
    displaySequentially: {
      type: Boolean,
      default: false,
    },
    openedSpace: {
      type: Object,
      default: null,
    },
    drawerWidth: {
      type: Number,
      default: null,
    },
    homeLink: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
  }),
  computed: {
    drawerLeft() {
      return this.displaySequentially && this.drawerWidth * 2 || 0;
    },
  },
  watch: {
    drawer() {
      this.$emit('input', this.drawer);
    },
    value() {
      this.drawer = this.value;
    },
  },
  created() {
    this.drawer = this.value;
  },
};
</script>
