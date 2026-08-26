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
  <!-- role="none": one v-list wraps each entry, so without it a generic container sits between
       the menu (role="menu", set by Vuetify on its content) and its menuitems, and the menu no
       longer owns its own items - which is what a screen reader counts and announces -->
  <v-list
    class="pa-0"
    role="none"
    dense>
    <!-- the keyboard counterpart of @mouseleave is not @blur here: the arrow keys hand focus
         over to the submenu on purpose, so closing on blur would shut it as it opens. Focus
         leaving the menu for real is handled by @keydown.tab -> exit-menu. -->
    <!-- eslint-disable-next-line vuejs-accessibility/mouse-events-have-key-events -->
    <v-list-item
      v-if="hasPage || hasSubMenu"
      ref="row"
      :href="navigationNodeUri"
      :target="navigationNodeTarget"
      :rel="navigationNodeRel"
      :link="!!hasPage"
      :aria-label="navigation.label"
      :aria-haspopup="hasSubMenu && 'true' || null"
      :aria-expanded="hasSubMenu && String(showMenu) || null"
      class="py-0 px-0 transparent navigation-menu-sub-item"
      @click="checkLink"
      @keydown.stop
      @focus="showMenu = hasSubMenu"
      @mouseleave="showMenu = false"
      @keydown.tab="$emit('exit-menu')"
      @keydown.down.prevent="walkNext"
      @keydown.up.prevent="walkPrevious"
      @keydown.right.prevent="onForwardKey"
      @keydown.left.prevent="onBackKey"
      @keydown.esc.prevent="walkBack"
      @keydown.enter="onEnterKey">
      <div
        class="d-flex width-full px-4">
        <v-list-item-title
          class="pt-5 pb-5 d-flex"
          :class="hasPage && ' ' || ' not-clickable '">
          <span class="text-body">{{ navigation.label }}</span>
          <v-icon
            v-if="navigation.target === 'NEW_TAB'"
            size="12"
            class="mx-1">
            fa-external-link-alt
          </v-icon>
        </v-list-item-title>
        <v-list-item-icon
          v-if="hasSubMenu"
          class="ms-0 me-n2 ma-auto full-height">
          <span class="d-flex align-center" aria-hidden="true">
            <v-icon class="pa-3" size="18">
              {{ $vuetify.rtl && 'fa-angle-left' || 'fa-angle-right' }}
            </v-icon>
          </span>
        </v-list-item-icon>
      </div>
      <v-menu
        ref="menu"
        v-model="showMenu"
        :activator="rowElement"
        :open-on-click="false"
        rounded
        :content-class="isTopBarElement && 'layout-top-bar' || ''"
        :position-x="positionX"
        :position-y="positionY"
        transition="slide-x-reverse-transition"
        :left="$vuetify.rtl"
        :open-on-hover="isOpenedOnHover"
        absolute
        disable-keys
        eager
        offset-x>
        <navigation-menu-sub-item
          v-for="children in navigation.children"
          ref="entries"
          class="transparent"
          :key="children.id"
          :navigation="children"
          :parent-navigation-uri="parentNavigationUri"
          :base-site-uri="baseSiteUri"
          :selected-path="selectedPath"
          @update-navigation-state="updateNavigationState"
          @exit-menu="$emit('exit-menu')"
          @walk="walkFromEntry"
          @leave-level="leaveLevel"
          @select="$emit('select')" />
      </v-menu>
    </v-list-item>
  </v-list>
</template>

<script>
import menuKeyboardNavigation from '../menuKeyboardNavigation.js';

export default {
  mixins: [menuKeyboardNavigation],
  props: {
    navigation: {
      type: Object,
      default: null
    },
    baseSiteUri: {
      type: String,
      default: null
    },
    parentNavigationUri: {
      type: String,
      default: null
    },
    selectedPath: {
      type: String,
      default: null
    },
  },
  data() {
    return {
      isOpenedOnHover: true,
      showMenu: false,
      rowElement: null,
      positionX: 0,
      positionY: 0,
    };
  },
  computed: {
    hasChildren() {
      return this.navigation?.children?.length;
    },
    hasPage() {
      return !!this.navigation?.pageKey;
    },
    childrenHasPage() {
      return this.checkChildrenHasPage(this.navigation);
    },
    hasSubMenu() {
      // a node opens a submenu only when it has children that actually lead somewhere;
      // this is what aria-haspopup/aria-expanded must reflect, so it is computed once
      return !!(this.hasChildren && this.childrenHasPage);
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
    isSelected() {
      return this.navigationNodeUri === this.selectedPath;
    },
    isTopBarElement() {
      return this.$root.isTopBarElement;
    },
  },
  watch: {
    isSelected: {
      immediate: true,
      handler() {
        if (this.isSelected) {
          this.$emit('select');
        }
      }
    },
    showMenu() {
      this.isOpenedOnHover = !this.showMenu;
      this.positionX = window.innerWidth - (window.innerWidth - this.$el.getBoundingClientRect().right);
      this.positionY = this.$el.getBoundingClientRect().top;
      this.$root.$emit('close-sibling-drop-menus-children', this);
    },
    hasPage() {
      return !!this.navigation?.pageKey;
    },
  },
  mounted() {
    this.rowElement = this.$refs.row?.$el;
  },
  created() {
    window.addEventListener('resize', this.updateSize);
    this.$root.$on('close-sibling-drop-menus-children', this.handleCloseSiblingMenus);
  },
  methods: {
    checkLink(e) {
      if (!this.navigationNodeUri) {
        e.stopPropagation();
        e.preventDefault();
      } else {
        this.$emit('update-navigation-state', `${this.parentNavigationUri}`);
      }
      if (this.navigationNodeUri?.includes?.('#')) {
        if (this.navigationNodeTarget === '_blank') {
          window.open(this.navigationNodeUri);
        } else {
          window.location.href = this.navigationNodeUri;
        }
      } else if (!this.hasPage && this.hasSubMenu) {
        this.showMenu = !this.showMenu;
      }
    },
    leaveLevel() {
      // an entry of the submenu asked to come back to this level: take the focus first, then
      // collapse the level the focus just left (the row's own @focus reopens it otherwise)
      if (!this.focusSelf()) {
        return;
      }
      this.showMenu = false;
    },
    updateNavigationState(value) {
      this.$emit('update-navigation-state', value);
    },
    handleCloseSiblingMenus(emitter) {
      if (!this.showMenu || !emitter) {
        return;
      }
      if (emitter === this) {
        return;
      }
      const myUri = this.navigationNodeUri;
      const emitterUri = emitter.navigationNodeUri;
      if (myUri && emitterUri && (myUri.startsWith(emitterUri) || emitterUri.startsWith(myUri))) {
        return;
      }
      if ((!myUri || emitter && myUri && !myUri.includes(emitter.baseSiteUri)) && this.isDescendant(emitter.navigation, this.navigation)) {
        return;
      }
      this.showMenu = false;
    },
    isDescendant(childNav, parentNav) {
      if (!parentNav?.children) {
        return false;
      }
      for (const child of parentNav.children) {
        if (child === childNav) {
          return true;
        }
        if (this.isDescendant(childNav, child)) {
          return true;
        }
      }
      return false;
    },
    updateSize() {
      this.positionX = window.innerWidth - (window.innerWidth - this.$el.getBoundingClientRect().right) ;
    }
  }
};
</script>