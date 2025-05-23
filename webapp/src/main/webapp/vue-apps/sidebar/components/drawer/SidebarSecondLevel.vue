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
    :right="$vuetify.rtl"
    class="HamburgerMenuSecondLevelParent layout-side-bar border-box-sizing z-index-drawer"
    max-width="100%"
    hide-overlay>
    <v-hover v-if="drawer" v-model="$root.hoverSecondLevel">
      <div class="full-width fill-height overflow-x-hidden overflow-y-auto specific-scrollbar">
        <spaces-hamburger-navigation
          v-if="secondLevel === 'spaces'"
          :opened-space="thirdLevelDrawer && openedSpace"
          @close="drawer = false" />
        <space-panel-hamburger-navigation
          v-else-if="secondLevel === 'spaceMenu'"
          :space="openedSpace"
          :home-link="homeLink"
          @close="drawer = false" />
        <site-details
          v-else-if="secondLevel === 'site'"
          :site="site"
          :enable-change-home="$root.allowUserHome"
          :display-sequentially="$root.displaySequentially"
          @close="drawer = false" />
      </div>
    </v-hover>
  </v-navigation-drawer>
</template>
<script>
export default {
  props: {
    value: {
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
    listFocusableElement: [],
    lastFocusedElement: null,
    firstElementFocusable: null,
    lastElementFocusable: null,
    lastElementDisabled: null,
    secondLastElementFocusable: null,
    firstListener: null,
    lastListener: null,
    secondLastListener: null,
    lastDisabledListener: null,
  }),
  computed: {
    drawerOffset() {
      return this.$root.displaySequentially && this.drawerWidth || 0;
    },
    drawerOffsetStyle() {
      return this.$vuetify.rtl && `right: ${this.drawerOffset}px;` || `left: ${this.drawerOffset}px;`;
    },
    expand() {
      return this.$root.expand;
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
      this.$nextTick().then(() => {
        this.focusNavigationDrawer().then(() => {
          this.firstElementFocusable?.focus();
          this.$emit('input', this.drawer);
        });
      });
    },
    value(newVal) {
      if (newVal) {
        this.lastFocusedElement = document.activeElement;
      }
      this.drawer = newVal;
    },
  },
  created() {
    this.drawer = this.value;
  },
  beforeDestroy() {
    this.removeEventListenerKeydown(this.$refs?.secondLevelDrawer?.$el, this.keydownListener);
    this.removeEventListenerKeydown(this.firstElementFocusable, this.firstListener);
    this.removeEventListenerKeydown(this.lastElementFocusable, this.lastListener);
    this.removeEventListenerKeydown(this.secondLastElementFocusable, this.lastListener);
    this.removeEventListenerKeydown(this.lastElementDisabled, this.lastDisabledListener);
  },
  methods: {
    async focusNavigationDrawer() {
      await this.$nextTick();
      await new Promise(resolve => setTimeout(resolve, 200));
      this.listFocusableElement = this.getVisibleFocusableElements();
      this.firstElementFocusable = this.listFocusableElement[0];
      this.lastElementFocusable = this.listFocusableElement[this.listFocusableElement.length-1];
      if (this.listFocusableElement.length > 0) {
        this.firstListener = (e) => this.handleFocusableKeydown('firstElementFocusable', e);
        this.lastListener = (e) => this.handleFocusableKeydown('lastElementFocusable', e);
        this.addEventListenerKeydown(this.firstElementFocusable, this.firstListener);
        this.addEventListenerKeydown(this.lastElementFocusable, this.lastListener);
        this.addEventListenerKeydown(this.$refs?.secondLevelDrawer?.$el, this.keydownListener);
      }
    },
    keydownListener(event) {
      if (event.key === 'Escape') {
        this.$emit('input', false);
        this.$nextTick(() => {
          if (this.lastFocusedElement) {
            this.lastFocusedElement?.focus();
          }
        });
      } else if (event.key === 'Enter') {
        setTimeout(() => {
          const listFocusableElement = this.getVisibleFocusableElements();
          if (!listFocusableElement.includes(this.lastElementFocusable)) {
            this.removeEventListenerKeydown(this.lastElementFocusable, this.lastListener);
            this.lastElementFocusable = listFocusableElement[listFocusableElement.length-1];
            this.addEventListenerKeydown(this.lastElementFocusable, this.lastListener);
          }
        }, 500);
      }
    },
    handleFocusableKeydown(elemntFocusable, event) {
      if (event && event.key !== 'Tab') {
        return;
      }
      const listFocusableElement = this.getVisibleFocusableElements();
      const secondLastElementFocusable = listFocusableElement[listFocusableElement.length-1];
      if (secondLastElementFocusable !== this.lastElementFocusable && !this.secondLastElementFocusable && secondLastElementFocusable !== this.secondLastElementFocusable) {
        this.secondLastElementFocusable = secondLastElementFocusable;
        this.secondLastListener = (e) => this.handleFocusableKeydown('secondLastElementFocusable', e);
        this.addEventListenerKeydown(this.secondLastElementFocusable, this.secondLastListener);
        return;
      } else if (secondLastElementFocusable === this.lastElementFocusable && this.secondLastElementFocusable) {
        this.removeEventListenerKeydown(this.secondLastElementFocusable, this.secondLastListener);
        this.secondLastElementFocusable = null;
      } else if (secondLastElementFocusable !== this.lastElementFocusable && !this.lastElementDisabled && secondLastElementFocusable !== this.secondLastElementFocusable) {
        this.lastElementDisabled = secondLastElementFocusable;
        this.lastDisabledListener = (e) => this.handleFocusableKeydown('lastElementDisabled', e);
        this.addEventListenerKeydown(this.lastElementDisabled, this.lastDisabledListener);
      } else if (secondLastElementFocusable !== this.lastElementFocusable && elemntFocusable === 'lastElementFocusable') {
        return;
      }
      
      if (event.key === 'Tab') {
        if (!event.shiftKey && elemntFocusable !== 'firstElementFocusable') {
          if (this.lastElementDisabled && elemntFocusable === 'secondLastElementFocusable') {
            event.preventDefault();
            this.lastElementDisabled?.focus();
          } else {
            event.preventDefault();
            this.firstElementFocusable?.focus();
          }
        } else if (event.shiftKey && elemntFocusable === 'firstElementFocusable') {
          if (this.secondLastElementFocusable) {
            event.preventDefault();
            this.secondLastElementFocusable?.focus();
          } else {
            event.preventDefault();
            this.lastElementFocusable?.focus();
          }
        }
      }
    },
    getVisibleFocusableElements() {
      const listFocusableElement = this.$refs?.secondLevelDrawer?.$el.querySelectorAll('button, [href], input, [tabindex]:not([tabindex="-1"])');
      return Array.from(listFocusableElement).filter(el  => { return this.checkVisibleElement(el);});
    },
    checkVisibleElement(element) {
      return element?.offsetParent !== null && element instanceof HTMLElement && window?.getComputedStyle(element)?.visibility !== 'hidden' && window?.getComputedStyle(element)?.display !== 'none';
    },
    isSameElement(element1, element2) {
      if (!element1 || !element2) {
        return false;
      }
      return element1.isSameNode(element2);
    },
    addEventListenerKeydown(element, event) {
      if (element && typeof element.addEventListener === 'function') {
        element.addEventListener('keydown', event);
      }
    },
    removeEventListenerKeydown(element, event) {
      if (element && typeof element.addEventListener === 'function') {
        element.removeEventListener('keydown', event);
      }
    },
  }
};
</script>
