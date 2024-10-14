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
  <!-- Added after third level to make the drawer close animation smooth -->
  <v-navigation-drawer
    ref="secondLevelDrawer"
    v-model="drawer"
    :width="drawerWidth"
    :style="drawerOffsetStyle"
    :right="$root.rtl"
    class="HamburgerMenuSecondLevelParent border-box-sizing"
    max-width="100%"
    hide-overlay>
    <template v-if="drawer">
      <recent-spaces-hamburger-navigation
        v-if="secondLevel === 'recentSpaces'"
        :display-sequentially="displaySequentially"
        :opened-space="thirdLevelDrawer && openedSpace"
        @close="drawer = false" />
      <space-panel-hamburger-navigation
        v-else-if="secondLevel === 'spaceMenu'"
        :display-sequentially="displaySequentially"
        :space="openedSpace"
        :home-link="homeLink"
        @close="drawer = false" />
      <site-details
        v-else-if="secondLevel === 'site'"
        :site="site"
        :display-sequentially="displaySequentially"
        enable-change-home
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
    site: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
  }),
  computed: {
    drawerOffset() {
      return this.displaySequentially && this.drawerWidth || 0;
    },
    drawerOffsetStyle() {
      return this.$root.ltr && `left: ${this.drawerOffset}px;` || `right: ${this.drawerOffset}px;`;
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
