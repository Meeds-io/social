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
    :right="$vuetify.rtl"
    :style="{
      ...drawerOffsetStyle,
      'z-index': zIndex,
    }"
    id="HamburgerMenuThirdLevelPanel"
    role="region"
    :aria-label="dialogLabel"
    class="HamburgerMenuThirdLevelParent layout-side-bar border-box-sizing"
    max-width="100%"
    hide-overlay
    @keydown.native="handlePanelKeydown">
    <v-hover v-if="drawer" v-model="$root.hoverThirdLevel">
      <div class="full-width fill-height overflow-x-hidden overflow-x-auto specific-scrollbar">
        <space-panel-hamburger-navigation
          :space="openedSpace"
          :home-link="homeLink"
          :opened-space="openedSpace"
          @close="drawer = false" />
      </div>
    </v-hover>
  </v-navigation-drawer>
</template>
<script>
import panelFocusTrap from '../../mixins/panelFocusTrap.js';

export default {
  mixins: [panelFocusTrap],
  props: {
    value: {
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
    drawerZIndex: 1035,
  }),
  computed: {
    zIndex() {
      return this.drawer ? (this.drawerZIndex + (eXo.openedDrawers?.length || 0)) : this.drawerZIndex;
    },
    drawerOffset() {
      return this.$root.displaySequentially && this.drawerWidth * 2 || 0;
    },
    drawerOffsetStyle() {
      return this.$vuetify.rtl && {right: `${this.drawerOffset}px`} || {left: `${this.drawerOffset}px`};
    },
    expand() {
      return this.$root.expand;
    },
    dialogLabel() {
      return this.openedSpace?.displayName || this.$t('menu.spaces.yourSpaces');
    },
  },
  watch: {
    expand() {
      if (!this.expand) {
        this.$nextTick().then(() => {
          if (!this.expand && this.drawer) {
            this.drawer = false;
          }
        });
      }
    },
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
