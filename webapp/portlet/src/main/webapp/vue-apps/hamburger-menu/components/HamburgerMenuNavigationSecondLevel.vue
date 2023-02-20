<template>
  <!-- Added after third level to make the drawer close animation smooth -->
  <v-navigation-drawer
    ref="secondLevelDrawer"
    v-model="drawer"
    :width="drawerWidth"
    :style="`left: ${drawerLeft}px;`"
    max-width="100%"
    hide-overlay>
    <template v-if="drawer">
      <recent-spaces-hamburger-navigation
        v-if="secondLevel === 'recentSpaces'"
        :display-sequentially="displaySequentially"
        :opened-space="thirdLevelDrawer && openedSpace"
        @close="drawer = false" />
      <administration-navigations
        v-else-if="hasAdministrationNavigations && secondLevel === 'administration'"
        :display-sequentially="displaySequentially"
        :navigations="administrationNavigations"
        :categories="administrationCategories"
        @close="drawer = false" />
      <space-panel-hamburger-navigation
        v-else-if="secondLevel === 'spaceMenu'"
        :display-sequentially="displaySequentially"
        :space="openedSpace"
        :home-link="homeLink"
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
    thirdLevelDrawer: {
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
    secondLevel: {
      type: String,
      default: null,
    },
    hasAdministrationNavigations: {
      type: Boolean,
      default: false,
    },
    administrationNavigations: {
      type: Array,
      default: null,
    },
    administrationCategories: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
  }),
  computed: {
    drawerLeft() {
      return this.displaySequentially && this.drawerWidth || 0;
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
