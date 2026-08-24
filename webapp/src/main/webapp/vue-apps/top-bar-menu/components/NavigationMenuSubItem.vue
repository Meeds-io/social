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
  <v-list
    class="pa-0"
    dense>
    <v-list-item
      v-if="hasPage || hasChildren && childrenHasPage"
      ref="row"
      :href="navigationNodeUri"
      :target="navigationNodeTarget"
      :rel="navigationNodeRel"
      :link="!!hasPage"
      class="py-0 px-0 transparent"
      @click="checkLink"
      @keydown.stop
      @focus="showMenu = hasChildren && childrenHasPage || false"
      @keydown.tab="$emit('exit-menu')"
      @keydown.right="focusHighlightedItem"
      @keydown.left="focusHighlightedItem">
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
        eager
        offset-x>
        <template #activator>
          <div
            class="d-flex width-full px-4"
            @mouseleave="showMenu = false">
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
              v-if="hasChildren && childrenHasPage"
              class="ms-0 me-n2 ma-auto full-height">
              <span class="d-flex align-center" aria-hidden="true">
                <v-icon class="pa-3" size="18">
                  {{ $vuetify.rtl && 'fa-angle-left' || 'fa-angle-right' }}
                </v-icon>
              </span>
            </v-list-item-icon>
          </div>
        </template>
        <navigation-menu-sub-item
          v-for="children in navigation.children"
          class="transparent"
          :key="children.id"
          :navigation="children"
          :parent-navigation-uri="parentNavigationUri"
          :base-site-uri="baseSiteUri"
          :selected-path="selectedPath"
          :highlighted="renderedChildren[highlightedIndex] === children"
          @update-navigation-state="updateNavigationState"
          @exit-menu="$emit('exit-menu')"
          @select="$emit('select')" />
      </v-menu>
    </v-list-item>
  </v-list>
</template>

<script>
export default {
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
    highlighted: {
      type: Boolean,
      default: false
    },
  },
  data() {
    return {
      isOpenedOnHover: true,
      showMenu: false,
      rowElement: null,
      highlightedIndex: -1,
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
    renderedChildren() {
      return this.navigation?.children?.filter(child => !!child.pageKey
        || child.children?.length && this.checkChildrenHasPage(child)) || [];
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
    highlighted() {
      this.showMenu = this.highlighted && (this.hasChildren && this.childrenHasPage || false);
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
    // the arrow keys only add a css class on the highlighted entry, so watch the menu own index
    // to tell that entry to open its submenu right away, without waiting for a horizontal arrow
    this.$watch(() => this.$refs.menu?.listIndex, index => this.highlightedIndex = index);
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
      } else if (!this.hasPage && this.hasChildren && this.childrenHasPage) {
        this.showMenu = !this.showMenu;
      }
    },
    focusHighlightedItem() {
      // same hand-off as the parent level, which makes it work at any depth
      this.$refs.menu?.$refs?.content?.querySelector('.v-list-item--highlighted')?.focus();
    },
    updateNavigationState(value) {
      this.$emit('update-navigation-state', value);
    },
    checkChildrenHasPage(navigation) {
      let childrenHasPage = false;
      navigation.children.forEach(child => {
        if (childrenHasPage === true) {
          return;
        }
        if (child.pageKey) {
          childrenHasPage = true;
        } else if (child.children.length > 0) {
          childrenHasPage = this.checkChildrenHasPage(child);
        } else {
          childrenHasPage = false;
        }
      });
      return childrenHasPage;
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