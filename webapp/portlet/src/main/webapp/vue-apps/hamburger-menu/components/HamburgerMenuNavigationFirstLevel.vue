<template>
  <component
    :is="stickyDisplay && 'hamburger-menu-parent-menu' || 'hamburger-menu-parent-drawer'"
    id="HamburgerMenuNavigation"
    :value="firstLevelDrawer"
    :drawer-width="drawerWidth"
    :levels-opened="levelsOpened"
    class="no-box-shadow"
    @opened="$emit('firstLevelDrawer', true)"
    @closed="$emit('firstLevelDrawer', false)">
    <v-card
      id="StickyHamburgerMenu"
      :aria-label="$t('menu.role.navigation.first.level')"
      :min-width="drawerWidth"
      :max-width="drawerWidth"
      max-height="100vh"
      class="d-flex flex-column fill-height HamburgerNavigationMenu"
      role="navigation"
      color="white"
      flat>
      <profile-hamburger-navigation
        :value="stickyPreference"
        :sticky-allowed="stickyAllowed"
        class="flex-grow-0 flex-shrink-0"
        @input="$emit('stickyPreference', $event)" />
      <v-card
        id="StickyHamburgerMenu"
        :aria-label="$t('menu.role.navigation.first.level')"
        max-width="100%"
        class="overflow-y-auto overflow-x-hidden flex-grow-1 flex-shrink-1"
        flat>
        <site-hamburger-navigation />
        <spaces-hamburger-navigation
          :recent-spaces-drawer-opened="recentSpacesDrawerOpened"
          :opened-space="openedSpace"
          :third-level="thirdLevelDrawer" />
        <administration-hamburger-navigation
          v-if="hasAdministrationNavigations"
          :opened-menu="secondLevel === 'administration'" />
        <user-hamburger-navigation />
      </v-card>
    </v-card>
  </component>
</template>
<script>
export default {
  props: {
    stickyPreference: {
      type: Boolean,
      default: false,
    },
    firstLevelDrawer: {
      type: Boolean,
      default: false,
    },
    secondLevelDrawer: {
      type: Boolean,
      default: false,
    },
    thirdLevelDrawer: {
      type: Boolean,
      default: false,
    },
    secondLevel: {
      type: Boolean,
      default: false,
    },
    hasAdministrationNavigations: {
      type: Boolean,
      default: false,
    },
    openedSpace: {
      type: Object,
      default: null,
    },
    stickyAllowed: {
      type: Boolean,
      default: false,
    },
    drawerWidth: {
      type: Number,
      default: null,
    },
  },
  computed: {
    levelsOpened() {
      return this.secondLevelDrawer || this.thirdLevelDrawer;
    },
    recentSpacesDrawerOpened() {
      return this.secondLevelDrawer && this.secondLevel === 'recentSpaces';
    },
    stickyDisplay() {
      return this.stickyPreference && this.stickyAllowed;
    },
  },
};
</script>
