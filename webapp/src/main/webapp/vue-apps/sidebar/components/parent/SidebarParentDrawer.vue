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
  <exo-drawer
    v-model="drawer"
    ref="drawer"
    :drawer-width="width"
    :style="drawerStyle"
    :permanent="permanent"
    :show-overlay="showOverlay"
    :is-branding-layout="false"
    :autofocus="false"
    no-external-overlay
    attached
    left>
    <template #content>
      <slot></slot>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    value: {
      type: Boolean,
      default: false,
    },
    drawerWidth: {
      type: String,
      default: null,
    },
    drawerStyle: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
    hideOverlay: true,
  }),
  computed: {
    width() {
      return this.$root.expand ? this.drawerWidth : 70;
    },
    stickyAllowed() {
      return this.$root.stickyAllowed;
    },
    isMobile() {
      return this.$root.isMobile;
    },
    showOverlay() {
      const isMobile = this.$root.isMobile || window.innerWidth < this.$vuetify.breakpoint.thresholds.md;
      return (!isMobile || !this.hideOverlay) && (!this.$root.icon || this.$root.expand);
    },
    permanent() {
      return this.$root.icon;
    },
  },
  watch: {
    stickyAllowed() {
      if (!this.stickyAllowed && this.drawer) {
        this.drawer = false;
      } else if (this.$root.icon && !this.drawer) {
        this.drawer = true;
      }
    },
    isMobile: {
      immediate: true,
      handler(newVal, oldVal) {
        if (newVal !== oldVal) {
          this.hideOverlay = true;
          if (this.timeout) {
            window.clearTimeout(this.timeout);
          }
          if (this.isMobile) {
            this.timeout = window.setTimeout(() => {
              this.timeout = null;
              this.hideOverlay = false;
            }, 500);
          }
        }
      },
    },
    drawer() {
      if (!this.drawer && this.$root.icon) {
        this.$nextTick().then(() => this.drawer = true);
      } else if (this.value !== this.drawer) {
        this.$emit('input', this.drawer);
      }
    },
    value() {
      if (this.value !== this.drawer) {
        this.drawer = this.value;
      }
    },
  },
  mounted() {
    this.drawer = this.value;
  },
};
</script>
