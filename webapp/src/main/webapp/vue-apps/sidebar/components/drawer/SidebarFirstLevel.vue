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
    ref="HamburgerMenuNavigation"
    id="HamburgerMenuNavigation"
    :value="firstLevelDrawer"
    :drawer-width="drawerWidth"
    :drawer-style="drawerStyle"
    :levels-opened="levelsOpened"
    class="HamburgerMenuFirstLevelParent layout-side-bar no-box-shadow border-box-sizing"
    @opened="$emit('firstLevelDrawer', true)"
    @closed="$emit('firstLevelDrawer', false)">
    <v-hover v-model="$root.hoverFirstLevel">
      <v-card
        :aria-label="$t('menu.role.navigation.first.level')"
        :max-width="drawerWidth"
        max-height="var(--100vh, 100vh)"
        class="d-flex flex-column fill-height HamburgerNavigationMenu transparent"
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
    openedSpace: {
      type: Object,
      default: null,
    },
    drawerWidth: {
      type: Number,
      default: null,
    },
  },
  data: () => ({
    drawerStyle: null,
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
    levelsOpened() {
      return this.secondLevelDrawer || this.thirdLevelDrawer;
    },
    stickyDisplay() {
      return this.$root.sticky;
    },
    iconCollapse() {
      return this.$root.iconCollapse;
    },
  },
  watch: {
    iconCollapse: {
      immediate: true,
      handler() {
        if (this.iconCollapse) {
          window.setTimeout(() => {
            if (this.iconCollapse) {
              this.drawerStyle = 'z-index: 2 !important';
            } else {
              this.drawerStyle = '';
            }
          }, 500);
        } else {
          this.drawerStyle = '';
        }
      },
    },
    firstLevelDrawer(newVal) {
      this.$nextTick().then(()  => {
        if (newVal) {
          this.lastFocusedElement = document.activeElement;
          this.focusNavigationDrawer().then(() => {
            this.firstElementFocusable?.focus();
            if (this.$root.mode === 'ICON') {
              this.$emit('focusedDrawer', true);
            }
          });
        } else {
          this.$emit('focusedDrawer', false); 
        }
      });
    },
  },
  beforeDestroy() {
    if (this.$refs.HamburgerMenuNavigation) {
      this.removeEventListenerKeydown(this.$refs?.HamburgerMenuNavigation?.$el, this.keydownListener);
      this.removeEventListenerKeydown(this.firstElementFocusable, this.firstListener);
      this.removeEventListenerKeydown(this.lastElementFocusable, this.lastListener);
      this.removeEventListenerKeydown(this.secondLastElementFocusable, this.lastListener);
      this.removeEventListenerKeydown(this.lastElementDisabled, this.lastDisabledListener);
    }
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
        this.addEventListenerKeydown(this.$refs?.HamburgerMenuNavigation?.$el, this.keydownListener);
      }
    },
    keydownListener(event) {
      if (event.key === 'Escape') {
        this.$nextTick(() => {
          this.$emit('input', false);
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
      } else if (event.key === 'Tab' && this.$root.mode === 'ICON' && !this.$root.icon) {
        this.$emit('focusedDrawer', true);
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
            this.lastElementDisabled?.focus();
          } else {
            this.firstElementFocusable?.focus();
          }
        } else if (event.shiftKey && elemntFocusable === 'firstElementFocusable') {
          if (this.secondLastElementFocusable) {
            //event.preventDefault();
            this.secondLastElementFocusable?.focus();
          } else {
            //event.preventDefault();
            this.lastElementFocusable?.focus();
          }
        }
      }
    },
    getVisibleFocusableElements() {
      const listFocusableElement = this.$refs?.HamburgerMenuNavigation?.$el.querySelectorAll('button, [href], input, [tabindex]:not([tabindex="-1"])');
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