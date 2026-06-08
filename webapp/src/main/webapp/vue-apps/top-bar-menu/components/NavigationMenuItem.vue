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
    v-model="showMenu"
    rounded
    :content-class="`topBar-navigation-drop-menu ${isTopBarElement && 'layout-top-bar' || ''}`"
    :left="$vuetify.rtl"
    :open-on-hover="isOpenedOnHover"
    :max-height="menuMaxHeight"
    bottom
    offset-y
    eager>
    <template #activator="{ on, attrs }">
      <v-tab
        v-if="hasPage || hasChildren && childrenHasPage"
        :href="navigationNodeUri"
        :target="navigationNodeTarget"
        :rel="navigationNodeRel"
        :link="hasPage"
        :aria-label="navigation.label"
        :aria-current="isSelected && 'true'"
        :aria-selected="isSelected && 'true'"
        :class="`mx-auto text-break ${notClickable}`"
        :value="navigationNodeUri"
        v-on="on"
        v-bind="attrs"
        role="tab"
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
        <v-btn
          v-if="hasChildren && childrenHasPage"
          icon
          @click.stop.prevent="openDropMenu"
          @mouseover="showMenu = true">
          <v-icon size="20">
            fa-angle-down
          </v-icon>
        </v-btn>
      </v-tab>
    </template>
    <navigation-menu-sub-item
      v-for="children in navigation.children"
      class="transparent"
      :key="children.id"
      :navigation="children"
      :base-site-uri="baseSiteUri"
      :parent-navigation-uri="navigation.uri"
      :selected-path="selectedPath"
      @update-navigation-state="updateNavigationState"
      @select="updateNavigationState" />
  </v-menu>
</template>

<script>
export default {
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
    isTopBarElement() {
      return this.$root.isTopBarElement;
    }
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
      } else if (this.hasChildren && this.childrenHasPage) {
        this.openDropMenu();
      }
    },
    openDropMenu(persist) {
      if (!persist && this.showMenu) {
        this.showMenu = false;
      } else if (!this.showMenu) {
        this.showMenu = true;
        this.$root.$emit('close-sibling-drop-menus', this);
      }
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
  }
};
</script>
