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
    :permanent="$root.icon"
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
  }),
  computed: {
    width() {
      return this.$root.expand ? this.drawerWidth : 67;
    },
    stickyAllowed() {
      return this.$root.stickyAllowed;
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
