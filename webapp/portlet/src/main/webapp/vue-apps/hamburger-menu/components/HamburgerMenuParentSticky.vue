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
  <v-menu
    :value="true"
    :absolute="false"
    :close-on-click="false"
    :close-on-content-click="false"
    :content-class="`overflow-hidden elevation-0 fill-height ${extraClass} ${componentId}`"
    :min-width="drawerWidth"
    max-width="none"
    attach="#ParentSiteStickyMenu"
    eager
    tile>
    <slot></slot>
  </v-menu>
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
    levelsOpened: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    open: false,
    componentId: `sticky-menu-${parseInt(Math.random() * 65536)}`,
    extraClass: '',
  }),
  watch: {
    levelsOpened() {
      if (this.levelsOpened) {
        this.extraClass = 'z-index-drawer';
      } else {
        window.setTimeout(() => {
          this.extraClass = '';
        }, 300);
      }
    },
    open() {
      if (this.value !== this.open) {
        this.$emit('input', this.open);
      }
    },
    value() {
      if (this.value !== this.open) {
        this.open = this.value;
      }
    },
  },
  created() {
    window.addEventListener('beforeunload', this.cacheMenuContent);
  },
  mounted() {
    window.setTimeout(this.hideCachedMenu, 200);
  },
  methods: {
    cacheMenuContent() {
      const menuElement = document.querySelector('#ParentSiteStickyMenu > .v-menu__content');
      menuElement.style.zIndex = String(parseInt(menuElement.style.zIndex) - 1);
      sessionStorage.setItem('ParentSiteStickyMenu', document.querySelector('#ParentSiteStickyMenu').innerHTML);
    },
    hideCachedMenu() {
      const menuElements = document.querySelectorAll('#ParentSiteStickyMenu > .v-menu__content');
      for (const menuElement of menuElements) {
        if (!menuElement.classList.contains(this.componentId)) {
          menuElement.remove();
        }
      }
    },
  },
};
</script>
