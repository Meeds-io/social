<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <v-menu
    ref="menu"
    v-model="showMenu"
    rounded
    :content-class="`topBar-navigation-drop-menu ${isTopBarElement && 'layout-top-bar' || ''}`"
    :left="$vuetify.rtl"
    :open-on-hover="isOpenedOnHover"
    :open-on-click="false"
    :max-height="menuMaxHeight"
    bottom
    offset-y
    disable-keys
    eager>
    <template #activator="{ on, attrs }">
      <v-tab
        v-if="hasPage || hasSubMenu"
        ref="tab"
        :href="navigationNodeUri"
        :target="navigationNodeTarget"
        :rel="navigationNodeRel"
        :link="hasPage"
        :aria-label="navigation.label"
        :class="`mx-auto text-break ${notClickable}`"
        :value="navigationNodeUri"
        v-on="on"
        v-bind="attrs"
        role="tab"
        :aria-haspopup="hasSubMenu && 'true' || null"
        :aria-expanded="hasSubMenu && String(showMenu) || null"
        @focus="openDropMenuOnFocus"
        @keydown.tab="showMenu = false"
        @keydown.down.prevent="walkDeeper"
        @keydown.up.prevent="walkDeeperFromEnd"
        @keydown.right.prevent="onForwardKey"
        @keydown.left.prevent="onBackKey"
        @keydown.esc="showMenu = false"
        @keydown.enter="onEnterKey"
        @click="checkLink"
        @change="updateNavigationState">
        <span
          class="text-truncate-3">
          {{ navigation.label }}
        </span>
        <v-icon
          v-if="navigation.target === 'NEW_TAB'"
          size="12"
          class="mx-1">
          fa-external-link-alt
        </v-icon>
        <span
          v-if="hasSubMenu"
          class="d-flex align-center"
          aria-hidden="true">
          <v-icon class="ms-3" size="20">
            fa-angle-down
          </v-icon>
        </span>
      </v-tab>
    </template>
    <navigation-menu-sub-item
      v-for="children in navigation.children"
      ref="entries"
      class="transparent"
      :key="children.id"
      :navigation="children"
      :base-site-uri="baseSiteUri"
      :parent-navigation-uri="navigation.uri"
      :selected-path="selectedPath"
      @update-navigation-state="updateNavigationState"
      @exit-menu="exitMenu"
      @walk="walkFromEntry"
      @leave-level="focusSelf"
      @select="updateNavigationState" />
  </v-menu>
</template>

<script>
import menuKeyboardNavigation from '../menuKeyboardNavigation.js';

export default {
  mixins: [menuKeyboardNavigation],
  props: {
    navigation: {
      type: Object,
      default: null,
    },
    baseSiteUri: {
      type: String,
      default: null
    },
    selectedPath: {
      type: String,
      default: null
    },
  },
  data () {
    return {
      showMenu: false,
      exitingMenu: false,
      isOpenedOnHover: true,
      menuMaxHeight: '100vh'
    };
  },
  computed: {
    isSelected() {
      return this.selectedPath === this.navigationNodeUri;
    },
    notClickable() {
      return `${this.hasPage ? ' ' : ' not-clickable ' }`;
    },
    hasChildren() {
      return this.navigation?.children?.length;
    },
    hasPage() {
      return !!this.navigation?.pageKey;
    },
    navigationNodeUri() {
      return this.$navigationUtils.getNavigationNodeUri(this.baseSiteUri, this.navigation);
    },
    navigationNodeTarget() {
      return this.$navigationUtils.getNavigationNodeTarget(this.navigation);
    },
    navigationNodeRel() {
      return this.$navigationUtils.getNavigationNodeRel(this.navigation);
    },
    childrenHasPage() {
      return this.checkChildrenHasPage(this.navigation);
    },
    hasSubMenu() {
      // a node opens a submenu only when it has children that actually lead somewhere;
      // this is what aria-haspopup/aria-expanded must reflect, so it is computed once
      return !!(this.hasChildren && this.childrenHasPage);
    },
    isTopBarElement() {
      return this.$root.isTopBarElement;
    },
  },
  watch: {
    showMenu() {
      this.isOpenedOnHover = !this.showMenu;
      this.$root.$emit('close-sibling-drop-menus', this); 
    },
    isSelected: {
      immediate: true,
      handler() {
        if (this.isSelected) {
          this.updateNavigationState();
        }
      }
    },
  },
  created() {
    document.addEventListener('click', this.handleCloseMenu);
    this.$root.$on('close-sibling-drop-menus', this.handleCloseSiblingMenus);
  },
  beforeDestroy() {
    // the navigation tree is rebuilt on `space-settings-updated`, so these components are
    // destroyed and recreated: without this, every rebuild leaves a document listener and a
    // $root handler behind, holding the dead instance alive (EXO-88911 review)
    document.removeEventListener('click', this.handleCloseMenu);
    this.$root.$off('close-sibling-drop-menus', this.handleCloseSiblingMenus);
  },
  methods: {
    updateNavigationState() {
      this.$emit('update-navigation-state', this.navigationNodeUri);
    },
    checkLink(e) {
      if (!this.navigationNodeUri) {
        e.stopPropagation();
        e.preventDefault();
      }
      if (this.navigationNodeUri?.includes?.('#')) {
        if (this.navigationNodeTarget === '_blank') {
          window.open(this.navigationNodeUri);
        } else {
          window.location.href = this.navigationNodeUri;
        }
      }
    },
    openDropMenuOnFocus() {
      if (this.exitingMenu) {
        // the focus is only passing by on its way out of the menu, don't open it again
        return;
      }
      this.showMenu = this.hasSubMenu;
    },
    exitMenu() {
      // Tab from a sub item must leave the menu instead of walking through the drop menu
      // contents, which are detached at the end of the document: close the whole menu and
      // give the focus back to this tab, so the browser default moves on to the next tab
      this.exitingMenu = true;
      this.showMenu = false;
      this.$refs.tab?.$el?.focus();
      this.$nextTick(() => this.exitingMenu = false);
    },
    handleCloseSiblingMenus(emitter) {
      if (this !== emitter && this.showMenu) {
        this.showMenu = false;
      }
    },
    handleCloseMenu() {
      if (this.showMenu) {
        setTimeout(() => {
          this.showMenu = false;
        }, 100);
      }
    },
  }
};
</script>
