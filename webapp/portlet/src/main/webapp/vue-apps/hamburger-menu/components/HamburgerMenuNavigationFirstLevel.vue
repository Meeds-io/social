<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
    :is="stickyDisplay && 'hamburger-menu-parent-menu' || 'hamburger-menu-parent-drawer'"
    id="HamburgerMenuNavigation"
    :value="firstLevelDrawer"
    :drawer-width="drawerWidth"
    :levels-opened="levelsOpened"
    class="no-box-shadow"
    @opened="$emit('firstLevelDrawer', true)"
    @closed="$emit('firstLevelDrawer', false)">
    <v-card
      :aria-label="$t('menu.role.navigation.first.level')"
      :min-width="drawerWidth"
      :max-width="drawerWidth"
      max-height="100vh"
      class="d-flex flex-column fill-height HamburgerNavigationMenu"
      role="navigation"
      color="white"
      flat
      tile>
      <profile-hamburger-navigation
        :value="stickyPreference"
        :sticky-allowed="stickyAllowed"
        class="flex-grow-0 flex-shrink-0"
        @input="$emit('stickyPreference', $event)" />
      <v-card
        id="StickyHamburgerMenu"
        :aria-label="$t('menu.role.navigation.first.level')"
        max-width="100%"
        class="overflow-y-overlay overflow-x-hidden flex-grow-1 flex-shrink-1"
        flat
        tile>
        <site-hamburger-navigation :navigations="siteNavigations" />
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
    siteNavigations: {
      type: Array,
      default: null,
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
