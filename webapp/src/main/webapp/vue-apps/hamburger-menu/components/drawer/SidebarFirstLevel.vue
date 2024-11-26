<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <component
    :is="stickyDisplay && 'sidebar-parent-menu' || 'sidebar-parent-drawer'"
    id="HamburgerMenuNavigation"
    :value="firstLevelDrawer"
    :drawer-width="drawerWidth"
    :levels-opened="levelsOpened"
    class="HamburgerMenuFirstLevelParent no-box-shadow border-box-sizing"
    @opened="$emit('firstLevelDrawer', true)"
    @closed="$emit('firstLevelDrawer', false)">
    <v-hover v-model="$root.hoverFirstLevel">
      <v-card
        :aria-label="$t('menu.role.navigation.first.level')"
        :max-width="drawerWidth"
        max-height="var(--100vh, 100vh)"
        class="d-flex flex-column fill-height HamburgerNavigationMenu"
        role="navigation"
        color="white"
        flat
        tile>
        <sidebar-list />
      </v-card>
    </v-hover>
  </component>
</template>
<script>
export default {
  props: {
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
    sites: {
      type: Array,
      default: () => [],
    },
    openedSite: {
      type: Object,
      default: null,
    },
    recentSpaces: {
      type: Array,
      default: null,
    },
    openedSpace: {
      type: Object,
      default: null,
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
      return this.$root.sticky;
    },
  }
};
</script>