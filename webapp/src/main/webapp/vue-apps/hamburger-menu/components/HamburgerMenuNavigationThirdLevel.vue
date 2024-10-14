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
  <v-navigation-drawer
    ref="thirdLevelDrawer"
    v-model="drawer"
    :width="drawerWidth"
    :style="drawerOffsetStyle"
    :right="$root.rtl"
    class="HamburgerMenuThirdLevelParent border-box-sizing"
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
    drawerOffset() {
      return this.displaySequentially && this.drawerWidth * 2 || 0;
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
